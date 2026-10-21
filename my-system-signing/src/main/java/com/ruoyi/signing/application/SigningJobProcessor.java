package com.ruoyi.signing.application;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.result.SigningFlowResult;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.spi.*;
import java.util.*;
import java.util.function.Consumer;

/** 可恢复的分阶段执行器，所有第三方调用都在本地事务之外。 */
public final class SigningJobProcessor {
  private final SigningRepository repository;
  private final SigningProvider provider;
  private final SigningUnitOfWork transactions;
  private final SigningFileStore files;
  private final SigningTemplateService templates;
  private final DefaultSigningService service;
  private final SigningStateMachine states = new SigningStateMachine();

  /** 注入可替换的基础设施。 */
  public SigningJobProcessor(
      SigningRepository repository,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      SigningFileStore files,
      SigningTemplateService templates,
      DefaultSigningService service) {
    this.repository = repository;
    this.provider = provider;
    this.transactions = transactions;
    this.files = files;
    this.templates = templates;
    this.service = service;
  }

  /**
   * 领取并处理一个持久化操作；异常只记录脱敏提示。
   *
   * @return 是否处理过操作
   */
  public boolean runOnce() {
    SigningJob job = repository.claim();
    if (job == null) {
      return false;
    }
    try {
      SigningJobType next = process(job);
      transactions.execute(
          () -> {
            if (!repository.owns(job)) {
              throw new SigningException("执行租约已失效", job.getType() == SigningJobType.CREATE_FLOW);
            }
            repository.finish(job, "DONE", null);
            if (next != null) {
              repository.enqueue(job.getTaskId(), next);
            }
            return null;
          });
    } catch (RuntimeException error) {
      boolean unknown =
          error instanceof SigningException && ((SigningException) error).isUncertain();
      boolean waiting = error instanceof WaitingForFile;
      boolean retry =
          !unknown
              && job.getType() != SigningJobType.CREATE_FLOW
              && (waiting ? job.getAttempt() < 30 : job.getAttempt() < 5);
      transactions.execute(
          () -> {
            if (!repository.owns(job)) {
              return null;
            }
            SigningTask task = service.task(job.getTaskId());
            long version = task.getVersion();
            task.setOperationStatus(
                unknown
                    ? SigningOperationStatus.UNKNOWN
                    : retry ? SigningOperationStatus.QUEUED : SigningOperationStatus.FAILED);
            task.setLastError(
                unknown
                    ? "创建结果不确定，请在平台核对并关联流程 ID，禁止重复创建"
                    : waiting ? "文件正在转换，请稍后刷新" : failureHint(error));
            if (!retry && job.getType() == SigningJobType.ARCHIVE) {
              task.setArchiveStatus(ArchiveStatus.FAILED);
            }
            if (!retry && job.getType() == SigningJobType.PREPARE) {
              task.setDocumentStatus(DocumentStatus.FAILED);
            }
            service.save(task, version, "signing:worker");
            repository.finish(
                job, unknown ? "UNKNOWN" : retry ? "PENDING" : "FAILED", task.getLastError());
            return null;
          });
    }
    return true;
  }

  /** 持有执行权时原子写回最新版本，不把网络等待放入事务。 */
  private void apply(SigningJob job, Consumer<SigningTask> update) {
    transactions.execute(
        () -> {
          if (!repository.owns(job)) {
            throw new SigningException("执行租约已失效", job.getType() == SigningJobType.CREATE_FLOW);
          }
          SigningTask task = service.task(job.getTaskId());
          long version = task.getVersion();
          update.accept(task);
          service.save(task, version, "signing:worker");
          return null;
        });
  }

  /** 执行单个阶段；下一阶段由完成事务原子排队。 */
  private SigningJobType process(SigningJob job) {
    SigningTask task = service.task(job.getTaskId());
    service.verifyEnvironment(task);
    switch (job.getType()) {
      case PREPARE:
        prepare(job, task);
        return null;
      case CREATE_FLOW:
        service.verifyBusiness(task);
        if (task.getFlowId() == null) {
          service.verifySeals(task);
          SigningDocument document = service.document(task);
          String flow =
              provider.createFlow(
                  task, document.getProviderFileId(), repository.parties(task.getId()));
          try {
            apply(
                job,
                current -> {
                  current.setFlowId(flow);
                  current.setOperationStatus(SigningOperationStatus.QUEUED);
                });
          } catch (RuntimeException error) {
            throw new SigningException("已创建流程，但保存结果失败，请人工核对", true);
          }
        }
        return SigningJobType.START_FLOW;
      case START_FLOW:
        service.verifyBusiness(task);
        SigningFlowResult before = provider.detail(task.getFlowId());
        if (before.getStatus() == SigningStatus.DRAFT) {
          provider.start(task.getFlowId());
        }
        return sync(job, task);
      case SYNC:
        return sync(job, task);
      case REVOKE:
        SigningFlowResult current = provider.detail(task.getFlowId());
        SigningStatus status = current.getStatus();
        if (status == SigningStatus.SIGNING) {
          provider.revoke(task.getFlowId(), task.getRevokeReason());
        }
        return sync(job, task);
      case ARCHIVE:
        archive(job, task);
        return null;
      default:
        throw new SigningException("未知后台签署操作");
    }
  }

