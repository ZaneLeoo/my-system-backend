package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;

/** 状态命令携带版本及业务说明 */
public class ArrivalActionRequest {
  /** 当前整单版本。 */
  @NotNull @PositiveOrZero private Integer version;

  /** 驳回或作废原因。 */
  @Size(max = 500)
  private String reason = "";

  /** 返回当前整单版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置当前整单版本。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回驳回或作废原因。 */
  public String getReason() {
    return reason;
  }

  /** 设置驳回或作废原因。 */
  public void setReason(String reason) {
    this.reason = reason == null ? "" : reason.strip();
  }
}
