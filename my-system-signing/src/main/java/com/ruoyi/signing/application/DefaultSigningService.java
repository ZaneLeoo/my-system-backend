package com.ruoyi.signing.application;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.command.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.model.enums.PartyRole;
import com.ruoyi.signing.spi.*;
import java.util.*;

/** 用户命令先完成外部只读校验，再以本地事务保存任务；外部写操作由处理器执行。 */
public class DefaultSigningService implements SigningService {
  private final SigningRepository repository;
  private final SigningProvider provider;
  private final SigningUnitOfWork transactions;
  private final SigningFileStore files;
  private final SigningJsonCodec json;
  private final Map<String, SigningBusinessAdapter> adapters;
  private final SigningSealService seals;
  private final SigningStateMachine states = new SigningStateMachine();

  /** 通过构造方法装配宿主能力。 */
  public DefaultSigningService(
      SigningRepository repository,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      SigningFileStore files,
      SigningJsonCodec json,
      Map<String, SigningBusinessAdapter> adapters,
      SigningSealService seals) {
    this.repository = repository;
    this.provider = provider;
    this.transactions = transactions;
    this.files = files;
    this.json = json;
    this.adapters = adapters;
    this.seals = seals;
  }

  /** 按登记业务生成快照。 */
  private BusinessSnapshot snapshot(String type, String businessId) {
    SigningBusinessAdapter adapter = adapters.get(type);
    if (adapter == null) {
      throw new SigningException("未登记该签署场景");
    }
    return adapter.snapshot(businessId);
  }

  /** 校验环境并读取任务。 */
  public SigningTask task(String id) {
    SigningTask task = repository.findTask(id);
    if (task == null) {
      throw new SigningException("签署任务不存在");
    }
    return task;
  }

  /** 从已保存快照读取默认签署方，不重新读取或覆盖用户维护的签署信息。 */
  public SigningParty defaultCounterparty(SigningTask task) {
    SigningBusinessAdapter adapter = adapters.get(task.getBusinessType());
    return adapter == null ? null : adapter.defaultCounterparty(task.getSnapshot());
  }

  /** 只有平台操作需要匹配当前应用；历史私有文件仍可鉴权查看。 */
  public void verifyEnvironment(SigningTask task) {
    if (!provider.environment().equals(task.getProviderEnvironment())) {
      throw new SigningException("任务所属平台应用或环境不匹配，请使用原应用处理");
    }
  }

  /** 更新版本及明确的审计人员。 */
  public void save(SigningTask task, long expected, String actor) {
    task.setVersion(Math.addExact(expected, 1));
    task.setUpdateBy(required(actor, 64, "操作人"));
    task.setUpdateTime(now());
    repository.updateTask(task, expected);
  }

  /** 写入无敏感值的操作事件。 */
  private void event(SigningTask task, String kind, String actor) {
    SigningEvent event = new SigningEvent();
    event.setId(id());
    event.setTaskId(task.getId());
    event.setKind(kind);
    event.setActor(actor);
    event.setDeduplicationKey(event.getId());
    event.setPayload("{}");
    repository.insertEvent(event);
  }

  /** {@inheritDoc} */
  public SigningTask create(CreateSigningTaskCommand command, String actor) {
    String type = required(command.getBusinessType(), 64, "业务类型");
    String businessId = required(command.getBusinessId(), 64, "业务主键");
    try {
      DocumentSource.valueOf(required(command.getSource(), 16, "文件来源"));
    } catch (IllegalArgumentException error) {
      throw new SigningException("文件来源必须为 UPLOAD 或 TEMPLATE");
    }
    BusinessSnapshot snapshot = snapshot(type, businessId);
    SigningTask task = new SigningTask();
    task.setId(id());
    task.setBusinessType(type);
    task.setBusinessId(businessId);
    task.setTitle(required(snapshot.getTitle(), 100, "合同标题"));
    task.setSnapshot(snapshot.getValues());
    task.setSnapshotHash(hash(json.write(snapshot.getValues())));
    task.setProviderEnvironment(provider.environment());
    task.setSource(required(command.getSource(), 16, "文件来源"));
    task.setBindingId(command.getBindingId());
    if ("TEMPLATE".equals(task.getSource())) {
      task.setBindingId(required(command.getBindingId(), 36, "模板绑定 ID"));
      SigningTemplateBinding binding = repository.binding(task.getBindingId());
      if (binding == null
          || !binding.isEnabled()
          || !type.equals(binding.getBusinessType())
          || !provider.environment().equals(binding.getProviderEnvironment())) {
        throw new SigningException("模板未启用或不属于当前业务和环境");
      }
      task.setTemplateBindingSnapshot(binding);
    }
    task.setCreateBy(required(actor, 64, "操作人"));
    task.setCreateTime(now());
    task.setUpdateBy(actor);
    task.setUpdateTime(task.getCreateTime());
    return transactions.execute(
        () -> {
          repository.insertTask(task);
          event(task, "CREATED", actor);
          return task;
        });
  }

