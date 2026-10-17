package com.ruoyi.business.purchase.order.vo;

import com.ruoyi.business.purchase.order.domain.*;

/** 当前账号的默认采购归属。 */
public class OrderDefaults {
  /** 数量小数位。 */
  private Integer quantityDecimalPlaces;

  /** 单价小数位。 */
  private Integer priceDecimalPlaces;

  /** 金额小数位。 */
  private Integer amountDecimalPlaces;

  /** 舍入方式。 */
  private String roundingMode;

  /** 采购员 ID。 */
  private Long buyerId;

  /** 采购员名称。 */
  private String buyerName;

  /** 采购部门 ID。 */
  private Long deptId;

  /** 采购部门名称。 */
  private String deptName;

  /** 返回数量小数位。 */
  public Integer getQuantityDecimalPlaces() {
    return quantityDecimalPlaces;
  }

  /** 设置数量小数位。 */
  public void setQuantityDecimalPlaces(Integer quantityDecimalPlaces) {
    this.quantityDecimalPlaces = quantityDecimalPlaces;
  }

  /** 返回单价小数位。 */
  public Integer getPriceDecimalPlaces() {
    return priceDecimalPlaces;
  }

  /** 设置单价小数位。 */
  public void setPriceDecimalPlaces(Integer priceDecimalPlaces) {
    this.priceDecimalPlaces = priceDecimalPlaces;
  }

  /** 返回金额小数位。 */
  public Integer getAmountDecimalPlaces() {
    return amountDecimalPlaces;
  }

  /** 设置金额小数位。 */
  public void setAmountDecimalPlaces(Integer amountDecimalPlaces) {
    this.amountDecimalPlaces = amountDecimalPlaces;
  }

  /** 返回舍入方式。 */
  public String getRoundingMode() {
    return roundingMode;
  }

  /** 设置舍入方式。 */
  public void setRoundingMode(String roundingMode) {
    this.roundingMode = roundingMode;
  }

  /** 返回采购员 ID。 */
  public Long getBuyerId() {
    return buyerId;
  }

  /** 设置采购员 ID。 */
  public void setBuyerId(Long buyerId) {
    this.buyerId = buyerId;
  }

  /** 返回采购员名称。 */
  public String getBuyerName() {
    return buyerName;
  }

  /** 设置采购员名称。 */
  public void setBuyerName(String buyerName) {
    this.buyerName = buyerName;
  }

  /** 返回采购部门 ID。 */
  public Long getDeptId() {
    return deptId;
  }

  /** 设置采购部门 ID。 */
  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  /** 返回采购部门名称。 */
  public String getDeptName() {
    return deptName;
  }

  /** 设置采购部门名称。 */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }
}
