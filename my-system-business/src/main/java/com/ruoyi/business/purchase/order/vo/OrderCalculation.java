package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import java.util.List;

/** 逐行金额与整单汇总。 */
public class OrderCalculation {
  /** 按输入顺序的计算行。 */
  private List<LineAmounts> lines;

  /** 未税金额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netAmount;

  /** 税额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal tax;

  /** 价税合计。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossAmount;

  /** 返回按输入顺序的计算行。 */
  public List<LineAmounts> getLines() {
    return lines;
  }

  /** 设置按输入顺序的计算行。 */
  public void setLines(List<LineAmounts> lines) {
    this.lines = lines;
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
