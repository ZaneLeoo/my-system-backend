package com.ruoyi.business.purchase.receipt.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.receipt.domain.PurchaseReceiptLine;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** ReceiptLineView 的显式数据约定。 */
public class ReceiptLineView {
  /** 实际入库仓库 ID。 */
  private Long warehouseId;

  /** 仓库编码快照。 */
  private String warehouseCode;

  /** 仓库名称快照。 */
  private String warehouseName;

  /** 本行货位管理方式快照。 */
  private Boolean locationEnabled;

  /** 入库明细 ID。 */
  private Long lineId;

  /** 入库单 ID。 */
  private Long receiptId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 来源到货单 ID。 */
  private Long arrivalId;

  /** 来源到货明细 ID。 */
  private Long arrivalLineId;

  /** 来源到货单号。 */
  private String arrivalNo;

  /** 来源到货行号。 */
  private Integer arrivalLineNo;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 来源订单号。 */
  private String orderNo;

  /** 来源订单行号。 */
  private Integer orderLineNo;

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

  /** 单位快照。 */
  private String unitName;

  /** 货位 ID。 */
  private Long locationId;

  /** 货位编码快照。 */
  private String locationCode;

  /** 货位名称快照。 */
  private String locationName;

  /** 本次入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;

  /** 来源到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivalQuantity;

  /** 待审核入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingReceiptQuantity;

  /** 已入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal receivedQuantity;

  /** 当前可入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;

  /** 返回入库明细 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置入库明细 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回入库单 ID。 */
  public Long getReceiptId() {
    return receiptId;
  }

  /** 设置入库单 ID。 */
  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  /** 返回业务行号。 */
  public Integer getLineNo() {
    return lineNo;
  }

  /** 设置业务行号。 */
  public void setLineNo(Integer lineNo) {
    this.lineNo = lineNo;
  }

  /** 返回来源到货单 ID。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置来源到货单 ID。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回来源到货明细 ID。 */
  public Long getArrivalLineId() {
    return arrivalLineId;
  }

  /** 设置来源到货明细 ID。 */
  public void setArrivalLineId(Long arrivalLineId) {
    this.arrivalLineId = arrivalLineId;
  }

  /** 返回来源到货单号。 */
  public String getArrivalNo() {
    return arrivalNo;
  }

  /** 设置来源到货单号。 */
  public void setArrivalNo(String arrivalNo) {
    this.arrivalNo = arrivalNo;
  }

  /** 返回来源到货行号。 */
  public Integer getArrivalLineNo() {
    return arrivalLineNo;
  }

  /** 设置来源到货行号。 */
  public void setArrivalLineNo(Integer arrivalLineNo) {
    this.arrivalLineNo = arrivalLineNo;
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

  /** 返回来源订单号。 */
  public String getOrderNo() {
    return orderNo;
  }

  /** 设置来源订单号。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  /** 返回来源订单行号。 */
  public Integer getOrderLineNo() {
    return orderLineNo;
  }

  /** 设置来源订单行号。 */
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

  /** 返回库存单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置库存单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回单位快照。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位快照。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /** 返回货位 ID。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置货位 ID。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  /** 返回货位编码快照。 */
  public String getLocationCode() {
    return locationCode;
  }

  /** 设置货位编码快照。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode;
  }

  /** 返回货位名称快照。 */
  public String getLocationName() {
    return locationName;
  }

  /** 设置货位名称快照。 */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  /** 返回本次入库数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置本次入库数量。 */
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

  /** 返回来源到货数量。 */
  public BigDecimal getArrivalQuantity() {
    return arrivalQuantity;
  }

  /** 设置来源到货数量。 */
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

  /** 返回当前可入库数量。 */
  public BigDecimal getAvailableQuantity() {
    return availableQuantity;
  }

  /** 设置当前可入库数量。 */
  public void setAvailableQuantity(BigDecimal availableQuantity) {
    this.availableQuantity = availableQuantity;
  }

  /** 从已保存明细生成响应快照。 */
  public static ReceiptLineView from(PurchaseReceiptLine row) {
    ReceiptLineView view = new ReceiptLineView();
    view.setLineId(row.getLineId());
    view.setReceiptId(row.getReceiptId());
    view.setLineNo(row.getLineNo());
    view.setArrivalId(row.getArrivalId());
    view.setArrivalLineId(row.getArrivalLineId());
    view.setArrivalNo(row.getArrivalNo());
    view.setArrivalLineNo(row.getArrivalLineNo());
    view.setOrderId(row.getOrderId());
    view.setOrderLineId(row.getOrderLineId());
    view.setOrderNo(row.getOrderNo());
    view.setOrderLineNo(row.getOrderLineNo());
    view.setItemId(row.getItemId());
    view.setItemCode(row.getItemCode());
    view.setItemName(row.getItemName());
    view.setSpecification(row.getSpecification());
    view.setUnitId(row.getUnitId());
    view.setUnitName(row.getUnitName());
    view.setLocationId(row.getLocationId());
    view.setLocationCode(row.getLocationCode());
    view.setLocationName(row.getLocationName());
    view.setQuantity(row.getQuantity());
    view.setQuantityDecimalPlaces(row.getQuantityDecimalPlaces());
    view.setRemark(row.getRemark());
    view.setWarehouseId(row.getWarehouseId());
    view.setWarehouseCode(row.getWarehouseCode());
    view.setWarehouseName(row.getWarehouseName());
    view.setLocationEnabled(row.getLocationEnabled());
    return view;
  }

  /** 返回实际入库仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置实际入库仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回仓库编码快照。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码快照。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode;
  }

  /** 返回仓库名称快照。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称快照。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
  }

  /** 返回本行货位管理方式快照。 */
  public Boolean getLocationEnabled() {
    return locationEnabled;
  }

  /** 设置本行货位管理方式快照。 */
  public void setLocationEnabled(Boolean locationEnabled) {
    this.locationEnabled = locationEnabled;
  }
}
