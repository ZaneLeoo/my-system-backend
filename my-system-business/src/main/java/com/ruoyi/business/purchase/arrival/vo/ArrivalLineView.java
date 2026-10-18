package com.ruoyi.business.purchase.arrival.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** 到货行快照及来源订单的当前执行数量 */
public class ArrivalLineView {
  /** 到货明细 ID。 */
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
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;

  /** 订单数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal orderQuantity;

  /** 待审核到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingArrivalQuantity;

  /** 已确认到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivedQuantity;

  /** 可新增到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;

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

  /** 返回订单数量。 */
  public BigDecimal getOrderQuantity() {
    return orderQuantity;
  }

  /** 设置订单数量。 */
  public void setOrderQuantity(BigDecimal orderQuantity) {
    this.orderQuantity = orderQuantity;
  }

  /** 返回待审核到货数量。 */
  public BigDecimal getPendingArrivalQuantity() {
    return pendingArrivalQuantity;
  }

  /** 设置待审核到货数量。 */
  public void setPendingArrivalQuantity(BigDecimal pendingArrivalQuantity) {
    this.pendingArrivalQuantity = pendingArrivalQuantity;
  }

  /** 返回已确认到货数量。 */
  public BigDecimal getArrivedQuantity() {
    return arrivedQuantity;
  }

  /** 设置已确认到货数量。 */
  public void setArrivedQuantity(BigDecimal arrivedQuantity) {
    this.arrivedQuantity = arrivedQuantity;
  }

  /** 返回可新增到货数量。 */
  public BigDecimal getAvailableQuantity() {
    return availableQuantity;
  }

  /** 设置可新增到货数量。 */
  public void setAvailableQuantity(BigDecimal availableQuantity) {
    this.availableQuantity = availableQuantity;
  }

  /** 显式映射业务快照，不返回内部删除标记。 */
  public static ArrivalLineView from(PurchaseArrivalLine entity) {
    ArrivalLineView result = new ArrivalLineView();
    result.setLineId(entity.getLineId());
    result.setArrivalId(entity.getArrivalId());
    result.setLineNo(entity.getLineNo());
    result.setOrderId(entity.getOrderId());
    result.setOrderLineId(entity.getOrderLineId());
    result.setOrderNo(entity.getOrderNo());
    result.setOrderLineNo(entity.getOrderLineNo());
    result.setItemId(entity.getItemId());
    result.setItemCode(entity.getItemCode());
    result.setItemName(entity.getItemName());
    result.setSpecification(entity.getSpecification());
    result.setUnitId(entity.getUnitId());
    result.setUnitName(entity.getUnitName());
    result.setQuantity(entity.getQuantity());
    result.setQuantityDecimalPlaces(entity.getQuantityDecimalPlaces());
    result.setRemark(entity.getRemark());
    return result;
  }
}
