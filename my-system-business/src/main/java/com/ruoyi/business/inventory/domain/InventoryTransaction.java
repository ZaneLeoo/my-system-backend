package com.ruoyi.business.inventory.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** InventoryTransaction 的显式数据约定。 */
@TableName("inventory_transaction")
public class InventoryTransaction extends AuditEntity {
  /** 库存流水 ID。 */
  @TableId(type = IdType.AUTO)
  private Long transactionId;

  /** 库存维度 ID。 */
  private Long balanceId;

  /** 业务类型。 */
  private String businessType;

  /** 入库单 ID。 */
  private Long receiptId;

  /** 入库单号。 */
  private String receiptNo;

  /** 入库行 ID。 */
  private Long receiptLineId;

  /** 仓库 ID。 */
  private Long warehouseId;

  /** 货位 ID。 */
  private Long locationId;

  /** 料品 ID。 */
  private Long itemId;

  /** 单位 ID。 */
  private Long unitId;

  /** 仓库编码快照。 */
  private String warehouseCode;

  /** 仓库名称快照。 */
  private String warehouseName;

  /** 货位编码快照。 */
  private String locationCode;

  /** 货位名称快照。 */
  private String locationName;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 库存单位名称快照。 */
  private String unitName;

  /** 数量变化。 */
  private BigDecimal quantityChange;

  /** 变动前数量。 */
  private BigDecimal quantityBefore;

  /** 变动后数量。 */
  private BigDecimal quantityAfter;

  /** 业务日期。 */
  private LocalDate businessDate;

  /** 实际记账时间。 */
  private LocalDateTime postedTime;

  /** 被冲销流水 ID。 */
  private Long reversalOf;

  /** 业务说明。 */
  private String remark;

  /** 返回库存流水 ID。 */
  public Long getTransactionId() {
    return transactionId;
  }

  /** 设置库存流水 ID。 */
  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
  }

  /** 返回库存维度 ID。 */
  public Long getBalanceId() {
    return balanceId;
  }

  /** 设置库存维度 ID。 */
  public void setBalanceId(Long balanceId) {
    this.balanceId = balanceId;
  }

  /** 返回业务类型。 */
  public String getBusinessType() {
    return businessType;
  }

  /** 设置业务类型。 */
  public void setBusinessType(String businessType) {
    this.businessType = businessType;
  }

  /** 返回入库单 ID。 */
  public Long getReceiptId() {
    return receiptId;
  }

  /** 设置入库单 ID。 */
  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  /** 返回入库单号。 */
  public String getReceiptNo() {
    return receiptNo;
  }

  /** 设置入库单号。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo;
  }

  /** 返回入库行 ID。 */
  public Long getReceiptLineId() {
    return receiptLineId;
  }

  /** 设置入库行 ID。 */
  public void setReceiptLineId(Long receiptLineId) {
    this.receiptLineId = receiptLineId;
  }

  /** 返回仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回货位 ID。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置货位 ID。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
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

  /** 返回库存单位名称快照。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置库存单位名称快照。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /** 返回数量变化。 */
  public BigDecimal getQuantityChange() {
    return quantityChange;
  }

  /** 设置数量变化。 */
  public void setQuantityChange(BigDecimal quantityChange) {
    this.quantityChange = quantityChange;
  }

  /** 返回变动前数量。 */
  public BigDecimal getQuantityBefore() {
    return quantityBefore;
  }

  /** 设置变动前数量。 */
  public void setQuantityBefore(BigDecimal quantityBefore) {
    this.quantityBefore = quantityBefore;
  }

  /** 返回变动后数量。 */
  public BigDecimal getQuantityAfter() {
    return quantityAfter;
  }

  /** 设置变动后数量。 */
  public void setQuantityAfter(BigDecimal quantityAfter) {
    this.quantityAfter = quantityAfter;
  }

  /** 返回业务日期。 */
  public LocalDate getBusinessDate() {
    return businessDate;
  }

  /** 设置业务日期。 */
  public void setBusinessDate(LocalDate businessDate) {
    this.businessDate = businessDate;
  }

  /** 返回实际记账时间。 */
  public LocalDateTime getPostedTime() {
    return postedTime;
  }

  /** 设置实际记账时间。 */
  public void setPostedTime(LocalDateTime postedTime) {
    this.postedTime = postedTime;
  }

  /** 返回被冲销流水 ID。 */
  public Long getReversalOf() {
    return reversalOf;
  }

  /** 设置被冲销流水 ID。 */
  public void setReversalOf(Long reversalOf) {
    this.reversalOf = reversalOf;
  }

  /** 返回业务说明。 */
  public String getRemark() {
    return remark;
  }

  /** 设置业务说明。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