  /** 查询转换结果并下载私有待签文件，失败时保留平台文件 ID。 */
  private void prepare(SigningJob job, SigningTask task) {
    SigningDocument document = null;
    if (task.getActiveDocumentId() != null) {
      document = service.document(task);
    }
    if (document == null || "SOURCE".equals(document.getKind())) {
      String fileId;
      if (document != null) {
        fileId = document.getProviderFileId();
        if (fileId == null) {
          fileId = provider.upload(document.getFileName(), files.read(document.getStorageKey()));
        }
      } else {
        SigningTemplateBinding binding = task.getTemplateBindingSnapshot();
        if (binding == null || !binding.isEnabled()) {
          throw new SigningException("模板已停用");
        }
        fileId =
            provider.fill(binding.getTemplateId(), templates.values(binding, task.getSnapshot()));
      }
      SigningDocument prepared = new SigningDocument();
      prepared.setId(id());
      prepared.setTaskId(task.getId());
      prepared.setKind("PREPARED");
      prepared.setFileName("采购合同.pdf");
      prepared.setProviderFileId(fileId);
      apply(
          job,
          latest -> {
            repository.saveDocument(prepared);
            latest.setActiveDocumentId(prepared.getId());
          });
      document = prepared;
    }
    Map<String, Object> info = provider.file(document.getProviderFileId());
    String state = text(info, "fileStatus");
    if (!"2".equals(state) && !"5".equals(state)) {
      if (Arrays.asList("3", "9", "12").contains(state)) {
        throw new SigningException("平台文件转换失败");
      }
      throw new WaitingForFile();
    }
    byte[] bytes = provider.download(text(info, "fileDownloadUrl"));
    if (bytes.length < 5
        || bytes[0] != '%'
        || bytes[1] != 'P'
        || bytes[2] != 'D'
        || bytes[3] != 'F') {
      throw new SigningException("平台转换结果不是 PDF");
    }
    SigningDocument stored = files.put("采购合同.pdf", bytes);
    SigningDocument prepared = document;
    prepared.setPageCount(
        info.get("fileTotalPageCount") instanceof Number
            ? ((Number) info.get("fileTotalPageCount")).intValue()
            : 0);
    prepared.setStorageKey(stored.getStorageKey());
    prepared.setSha256(stored.getSha256());
    prepared.setSize(stored.getSize());
    apply(
        job,
        latest -> {
          repository.saveDocument(prepared);
          latest.setDocumentStatus(DocumentStatus.READY);
          latest.setOperationStatus(SigningOperationStatus.IDLE);
          latest.setLastError(null);
        });
  }

  /** 以平台详情为准同步，完成后独立归档。 */
  private SigningJobType sync(SigningJob job, SigningTask task) {
    SigningFlowResult detail = provider.detail(task.getFlowId());
    SigningStatus next = detail.getStatus();
    List<SigningParty> parties = repository.parties(task.getId());
    for (SigningParty party : parties) {
      if (detail.getPartyStatuses().containsKey(party.getRole())) {
        party.setStatus(detail.getPartyStatuses().get(party.getRole()));
      }
    }
    apply(
        job,
        latest -> {
          states.transition(latest, next);
          latest.setOperationStatus(SigningOperationStatus.IDLE);
          latest.setLastError(null);
          repository.saveParties(task.getId(), parties);
          if (next == SigningStatus.COMPLETED && latest.getArchiveStatus() != ArchiveStatus.READY) {
            latest.setArchiveStatus(ArchiveStatus.PENDING);
          }
        });
    if (next == SigningStatus.COMPLETED
        && service.task(task.getId()).getArchiveStatus() != ArchiveStatus.READY) {
      return SigningJobType.ARCHIVE;
    }
    return next == SigningStatus.SIGNING
        ? SigningJobType.SYNC
        : next == SigningStatus.DRAFT ? SigningJobType.START_FLOW : null;
  }

  /** 下载正式已签文件后追加归档，不替换原始待签文件。 */
  private void archive(SigningJob job, SigningTask task) {
    for (SigningDocument existing : repository.documents(task.getId())) {
      if ("SIGNED".equals(existing.getKind())) {
        apply(
            job,
            current -> {
              current.setArchiveStatus(ArchiveStatus.READY);
              current.setOperationStatus(SigningOperationStatus.IDLE);
            });
        return;
      }
    }
    SigningDocument original = service.document(task);
    byte[] bytes = provider.signedFile(task.getFlowId(), original.getProviderFileId());
    if (bytes.length < 5 || bytes[0] != 37 || bytes[1] != 80 || bytes[2] != 68 || bytes[3] != 70) {
      throw new SigningException("平台已签文件不是 PDF，拒绝归档");
    }
    SigningDocument archived = files.put("已签合同.pdf", bytes);
    archived.setId(id());
    archived.setTaskId(task.getId());
    archived.setKind("SIGNED");
    archived.setProviderFileId(original.getProviderFileId());
    apply(
        job,
        current -> {
          repository.saveDocument(archived);
          current.setArchiveStatus(ArchiveStatus.READY);
          current.setOperationStatus(SigningOperationStatus.IDLE);
          current.setLastError(null);
        });
  }

  /** 保存受控异常提示以便定位失败原因，不保存第三方原始响应或非签署异常详情。 */
  private static String failureHint(RuntimeException error) {
    if (error instanceof SigningException) {
      String message = error.getMessage();
      if (message != null && !message.trim().isEmpty()) {
        return message.length() <= 500 ? message : message.substring(0, 500);
      }
    }
    return "平台操作未完成，请检查配置或稍后同步状态";
  }

  /** 转换等待与永久失败区分，避免快速重试上传。 */
  private static final class WaitingForFile extends SigningException {
    private WaitingForFile() {
      super("文件正在转换");
    }
  }
}
