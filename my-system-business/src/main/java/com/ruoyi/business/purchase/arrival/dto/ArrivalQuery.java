package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** 到货单接口分页、筛选及排序白名单 */
public class ArrivalQuery extends PageQuery {
  /** 到货单号包含条件。 */
  @Size(max = 64)
  private String arrivalNo;

  /** 供应商条件。 */
  @Positive private Long supplierId;

  /** 仓库条件。 */
  @Positive private Long warehouseId;

  /** 来源订单条件。 */
  @Positive private Long orderId;

  /** 状态条件。 */
  @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|VOIDED")
  private String status;

  /** 起始日期。 */
  private LocalDate beginDate;

  /** 截止日期。 */
  private LocalDate endDate;

  /** 排序字段。 */
  @Pattern(regexp = "arrivalNo|arrivalDate|updateTime")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "desc";

  /** 返回到货单号包含条件。 */
  public String getArrivalNo() {
    return arrivalNo;
  }

  /** 设置到货单号包含条件。 */
  public void setArrivalNo(String arrivalNo) {
    this.arrivalNo = arrivalNo == null || arrivalNo.isBlank() ? null : arrivalNo.strip();
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

  /** 返回来源订单条件。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置来源订单条件。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
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
}
