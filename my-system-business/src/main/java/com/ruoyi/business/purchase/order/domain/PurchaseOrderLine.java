package com.ruoyi.business.purchase.order.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 采购订单持久化实体，业务快照与审计分开维护。 */
@TableName("purchase_order_line")
public class PurchaseOrderLine extends BusinessEntity {
  /** 明细行 ID。 */
  @TableId(type = IdType.AUTO)
  private Long lineId;

  /** 订单 ID。 */
  private Long orderId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 库存单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 数量。 */
  private BigDecimal quantity;

  /** 未税单价。 */
  private BigDecimal netPrice;

  /** 含税单价。 */
  private BigDecimal grossPrice;

  /** 税率百分数。 */
  private BigDecimal taxRate;

  /** 未税金额。 */
  private BigDecimal netAmount;

  /** 税额。 */
  private BigDecimal tax;

  /** 价税合计。 */
  private BigDecimal grossAmount;

  /** 预计到货日期。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDate expectedDate;

  /** 行备注。 */
  private String remark;

  /** 返回明细行 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置明细行 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回业务行号。 */
  public Integer getLineNo() {
    return lineNo;
  }

  /** 设置业务行号。 */
  public void setLineNo(Integer lineNo) {
    this.lineNo = lineNo;
  }

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回料品编码快照。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码快照。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /** 返回料品名称快照。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称快照。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回规格快照。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格快照。 */
  public void setSpecification(String specification) {
    this.specification = specification;
  }

  /** 返回库存单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置库存单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回单位名称快照。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位名称快照。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /** 返回数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

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

  /** 返回税率百分数。 */
  public BigDecimal getTaxRate() {
    return taxRate;
  }

  /** 设置税率百分数。 */
  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
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
    this.remark = remark;
  }
}
