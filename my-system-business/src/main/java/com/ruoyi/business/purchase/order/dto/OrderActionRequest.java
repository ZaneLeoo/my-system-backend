package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 状态变更携带版本与业务说明。 */
@Getter
@Setter
public class OrderActionRequest {
  /** 当前版本。 */
  @NotNull @PositiveOrZero private Integer version;

  /** 驳回或关闭说明。 */
  @Size(max = 500)
  private String reason = "";

  /** 设置驳回或关闭说明。 */
  public void setReason(String reason) {
    this.reason = reason == null ? "" : reason.strip();
  }
}
