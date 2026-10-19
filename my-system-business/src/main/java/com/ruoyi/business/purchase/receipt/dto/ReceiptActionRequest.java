package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 状态命令携带版本及业务说明 */
@Getter
@Setter
public class ReceiptActionRequest {
  /** 当前整单版本。 */
  @NotNull @PositiveOrZero private Integer version;

  /** 驳回或作废原因。 */
  @Size(max = 500)
  private String reason = "";

  /** 设置驳回或作废原因。 */
  public void setReason(String reason) {
    this.reason = reason == null ? "" : reason.strip();
  }
}
