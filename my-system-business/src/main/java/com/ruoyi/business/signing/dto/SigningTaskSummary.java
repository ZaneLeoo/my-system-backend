package com.ruoyi.business.signing.dto;

import com.ruoyi.signing.model.enums.SigningStatus;
import lombok.Getter;
import lombok.Setter;

/** 工作台摘要，不返回业务快照、文件存储键或签署方账号。 */
@Getter
@Setter
public class SigningTaskSummary {
  /** 本地签署任务 ID。 */
  private String id;

  /** 来源业务类型。 */
  private String businessType;

  /** 中文业务名称。 */
  private String businessName;

  /** 来源主键。 */
  private String businessId;

  /** 合同标题。 */
  private String title;

  /** 平台流程状态。 */
  private SigningStatus status;

  /** 发起账号。 */
  private String createBy;

  /** 北京时间创建时间。 */
  private String createTime;

  /** 北京时间修改时间。 */
  private String updateTime;
}
