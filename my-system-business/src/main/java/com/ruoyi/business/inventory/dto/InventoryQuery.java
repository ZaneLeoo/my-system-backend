package com.ruoyi.business.inventory.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

/** InventoryQuery 的显式数据约定。 */
public class InventoryQuery extends PageQuery {
  /** 料品关键词。 */
  @Size(max = 200)
  private String keyword;

  /** 仓库编码。 */
  @Size(max = 64)
  private String warehouseCode;

  /** 货位编码。 */
  @Size(max = 64)
  private String locationCode;

  /** 指定仓库。 */
  @Positive private Long warehouseId;

  /** 指定货位。 */
  @Positive private Long locationId;

  /** 指定料品。 */
  @Positive private Long itemId;

  /** 仅无货位维度。 */
  private Boolean withoutLocation;

  /** 仅有库存。 */
  private Boolean onlyPositive = true;

  /** 入库单号。 */
  @Size(max = 64)
  private String receiptNo;

  /** 流水类型。 */
  @Pattern(regexp = "PURCHASE_RECEIPT|PURCHASE_RECEIPT_VOID")
  private String businessType;

  /** 开始记账时间。 */
  @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime beginTime;

  /** 结束记账时间。 */
  @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endTime;

  /** 排序字段。 */
  @Pattern(regexp = "quantity|postedTime")
  private String sortField;

  /** 排序方向。 */
  @Pattern(regexp = "asc|desc")
  private String sortDirection;

  /** 返回料品关键词。 */
  public String getKeyword() {
    return keyword;
  }

  /** 设置料品关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null ? null : keyword.strip();
  }

  /** 返回仓库编码。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode == null ? null : warehouseCode.strip();
  }

  /** 返回货位编码。 */
  public String getLocationCode() {
    return locationCode;
  }

  /** 设置货位编码。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode == null ? null : locationCode.strip();
  }

  /** 返回指定仓库。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置指定仓库。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回指定货位。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置指定货位。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  /** 返回指定料品。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置指定料品。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回仅无货位维度。 */
  public Boolean getWithoutLocation() {
    return withoutLocation;
  }

  /** 设置仅无货位维度。 */
  public void setWithoutLocation(Boolean withoutLocation) {
    this.withoutLocation = withoutLocation;
  }

  /** 返回仅有库存。 */
  public Boolean getOnlyPositive() {
    return onlyPositive;
  }

  /** 设置仅有库存。 */
  public void setOnlyPositive(Boolean onlyPositive) {
    this.onlyPositive = onlyPositive;
  }

  /** 返回入库单号。 */
  public String getReceiptNo() {
    return receiptNo;
  }

  /** 设置入库单号。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo == null ? null : receiptNo.strip();
  }

  /** 返回流水类型。 */
  public String getBusinessType() {
    return businessType;
  }

  /** 设置流水类型。 */
  public void setBusinessType(String businessType) {
    this.businessType = businessType == null ? null : businessType.strip();
  }

  /** 返回开始记账时间。 */
  public LocalDateTime getBeginTime() {
    return beginTime;
  }

  /** 设置开始记账时间。 */
  public void setBeginTime(LocalDateTime beginTime) {
    this.beginTime = beginTime;
  }

  /** 返回结束记账时间。 */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /** 设置结束记账时间。 */
  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  /** 返回排序字段。 */
  public String getSortField() {
    return sortField;
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null ? null : sortField.strip();
  }

  /** 返回排序方向。 */
  public String getSortDirection() {
    return sortDirection;
  }

  /** 设置排序方向。 */
  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection == null ? null : sortDirection.strip();
  }
}
