package com.ruoyi.business.inventory.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** InventoryQuery 的显式数据约定。 */
@Getter
@Setter
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

  /** 设置料品关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null ? null : keyword.strip();
  }

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode == null ? null : warehouseCode.strip();
  }

  /** 设置货位编码。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode == null ? null : locationCode.strip();
  }

  /** 设置入库单号。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo == null ? null : receiptNo.strip();
  }

  /** 设置流水类型。 */
  public void setBusinessType(String businessType) {
    this.businessType = businessType == null ? null : businessType.strip();
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null ? null : sortField.strip();
  }

  /** 设置排序方向。 */
  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection == null ? null : sortDirection.strip();
  }
}
