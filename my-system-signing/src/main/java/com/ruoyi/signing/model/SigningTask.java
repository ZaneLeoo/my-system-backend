package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 签署任务，不引用宿主实体或第三方 DTO。 */
@Getter
@Setter
public class SigningTask {
  /** 本次采用的模板映射快照，后续配置变更不追溯修改。 */
  private SigningTemplateBinding templateBindingSnapshot;

  /** 本系统任务 ID。 */
  private String id;

  /** 开发者登记的业务场景。 */
  private String businessType;

  /** 来源业务主键。 */
  private String businessId;

  /** 合同标题。 */
  private String title;

  /** 平台应用及环境标识，防止串用。 */
  private String providerEnvironment;

  /** 平台流程 ID，未创建时为空。 */
  private String flowId;

  /** UPLOAD 或 TEMPLATE。 */
  private String source;

  /** 采用的模板绑定 ID。 */
  private String bindingId;

  /** 首次准备的业务快照，仅包含允许映射的字段。 */
  private Map<String, Object> snapshot = new LinkedHashMap<String, Object>();

  /** 业务内容摘要，不包含执行进度。 */
  private String snapshotHash;

  /** 签署状态。 */
  private com.ruoyi.signing.model.enums.SigningStatus status =
      com.ruoyi.signing.model.enums.SigningStatus.DRAFT;

  /** 外部操作状态。 */
  private com.ruoyi.signing.model.enums.SigningOperationStatus operationStatus =
      com.ruoyi.signing.model.enums.SigningOperationStatus.IDLE;

  /** 待签文件准备状态。 */
  private com.ruoyi.signing.model.enums.DocumentStatus documentStatus =
      com.ruoyi.signing.model.enums.DocumentStatus.EMPTY;

  /** 最终文件在宿主的保存状态，不代表平台归档状态；保留字段名兼容历史数据。 */
  private com.ruoyi.signing.model.enums.ArchiveStatus archiveStatus =
      com.ruoyi.signing.model.enums.ArchiveStatus.NONE;

  /** 当前待签文档 ID。 */
  private String activeDocumentId;

  /** 已确认落章位置对应的文件摘要。 */
  private String positionDocumentHash;

  /** 官方定位会话，换文件或重开时失效。 */
  private String positionSession;

  /** 整项乐观锁版本。 */
  private long version;

  /** 原始发起账号。 */
  private String createBy;

  /** 北京时间创建时间。 */
  private String createTime;

  /** 最近操作账号，可为后台技术账号。 */
  private String updateBy;

  /** 北京时间修改时间。 */
  private String updateTime;

  /** 脱敏错误提示。 */
  private String lastError;

  /** 撤销原因。 */
  private String revokeReason;
}
