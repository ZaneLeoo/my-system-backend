package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** 入库单接口分页、筛选及排序白名单 */
public class ReceiptQuery extends PageQuery {
  /** 来源采购订单筛选。 */
  @Positive private Long orderId;

  /** 入库单号包含条件。 */
  @Size(max = 64)
  private String receiptNo;

  /** 供应商条件。 */
  @Positive private Long supplierId;

  /** 仓库条件。 */
  @Positive private Long warehouseId;

  /** 来源到货单条件。 */
  @Positive private Long arrivalId;

  /** 状态条件。 */
  @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|VOIDED")
  private String status;

  /** 起始日期。 */
  private LocalDate beginDate;

  /** 截止日期。 */
  private LocalDate endDate;

  /** 排序字段。 */
  @Pattern(regexp = "receiptNo|receiptDate|updateTime")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "desc";

  /** 返回入库单号包含条件。 */
  public String getReceiptNo() {
    return receiptNo;
  }

  /** 设置入库单号包含条件。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo == null || receiptNo.isBlank() ? null : receiptNo.strip();
  }

  /** 返回供应商条件。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商条件。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回仓库条件。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置仓库条件。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回来源到货单条件。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置来源到货单条件。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回状态条件。 */
  public String getStatus() {
    return status;
  }

  /** 设置状态条件。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
  }

  /** 返回起始日期。 */
  public LocalDate getBeginDate() {
    return beginDate;
  }

  /** 设置起始日期。 */
  public void setBeginDate(LocalDate beginDate) {
    this.beginDate = beginDate;
  }

  /** 返回截止日期。 */
  public LocalDate getEndDate() {
    return endDate;
  }

  /** 设置截止日期。 */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /** 返回排序字段。 */
  public String getSortField() {
    return sortField;
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }

  /** 返回排序方向。 */
  public String getSortDirection() {
    return sortDirection;
  }

  /** 设置排序方向。 */
  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection;
  }

  /** 返回来源采购订单筛选。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置来源采购订单筛选。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }
}
