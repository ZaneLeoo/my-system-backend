package com.ruoyi.signing.application;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.spi.*;
import java.util.*;

/** 先持久化后消费，回调只安排权威状态同步或回填当前定位会话。 */
public final class SigningEventProcessor {
  private final SigningRepository repository;
  private final SigningJsonCodec json;
  private final SigningProvider provider;
  private final SigningUnitOfWork transactions;
  private final DefaultSigningService service;

  /** 注入已验证事件仓库。 */
  public SigningEventProcessor(
      SigningRepository repository,
      SigningJsonCodec json,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      DefaultSigningService service) {
    this.repository = repository;
    this.json = json;
    this.provider = provider;
    this.transactions = transactions;
    this.service = service;
  }

  /** 消费最多二十条通知；单条失败保留，不吞掉其他消息。 */
  public void runOnce() {
    for (SigningEvent event : repository.pendingEvents()) {
      try {
        transactions.execute(
            () -> {
              consume(event);
              return null;
            });
      } catch (RuntimeException error) {
        repository.completeEvent(event.getId(), "PENDING");
      }
    }
  }

  /** 消费时再次匹配本地平台环境，不信任通知携带的业务主键。 */
  private void consume(SigningEvent event) {
    if (!repository.beginEvent(event.getId())) {
      return;
    }
    Map<String, Object> payload = object(json.read(event.getPayload(), Map.class));
    String kind = text(payload, "action");
    if ("GET_SEAL_POSITION".equals(kind)) {
      SigningTask task = repository.findByPositionSession(text(payload, "customBizNum"));
      if (task == null) {
        repository.completeEvent(event.getId(), "IGNORED");
        return;
      }
      if (task.getStatus() != SigningStatus.DRAFT
          || task.getOperationStatus() == SigningOperationStatus.QUEUED
          || task.getFlowId() != null) {
        repository.completeEvent(event.getId(), "IGNORED");
        return;
      }
      SigningDocument document = service.document(task);
      List<SigningParty> parties = repository.parties(task.getId());
      for (SigningParty party : parties) {
        party.setPositions(new ArrayList<SigningPosition>());
      }
      for (Object value : array(payload.get("components"))) {
        Map<String, Object> component = object(value);
        if (!document.getProviderFileId().equals(text(component, "fileId"))
            || !"6".equals(text(component, "componentType"))) {
          throw new SigningException("定位回调文件或控件不匹配");
        }
        Map<String, Object> position = object(component.get("componentPosition"));
        for (SigningParty party : parties) {
          if (party.getRole().equals(text(component, "signerRole"))) {
            SigningPosition point = new SigningPosition();
            point.setPage(Integer.parseInt(text(position, "componentPageNum")));
            point.setX(Double.parseDouble(text(position, "componentPositionX")));
            point.setY(Double.parseDouble(text(position, "componentPositionY")));
            party.getPositions().add(point);
          }
        }
      }
      repository.saveParties(task.getId(), parties);
      long version = task.getVersion();
      task.setPositionDocumentHash(null);
      service.save(task, version, "esign:callback");
      repository.attachEvent(event.getId(), task.getId());
      repository.completeEvent(event.getId(), "DONE");
      return;
    }
    String flow = text(payload, "signFlowId");
    if (flow.isEmpty()) {
      flow = text(payload, "flowId");
    }
    if (flow.isEmpty() || !(kind.startsWith("SIGN_") || kind.startsWith("SEAL_"))) {
      repository.completeEvent(event.getId(), "IGNORED");
      return;
    }
    SigningTask task = repository.findByFlow(provider.environment(), flow);
    if (task == null) {
      repository.completeEvent(event.getId(), "PENDING");
      return;
    }
    repository.attachEvent(event.getId(), task.getId());
    if (task.getStatus() == SigningStatus.SIGNING || task.getStatus() == SigningStatus.DRAFT) {
      repository.enqueueNow(task.getId(), SigningJobType.SYNC);
    }
    repository.completeEvent(event.getId(), "DONE");
  }
}