  /** {@inheritDoc} */
  public SigningTask prepare(String id, long version, String name, byte[] bytes, String actor) {
    SigningTask initial = task(id);
    verifyEnvironment(initial);
    states.version(initial, version);
    states.editable(initial);
    SigningDocument source = null;
    if ("UPLOAD".equals(initial.getSource())) {
      String checked = required(name, 100, "文件名");
      if (!checked.toLowerCase(Locale.ROOT).matches(".*\\.(pdf|doc|docx)$")
          || checked.matches(".*[\\\\/:*?\"<>|].*")) {
        throw new SigningException("仅支持合法文件名的 PDF、DOC、DOCX");
      }
      if (bytes == null || bytes.length == 0 || bytes.length > 10 * 1024 * 1024) {
        throw new SigningException("文件不能为空且不能超过 10MB");
      }
      source = files.put(checked, bytes);
      source.setId(SigningSupport.id());
      source.setTaskId(id);
      source.setKind("SOURCE");
    }
    final SigningDocument uploaded = source;
    return transactions.execute(
        () -> {
          SigningTask task = task(id);
          states.version(task, version);
          states.editable(task);
          if (uploaded != null) {
            repository.saveDocument(uploaded);
            task.setActiveDocumentId(uploaded.getId());
          }
          task.setPositionDocumentHash(null);
          task.setPositionSession(null);
          task.setDocumentStatus(DocumentStatus.PREPARING);
          task.setOperationStatus(SigningOperationStatus.QUEUED);
          task.setLastError(null);
          repository.enqueue(id, SigningJobType.PREPARE);
          save(task, version, actor);
          event(task, "PREPARE", actor);
          return task;
        });
  }

  /** 校验我方和对方的主体信息、签署顺序及落章位置。 */
  private void validateParties(List<SigningParty> parties, boolean requirePositions) {
    if (parties == null || parties.size() != 2) {
      throw new SigningException("必须配置我方与对方两个签署方");
    }
    Set<String> roles = new HashSet<String>();
    for (SigningParty party : parties) {
      if (party == null
          || !(PartyRole.OUR.name().equals(party.getRole())
              || PartyRole.COUNTERPARTY.name().equals(party.getRole()))
          || !roles.add(party.getRole())) {
        throw new SigningException("签署方角色不合法或重复");
      }
      if (party.getPartyType() == null) {
        party.setPartyType(SigningPartyType.ORGANIZATION);
      }
      if (PartyRole.OUR.name().equals(party.getRole())
          && party.getPartyType() != SigningPartyType.ORGANIZATION) {
        throw new SigningException("我方签署主体必须是企业或机构");
      }
      if (party.getPartyType() == SigningPartyType.ORGANIZATION) {
        party.setOrganizationName(required(party.getOrganizationName(), 200, "机构名称"));
      } else if (party.getPartyType() == SigningPartyType.PERSON) {
        party.setOrganizationName(null);
      } else {
        throw new SigningException("签署主体类型不合法");
      }
      if (party.isAutoSign()) {
        if (!PartyRole.OUR.name().equals(party.getRole())) {
          throw new SigningException("自动落章仅限我方企业");
        }
      } else {
        party.setTransactorAccount(required(party.getTransactorAccount(), 100, "授权经办人账号"));
        party.setTransactorName(required(party.getTransactorName(), 64, "授权经办人姓名"));
      }
      if (party.getSignOrder() < 1
          || party.getSignOrder() > 255
          || party.getPositions() == null
          || (requirePositions && party.getPositions().isEmpty())
          || party.getPositions().size() > 20) {
        throw new SigningException("签署顺序或签章位置不合法");
      }
      for (SigningPosition position : party.getPositions()) {
        if (position == null
            || position.getPage() < 1
            || !Double.isFinite(position.getX())
            || !Double.isFinite(position.getY())
            || position.getX() < 0
            || position.getY() < 0) {
          throw new SigningException("签章坐标不合法");
        }
      }
    }
  }

