package com.ruoyi.business.purchase.receipt.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** ArrivalLineReference 的显式数据约定。 */
public class ArrivalLineReference {
  /** 到货单 ID。 */
  private Long arrivalId;

  /** 到货行 ID。 */
  private Long arrivalLineId;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 仓库 ID。 */
  private Long warehouseId;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 料品 ID。 */
  private Long itemId;

  /** 单位 ID。 */
  private Long unitId;

  /** 到货单号。 */
  private String arrivalNo;

  /** 供应商编码。 */
  private String supplierCode;

  /** 供应商名称。 */
  private String supplierName;

  /** 仓库编码。 */
  private String warehouseCode;

  /** 仓库名称。 */
  private String warehouseName;

  /** 订单号。 */
  private String orderNo;

  /** 料品编码。 */
  private String itemCode;

  /** 料品名称。 */
  private String itemName;

  /** 规格。 */
  private String specification;

  /** 单位名称。 */
  private String unitName;

  /** 是否使用货位。 */
  private Boolean locationEnabled;

  /** 到货行号。 */
  private Integer arrivalLineNo;

  /** 订单行号。 */
  private Integer orderLineNo;

  /** 数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 到货日期。 */
  private LocalDate arrivalDate;

  /** 到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivalQuantity;

  /** 待审核入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingReceiptQuantity;

  /** 已入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal receivedQuantity;

  /** 可入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;

  /** 返回到货单 ID。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置到货单 ID。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回到货行 ID。 */
  public Long getArrivalLineId() {
    return arrivalLineId;
  }

  /** 设置到货行 ID。 */
  public void setArrivalLineId(Long arrivalLineId) {
    this.arrivalLineId = arrivalLineId;
  }

  /** 返回供应商 ID。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商 ID。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
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

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回到货单号。 */
  public String getArrivalNo() {
    return arrivalNo;
  }

  /** 设置到货单号。 */
  public void setArrivalNo(String arrivalNo) {
    this.arrivalNo = arrivalNo;
  }

  /** 返回供应商编码。 */
  public String getSupplierCode() {
    return supplierCode;
  }

  /** 设置供应商编码。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode;
  }

  /** 返回供应商名称。 */
  public String getSupplierName() {
    return supplierName;
  }

  /** 设置供应商名称。 */
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  /** 返回仓库编码。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode;
  }

  /** 返回仓库名称。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
  }

  /** 返回订单号。 */
  public String getOrderNo() {
    return orderNo;
  }

  /** 设置订单号。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  /** 返回料品编码。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /** 返回料品名称。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回规格。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格。 */
  public void setSpecification(String specification) {
    this.specification = specification;
  }

  /** 返回单位名称。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位名称。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /** 返回是否使用货位。 */
  public Boolean getLocationEnabled() {
    return locationEnabled;
  }

  /** 设置是否使用货位。 */
  public void setLocationEnabled(Boolean locationEnabled) {
    this.locationEnabled = locationEnabled;
  }

  /** 返回到货行号。 */
  public Integer getArrivalLineNo() {
    return arrivalLineNo;
  }

  /** 设置到货行号。 */
  public void setArrivalLineNo(Integer arrivalLineNo) {
    this.arrivalLineNo = arrivalLineNo;
  }

  /** 返回订单行号。 */
  public Integer getOrderLineNo() {
    return orderLineNo;
  }

  /** 设置订单行号。 */
  public void setOrderLineNo(Integer orderLineNo) {
    this.orderLineNo = orderLineNo;
  }

  /** 返回数量精度。 */
  public Integer getQuantityDecimalPlaces() {
    return quantityDecimalPlaces;
  }

  /** 设置数量精度。 */
  public void setQuantityDecimalPlaces(Integer quantityDecimalPlaces) {
    this.quantityDecimalPlaces = quantityDecimalPlaces;
  }

  /** 返回到货日期。 */
  public LocalDate getArrivalDate() {
    return arrivalDate;
  }

  /** 设置到货日期。 */
  public void setArrivalDate(LocalDate arrivalDate) {
    this.arrivalDate = arrivalDate;
  }

  /** 返回到货数量。 */
  public BigDecimal getArrivalQuantity() {
    return arrivalQuantity;
  }

  /** 设置到货数量。 */
  public void setArrivalQuantity(BigDecimal arrivalQuantity) {
    this.arrivalQuantity = arrivalQuantity;
  }

  /** 返回待审核入库数量。 */
  public BigDecimal getPendingReceiptQuantity() {
    return pendingReceiptQuantity;
  }

  /** 设置待审核入库数量。 */
  public void setPendingReceiptQuantity(BigDecimal pendingReceiptQuantity) {
    this.pendingReceiptQuantity = pendingReceiptQuantity;
  }

  /** 返回已入库数量。 */
  public BigDecimal getReceivedQuantity() {
    return receivedQuantity;
  }

  /** 设置已入库数量。 */
  public void setReceivedQuantity(BigDecimal receivedQuantity) {
    this.receivedQuantity = receivedQuantity;
  }

  /** 返回可入库数量。 */
  public BigDecimal getAvailableQuantity() {
    return availableQuantity;
  }

  /** 设置可入库数量。 */
  public void setAvailableQuantity(BigDecimal availableQuantity) {
    this.availableQuantity = availableQuantity;
  }
}
