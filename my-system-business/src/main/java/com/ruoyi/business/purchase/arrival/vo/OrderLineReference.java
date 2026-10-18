package com.ruoyi.business.purchase.arrival.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 来源订单行参照，只返回选择到货所需字段 */
public class OrderLineReference {
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

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 来源供应商 ID。 */
  private Long supplierId;

  /** 订单日期。 */
  private LocalDate orderDate;

  /** 预计到货日期。 */
  private LocalDate expectedDate;

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

  /** 返回来源数量精度。 */
  public Integer getQuantityDecimalPlaces() {
    return quantityDecimalPlaces;
  }

  /** 设置来源数量精度。 */
  public void setQuantityDecimalPlaces(Integer quantityDecimalPlaces) {
    this.quantityDecimalPlaces = quantityDecimalPlaces;
  }

  /** 返回来源供应商 ID。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置来源供应商 ID。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回订单日期。 */
  public LocalDate getOrderDate() {
    return orderDate;
  }

  /** 设置订单日期。 */
  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  /** 返回预计到货日期。 */
  public LocalDate getExpectedDate() {
    return expectedDate;
  }

  /** 设置预计到货日期。 */
  public void setExpectedDate(LocalDate expectedDate) {
    this.expectedDate = expectedDate;
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
}