  /** {@inheritDoc} */
  public SigningTask participants(
      String id, long version, List<SigningParty> parties, boolean confirmPositions, String actor) {
    validateParties(parties, confirmPositions);
    return transactions.execute(
        () -> {
          SigningTask task = task(id);
          states.version(task, version);
          states.editable(task);
          if (task.getDocumentStatus() != DocumentStatus.READY) {
            throw new SigningException("请先准备最终待签文件");
          }
          // 参与方接口不能改写仅允许在发起命令中设置的用章信息。
          List<SigningParty> previous = repository.parties(id);
          for (SigningParty party : parties) {
            SigningParty saved = null;
            for (SigningParty existing : previous) {
              if (existing.getRole().equals(party.getRole())) {
                saved = existing;
              }
            }
            party.setAutoSign(saved != null && saved.isAutoSign());
            party.setSealId(saved == null ? null : saved.getSealId());
            party.setSealName(saved == null ? null : saved.getSealName());
            party.setSealType(saved == null ? null : saved.getSealType());
          }
          validateParties(parties, confirmPositions);
          SigningDocument document = document(task);
          if (confirmPositions && document.getPageCount() > 0) {
            for (SigningParty party : parties) {
              for (SigningPosition point : party.getPositions()) {
                if (point.getPage() > document.getPageCount()) {
                  throw new SigningException("签署位置超过文件实际页数");
                }
              }
            }
          }
          for (SigningParty party : parties) {
            party.setId(SigningSupport.id());
            party.setTaskId(id);
            party.setStatus(null);
          }
          repository.saveParties(id, parties);
          task.setPositionDocumentHash(confirmPositions ? document.getSha256() : null);
          save(task, version, actor);
          event(task, "PARTICIPANTS_CONFIRMED", actor);
          return task;
        });
  }

  /** 在实际创建或启动前再次校验来源资格和业务内容。 */
  public void verifyBusiness(SigningTask task) {
    verifyEnvironment(task);
    BusinessSnapshot latest = snapshot(task.getBusinessType(), task.getBusinessId());
    if (!hash(json.write(latest.getValues())).equals(task.getSnapshotHash())) {
      throw new SigningException("来源内容已变化，请重新准备合同");
    }
  }

  /** 读取当前最终文档。 */
  public SigningDocument document(SigningTask task) {
    for (SigningDocument document : repository.documents(task.getId())) {
      if (document.getId().equals(task.getActiveDocumentId())) {
        return document;
      }
    }
    throw new SigningException("待签文件不存在");
  }

  /** {@inheritDoc} */
  public SigningTask start(String id, long version, String actor) {
    return start(id, version, false, null, actor);
  }

