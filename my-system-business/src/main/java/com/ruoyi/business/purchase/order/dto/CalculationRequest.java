package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.business.purchase.order.domain.PriceTaxMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** 只读金额试算请求，不修改订单。 */
public class CalculationRequest {
  /** 已有订单 ID。 */
  @Positive private Long orderId;

  /** 计价方式。 */
  @NotNull private PriceTaxMode priceTaxMode;

  /** 计算行。 */
  @NotEmpty
  @Size(max = 200)
  private List<@NotNull @Valid AmountInput> lines;

  /** 返回已有订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置已有订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回计价方式。 */
  public PriceTaxMode getPriceTaxMode() {
    return priceTaxMode;
  }

  /** 设置计价方式。 */
  public void setPriceTaxMode(PriceTaxMode priceTaxMode) {
    this.priceTaxMode = priceTaxMode;
  }

  /** 返回计算行。 */
  public List<@NotNull @Valid AmountInput> getLines() {
    return lines;
  }

  /** 设置计算行。 */
  public void setLines(List<@NotNull @Valid AmountInput> lines) {
    this.lines = lines;
  }
}
