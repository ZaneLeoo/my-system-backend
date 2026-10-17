package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;

/** 状态变更携带版本与业务说明。 */
public class OrderActionRequest {
  /** 当前版本。 */
  @NotNull @PositiveOrZero private Integer version;

  /** 驳回或关闭说明。 */
  @Size(max = 500)
  private String reason = "";

  /** 返回当前版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置当前版本。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回驳回或关闭说明。 */
  public String getReason() {
    return reason;
  }

  /** 设置驳回或关闭说明。 */
  public void setReason(String reason) {
    this.reason = reason == null ? "" : reason.strip();
  }
}
