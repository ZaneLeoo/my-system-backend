package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** 订单明细维护参数，不接受金额、快照与内部归属。 */
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

  /** 返回已有明细行 ID，新行不指定。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置已有明细行 ID，新行不指定。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回库存单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置库存单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回预计到货日期。 */
  public LocalDate getExpectedDate() {
    return expectedDate;
  }

  /** 设置预计到货日期。 */
  public void setExpectedDate(LocalDate expectedDate) {
    this.expectedDate = expectedDate;
  }

  /** 返回行备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置行备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
