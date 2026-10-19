package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 订单明细维护参数，不接受金额、快照与内部归属。 */
@Getter
@Setter
public class OrderLineInput extends AmountInput {
  /** 已有明细行 ID，新行不指定。 */
  @Positive private Long lineId;

  /** 料品 ID。 */
  @NotNull @Positive private Long itemId;

  /** 库存单位 ID。 */
  @NotNull @Positive private Long unitId;

  /** 预计到货日期。 */
  private LocalDate expectedDate;

  /** 行备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 设置行备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
