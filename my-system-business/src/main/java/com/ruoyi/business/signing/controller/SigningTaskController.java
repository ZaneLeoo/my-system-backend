package com.ruoyi.business.signing.controller;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.business.signing.config.SigningProperties;
import com.ruoyi.business.signing.dto.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.command.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.application.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.spi.*;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 签署业务接口，每个数据入口同时检查来源单据权限。 */
@RestController
@RequestMapping("/signing/tasks")
public class SigningTaskController {
  private final DefaultSigningService service;
  private final SigningRepository repository;
  private final SigningProvider provider;
  private final SigningFileStore files;
  private final SigningUnitOfWork transactions;
  private final SigningAccess access;
  private final SigningProperties config;

  /** 装配宿主边界。 */
  public SigningTaskController(
      DefaultSigningService service,
      SigningRepository repository,
      SigningProvider provider,
      SigningFileStore files,
      SigningUnitOfWork transactions,
      SigningAccess access,
      SigningProperties config) {
    this.service = service;
    this.repository = repository;
    this.provider = provider;
    this.files = files;
    this.transactions = transactions;
    this.access = access;
    this.config = config;
  }

  /** 校验当前任务来源业务。 */
  private SigningTask authorized(String id) {
    SigningTask task = service.task(id);
    access.business(task.getBusinessType(), task.getBusinessId());
    return task;
  }

  /** 当前服务端身份。 */
  private String actor() {
    return SecurityUtils.getUsername();
  }

  /**
   * 获取不含密钥的状态及当前我方经办信息。
   *
   * @return 集成状态、主体和当前用户
   */
  @GetMapping("/settings")
  @PreAuthorize("@ss.hasAnyPermi('signing:task:query,signing:task:start')")
  public R<Map<String, Object>> settings() {
    return R.ok(
        map(
            "enabled",
            config.isEnabled(),
            "configured",
            !config.getAppId().isEmpty() && !config.getAppSecret().isEmpty(),
            "environment",
            config.getEnvironment(),
            "organizationName",
            config.getOrganizationName(),
            "currentUserId",
            SecurityUtils.getUserId(),
            "currentUserName",
            SecurityUtils.getLoginUser().getUser().getNickName(),
            "currentUserAccount",
            SecurityUtils.getLoginUser().getUser().getPhonenumber()));
  }

  /**
   * 查询来源单据历史。
   *
   * @param businessType 登记场景
   * @param businessId 来源主键
   * @return 最新任务在前，最多一百条
   */
  @GetMapping
  @PreAuthorize("@ss.hasPermi('signing:task:query')")
  public R<List<SigningTask>> history(
      @RequestParam String businessType, @RequestParam String businessId) {
    access.business(businessType, businessId);
    return R.ok(service.history(businessType, businessId));
  }

  /**
   * 检查来源单据能否新建签署草稿，避免用户填写完文件后才发现已有有效任务。
   *
   * @param businessType 已登记业务类型
   * @param businessId 来源单据主键
   * @return 可新建标记；被占用时包含现有任务 ID 和状态
   */
  @GetMapping("/availability")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningAvailabilityView> availability(
      @RequestParam String businessType, @RequestParam String businessId) {
    access.business(businessType, businessId);
    SigningAvailabilityView view = new SigningAvailabilityView();
    SigningTask activeTask = repository.findActiveTask(businessType, businessId);
    view.setAvailable(activeTask == null);
    if (activeTask != null) {
      view.setTaskId(activeTask.getId());
      view.setStatus(activeTask.getStatus());
    }
    return R.ok(view);
  }

