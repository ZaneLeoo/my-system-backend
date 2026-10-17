package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** 金额计算输入，单价仅接受当前口径的一种价格。 */
public class AmountInput {
  /** 数量。 */
  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  @Digits(integer = 9, fraction = 6)
  private BigDecimal quantity;

  /** 所选计价口径的原始单价。 */
  @NotNull
  @DecimalMin("0")
  @Digits(integer = 12, fraction = 6)
  private BigDecimal unitPrice;

  /** 税率百分数。 */
  @NotNull
  @DecimalMin("0")
  @DecimalMax("100")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal taxRate;

  /** 返回数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回所选计价口径的原始单价。 */
  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  /** 设置所选计价口径的原始单价。 */
  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  /** 返回税率百分数。 */
  public BigDecimal getTaxRate() {
    return taxRate;
  }

  /** 设置税率百分数。 */
  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }
}
