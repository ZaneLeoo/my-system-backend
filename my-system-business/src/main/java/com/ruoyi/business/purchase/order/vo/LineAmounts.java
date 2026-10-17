package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;

/** 行金额计算结果，换算单价不参与金额反算。 */
public class LineAmounts {
  /** 未税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netPrice;

  /** 含税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossPrice;

  /** 未税金额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netAmount;

  /** 税额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal tax;

  /** 价税合计。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossAmount;

  /** 返回未税单价。 */
  public BigDecimal getNetPrice() {
    return netPrice;
  }

  /** 设置未税单价。 */
  public void setNetPrice(BigDecimal netPrice) {
    this.netPrice = netPrice;
  }

  /** 返回含税单价。 */
  public BigDecimal getGrossPrice() {
    return grossPrice;
  }

  /** 设置含税单价。 */
  public void setGrossPrice(BigDecimal grossPrice) {
    this.grossPrice = grossPrice;
  }

  /** 返回未税金额。 */
  public BigDecimal getNetAmount() {
    return netAmount;
  }

  /** 设置未税金额。 */
  public void setNetAmount(BigDecimal netAmount) {
    this.netAmount = netAmount;
  }

  /** 返回税额。 */
  public BigDecimal getTax() {
    return tax;
  }

  /** 设置税额。 */
  public void setTax(BigDecimal tax) {
    this.tax = tax;
  }

  /** 返回价税合计。 */
  public BigDecimal getGrossAmount() {
    return grossAmount;
  }

  /** 设置价税合计。 */
  public void setGrossAmount(BigDecimal grossAmount) {
    this.grossAmount = grossAmount;
  }
}
