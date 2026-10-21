package com.ruoyi.signing.api.command;

import lombok.Getter;
import lombok.Setter;

/** 创建业务签署草稿，不接受客户端业务快照。 */
@Getter
@Setter
public class CreateSigningTaskCommand {
  /** 登记业务类型。 */
  private String businessType;

  /** 业务主键。 */
  private String businessId;

  /** UPLOAD 或 TEMPLATE。 */
  private String source;

  /** 本地模板绑定 ID。 */
  private String bindingId;
}
