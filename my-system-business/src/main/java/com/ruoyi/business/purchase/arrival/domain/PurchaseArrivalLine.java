package com.ruoyi.business.purchase.arrival.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** 采购到货明细，稳定行 ID 供后续入库引用 */
@TableName("purchase_arrival_line")
public class PurchaseArrivalLine extends BusinessEntity {
  /** 到货明细 ID。 */
  @TableId(type = IdType.AUTO)
  private Long lineId;

  /** 到货单 ID。 */
  private Long arrivalId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 来源订单号快照。 */
  private String orderNo;

  /** 来源订单行号快照。 */
  private Integer orderLineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 本次到货数量。 */
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;

  /** 返回到货明细 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置到货明细 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回到货单 ID。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置到货单 ID。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回业务行号。 */
  public Integer getLineNo() {
    return lineNo;
  }

  /** 设置业务行号。 */
  public void setLineNo(Integer lineNo) {
    this.lineNo = lineNo;
  }

  /** 返回来源订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置来源订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回来源订单行 ID。 */
  public Long getOrderLineId() {
    return orderLineId;
  }

  /** 设置来源订单行 ID。 */
  public void setOrderLineId(Long orderLineId) {
    this.orderLineId = orderLineId;
  }

  /** 返回来源订单号快照。 */
  public String getOrderNo() {
    return orderNo;
  }

  /** 设置来源订单号快照。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  /** 返回来源订单行号快照。 */
  public Integer getOrderLineNo() {
    return orderLineNo;
  }

  /** 设置来源订单行号快照。 */
  public void setOrderLineNo(Integer orderLineNo) {
    this.orderLineNo = orderLineNo;
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

  /** 返回单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置单位 ID。 */
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

  /** 返回本次到货数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置本次到货数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回来源数量精度。 */
  public Integer getQuantityDecimalPlaces() {
    return quantityDecimalPlaces;
  }

  /** 设置来源数量精度。 */
  public void setQuantityDecimalPlaces(Integer quantityDecimalPlaces) {
    this.quantityDecimalPlaces = quantityDecimalPlaces;
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