  /**
   * 查询详情，隐藏存储键和原始回调。
   *
   * @param id 本地任务 ID
   * @return 任务、签署方、文档视图和时间线
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('signing:task:query')")
  public R<Map<String, Object>> detail(@PathVariable String id) {
    authorized(id);
    SigningDetail detail = service.detail(id);
    List<Object> documents = new ArrayList<Object>();
    for (SigningDocument document : detail.getDocuments()) {
      documents.add(
          map(
              "id",
              document.getId(),
              "kind",
              document.getKind(),
              "fileName",
              document.getFileName(),
              "sha256",
              document.getSha256(),
              "size",
              document.getSize()));
    }
    return R.ok(
        map(
            "task",
            detail.getTask(),
            "parties",
            detail.getParties(),
            "defaultCounterparty",
            service.defaultCounterparty(detail.getTask()),
            "documents",
            documents,
            "events",
            detail.getEvents()));
  }

  /**
   * 创建唯一有效签署草稿。
   *
   * @param command 业务来源及文件方式
   * @return 新任务
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  @Log(
      title = "电子签署",
      businessType = BusinessType.INSERT,
      isSaveRequestData = false,
      isSaveResponseData = false)
  public R<SigningTask> create(@RequestBody CreateSigningTaskCommand command) {
    if (!config.isEnabled() || config.getAppId().isEmpty() || config.getAppSecret().isEmpty()) {
      throw new SigningException("请先配置并启用电子签署");
    }
    access.business(command.getBusinessType(), command.getBusinessId());
    return R.ok(service.create(command, actor()));
  }

  /**
   * 上传完整文件并异步准备 PDF。
   *
   * @param id 任务
   * @param version 当前版本
   * @param file 合同原件，不超过 10MB
   * @return 已排队任务
   * @throws IOException 上传内容无法读取时
   */
  @PostMapping("/{id}/file")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningTask> upload(
      @PathVariable String id, @RequestParam long version, @RequestParam MultipartFile file)
      throws IOException {
    authorized(id);
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new SigningException("上传文件不能超过 10MB");
    }
    return R.ok(service.prepare(id, version, file.getOriginalFilename(), file.getBytes(), actor()));
  }

  /**
   * 根据绑定生成合同文件。
   *
   * @param id 任务
   * @param request 当前版本
   * @return 已排队任务
   */
  @PostMapping("/{id}/prepare")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningTask> prepare(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    SigningTask task = authorized(id);
    if (!"TEMPLATE".equals(task.getSource())) {
      throw new SigningException("上传方式请先选择文件");
    }
    return R.ok(service.prepare(id, request.getVersion(), null, null, actor()));
  }

  /**
   * 保存双方授权信息，我方身份由服务端绑定当前用户。
   *
   * @param id 任务
   * @param request 双方信息及是否确认位置
   * @return 最新任务
   */
  @PutMapping("/{id}/participants")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningTask> parties(
      @PathVariable String id, @Valid @RequestBody SigningParticipantsRequest request) {
    SigningTask task = authorized(id);
    for (SigningParty party : request.getParties()) {
      if (PartyRole.OUR.name().equals(party.getRole())) {
        party.setPartyType(SigningPartyType.ORGANIZATION);
        party.setOrganizationName(config.getOrganizationName());
        party.setOrganizationId(config.getOrganizationId());
        party.setLocalUserId(SecurityUtils.getUserId());
        party.setTransactorName(SecurityUtils.getLoginUser().getUser().getNickName());
        party.setTransactorAccount(SecurityUtils.getLoginUser().getUser().getPhonenumber());
      } else if (PartyRole.COUNTERPARTY.name().equals(party.getRole())) {
        party.setOrganizationId(null);
        party.setLocalUserId(null);
        party.setSealId(null);
        if (party.getPartyType() == null) {
          party.setPartyType(SigningPartyType.ORGANIZATION);
        }
        SigningParty defaults = service.defaultCounterparty(task);
        if (party.getPartyType() == SigningPartyType.ORGANIZATION) {
          if (defaults == null) {
            throw new SigningException("当前业务未提供对方机构信息");
          }
          party.setOrganizationName(defaults.getOrganizationName());
        } else {
          party.setOrganizationName(null);
        }
      }
    }
    return R.ok(
        service.participants(
            id, request.getVersion(), request.getParties(), request.isConfirmPositions(), actor()));
  }

  /**
   * 获取最终文件的官方拖章链接；回调只接受本次会话。
   *
   * @param id 任务
   * @param request 当前版本
   * @return 新版本与官方链接
   */
  @PostMapping("/{id}/position-url")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<Map<String, Object>> position(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    SigningTask task = authorized(id);
    new SigningStateMachine().version(task, request.getVersion());
    new SigningStateMachine().editable(task);
    if (task.getDocumentStatus() != DocumentStatus.READY || repository.parties(id).size() != 2) {
      throw new SigningException("请先准备文件并保存双方信息");
    }
    service.verifyEnvironment(task);
    String session = SigningSupport.id();
    String url = provider.positionUrl(service.document(task).getProviderFileId(), session);
    SigningTask updated =
        transactions.execute(
            () -> {
              SigningTask latest = authorized(id);
              new SigningStateMachine().version(latest, request.getVersion());
              new SigningStateMachine().editable(latest);
              latest.setPositionSession(session);
              latest.setPositionDocumentHash(null);
              service.save(latest, request.getVersion(), actor());
              return latest;
            });
    return R.ok(map("url", url, "task", updated));
  }

  /**
   * 提交经过位置确认的签署流程。
   *
   * @param id 任务
   * @param request 当前版本和明确选择的我方签署方式、印章编号
   * @return 已排队任务
   */
  @PostMapping("/{id}/start")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  @Log(
      title = "发起电子签署",
      businessType = BusinessType.UPDATE,
      isSaveRequestData = false,
      isSaveResponseData = false)
  public R<SigningTask> start(
      @PathVariable String id, @Valid @RequestBody SigningStartRequest request) {
    authorized(id);
    return R.ok(
        service.start(
            id, request.getVersion(), request.getAutoSign(), request.getSealId(), actor()));
  }

  /**
   * 请求权威状态同步。
   *
   * @param id 任务
   * @param request 当前版本
   * @return 已排队任务
   */
  @PostMapping("/{id}/sync")
  @PreAuthorize("@ss.hasPermi('signing:task:query')")
  public R<SigningTask> sync(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    authorized(id);
    return R.ok(service.action(id, request.getVersion(), "sync", null, actor()));
  }

  /**
   * 撤销尚未完成的流程。
   *
   * @param id 任务
   * @param request 当前版本及撤销原因
   * @return 已排队任务
   */
  @PostMapping("/{id}/revoke")
  @PreAuthorize("@ss.hasPermi('signing:task:revoke')")
  @Log(
      title = "撤销电子签署",
      businessType = BusinessType.UPDATE,
      isSaveRequestData = false,
      isSaveResponseData = false)
  public R<SigningTask> revoke(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    authorized(id);
    return R.ok(service.action(id, request.getVersion(), "revoke", request.getReason(), actor()));
  }

  /**
   * 鉴权下载或预览私有文件，不向前端暴露文件路径。
   *
   * @param id 任务
   * @param documentId 已保存文档 ID
   * @param inline 是否在浏览器预览
   * @return 原文件内容
   */
  @GetMapping("/{id}/documents/{documentId}")
  @PreAuthorize("@ss.hasPermi('signing:task:download')")
  public ResponseEntity<byte[]> download(
      @PathVariable String id,
      @PathVariable String documentId,
      @RequestParam(defaultValue = "false") boolean inline) {
    authorized(id);
    for (SigningDocument document : repository.documents(id)) {
      if (document.getId().equals(documentId) && document.getStorageKey() != null) {
        ContentDisposition disposition =
            (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(document.getFileName(), java.nio.charset.StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .header("X-Content-Type-Options", "nosniff")
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(
                document.getFileName().toLowerCase(Locale.ROOT).endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType.APPLICATION_OCTET_STREAM)
            .body(files.read(document.getStorageKey()));
      }
    }
    throw new SigningException("文档不存在或尚未准备完成");
  }

  /**
   * 人工核对结果不明的创建任务；必须验证平台文件归属。
   *
   * @param id 本地任务
   * @param request 当前版本及已在平台核实的流程 ID
   * @return 已关联且排队同步的任务
   */
  @PostMapping("/{id}/reconcile")
  @PreAuthorize("@ss.hasPermi('signing:task:reconcile')")
  @Log(
      title = "核对电子签署",
      businessType = BusinessType.UPDATE,
      isSaveRequestData = false,
      isSaveResponseData = false)
  public R<SigningTask> reconcile(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    SigningTask initial = authorized(id);
    new SigningStateMachine().version(initial, request.getVersion());
    if (initial.getOperationStatus() != SigningOperationStatus.UNKNOWN
        || initial.getFlowId() != null) {
      throw new SigningException("仅允许核对未知创建结果");
    }
    String flow = required(request.getFlowId(), 100, "平台流程 ID");
    service.verifyEnvironment(initial);
    SigningFlowResult external = provider.detail(flow);
    String fileId = service.document(initial).getProviderFileId();
    if (!external.getFileIds().contains(fileId)) {
      throw new SigningException("该流程未包含本任务最终文件，拒绝关联");
    }
    return R.ok(
        transactions.execute(
            () -> {
              SigningTask task = authorized(id);
              new SigningStateMachine().version(task, request.getVersion());
              if (task.getOperationStatus() != SigningOperationStatus.UNKNOWN
                  || task.getFlowId() != null) {
                throw new SigningException("任务状态已变化");
              }
              task.setFlowId(flow);
              task.setOperationStatus(SigningOperationStatus.QUEUED);
              task.setLastError(null);
              service.save(task, request.getVersion(), actor());
              repository.enqueueNow(id, SigningJobType.SYNC);
              return task;
            }));
  }

  /**
   * 取消尚未创建平台流程的草稿，不调用平台撤销。
   *
   * @param id 任务
   * @param request 当前版本
   * @return 保留历史的本地取消结果
   */
  @PostMapping("/{id}/cancel")
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningTask> cancel(
      @PathVariable String id, @Valid @RequestBody SigningActionRequest request) {
    authorized(id);
    return R.ok(service.action(id, request.getVersion(), "cancel", null, actor()));
  }

  /**
   * 将公共业务错误包装为 R，不暴露原始平台响应。
   *
   * @param error 已脱敏错误
   * @return 业务失败
   */
  @ExceptionHandler(SigningException.class)
  public R<Void> signingError(SigningException error) {
    return R.fail(error.getMessage());
  }
}