  /** {@inheritDoc} */
  public SigningTask start(String id, long version, boolean autoSign, String sealId, String actor) {
    SigningTask initial = task(id);
    states.version(initial, version);
    states.editable(initial);
    verifyEnvironment(initial);
    BusinessSnapshot latest = snapshot(initial.getBusinessType(), initial.getBusinessId());
    if (!hash(json.write(latest.getValues())).equals(initial.getSnapshotHash())) {
      throw new SigningException("订单内容已变化，请重新准备签署任务");
    }
    String selected = sealId == null ? "" : sealId.trim();
    if (autoSign && selected.isEmpty()) {
      throw new SigningException("自动落章必须明确选择我方印章");
    }
    final SigningSeal seal = selected.isEmpty() ? null : seals.requireSelectable(selected);
    return transactions.execute(
        () -> {
          SigningTask task = task(id);
          states.version(task, version);
          states.editable(task);
          SigningDocument document = document(task);
          if (task.getDocumentStatus() != DocumentStatus.READY
              || !Objects.equals(document.getSha256(), task.getPositionDocumentHash())) {
            throw new SigningException("最终文件或落章位置尚未确认");
          }
          List<SigningParty> parties = repository.parties(id);
          for (SigningParty party : parties) {
            if (PartyRole.OUR.name().equals(party.getRole())) {
              if (seal != null
                  && party.getOrganizationId() != null
                  && !party.getOrganizationId().isEmpty()
                  && !party.getOrganizationId().equals(seal.getOrganizationId())) {
                throw new SigningException("所选印章与我方签署机构不一致");
              }
              party.setAutoSign(autoSign);
              party.setSealId(seal == null ? null : seal.getId());
              party.setSealName(seal == null ? null : seal.getName());
              party.setSealType(seal == null ? null : seal.getType());
            } else if (party.isAutoSign() || party.getSealId() != null) {
              throw new SigningException("不能替对方指定自动落章或机构印章");
            }
          }
          validateParties(parties, true);
          repository.saveParties(id, parties);
          task.setOperationStatus(SigningOperationStatus.QUEUED);
          task.setLastError(null);
          repository.enqueue(id, SigningJobType.CREATE_FLOW);
          save(task, version, actor);
          event(task, "START_REQUESTED", actor);
          return task;
        });
  }

  /** 后台创建流程前重新读取选章，平台状态变化时不得换用其他印章。 */
  public void verifySeals(SigningTask task) {
    for (SigningParty party : repository.parties(task.getId())) {
      if (PartyRole.OUR.name().equals(party.getRole())) {
        if (party.getSealId() != null && !party.getSealId().trim().isEmpty()) {
          SigningSeal seal = seals.requireSelectable(party.getSealId());
          if (party.getOrganizationId() != null
              && !party.getOrganizationId().isEmpty()
              && !party.getOrganizationId().equals(seal.getOrganizationId())) {
            throw new SigningException("所选印章与我方签署机构不一致");
          }
        } else if (party.isAutoSign()) {
          throw new SigningException("自动落章必须明确选择我方印章");
        }
      } else if (party.isAutoSign() || party.getSealId() != null) {
        throw new SigningException("不能替对方指定自动落章或机构印章");
      }
    }
  }

  /** {@inheritDoc} */
  public SigningTask action(String id, long version, String action, String reason, String actor) {
    return transactions.execute(
        () -> {
          SigningTask task = task(id);
          states.version(task, version);
          if ("cancel".equals(action)) {
            states.editable(task);
            task.setStatus(SigningStatus.REVOKED);
            task.setOperationStatus(SigningOperationStatus.IDLE);
            task.setLastError(null);
            save(task, version, actor);
            event(task, "LOCAL_DRAFT_CANCELLED", actor);
            return task;
          }
          verifyEnvironment(task);
          if (task.getFlowId() == null) {
            throw new SigningException("尚未取得平台流程 ID；结果不明时请先人工核对");
          }
          if (task.getOperationStatus() == SigningOperationStatus.QUEUED
              || task.getOperationStatus() == SigningOperationStatus.RUNNING) {
            throw new SigningException("已有操作正在处理中");
          }
          SigningJobType type;
          if ("revoke".equals(action)) {
            if (task.getStatus() != SigningStatus.SIGNING) {
              throw new SigningException("仅签署中流程可撤销");
            }
            task.setRevokeReason(required(reason, 500, "撤销原因"));
            type = SigningJobType.REVOKE;
          } else if ("sync".equals(action)) {
            type = SigningJobType.SYNC;
          } else {
            throw new SigningException("不支持的签署操作");
          }
          task.setOperationStatus(SigningOperationStatus.QUEUED);
          if (type == SigningJobType.SYNC) {
            repository.enqueueNow(id, type);
          } else {
            repository.enqueue(id, type);
          }
          save(task, version, actor);
          event(task, action.toUpperCase(Locale.ROOT), actor);
          return task;
        });
  }

  /** {@inheritDoc} */
  public SigningDetail detail(String id) {
    SigningDetail result = new SigningDetail();
    result.setTask(task(id));
    result.setDocuments(repository.documents(id));
    result.setParties(repository.parties(id));
    result.setEvents(repository.events(id));
    return result;
  }

  /** {@inheritDoc} */
  public List<SigningTask> history(String type, String businessId) {
    return repository.history(type, businessId);
  }
}
