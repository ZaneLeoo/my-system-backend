package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** 分页筛选与白名单排序。 */
public class OrderQuery extends PageQuery {
  /** 订单号包含条件。 */
  @Size(max = 64)
  private String orderNo;

  /** 供应商条件。 */
  @Positive private Long supplierId;

  /** 状态条件。 */
  @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|CLOSED")
  private String status;

  /** 起始订单日期。 */
  private LocalDate beginDate;

  /** 截止订单日期。 */
  private LocalDate endDate;

  /** 排序字段。 */
  @Pattern(regexp = "orderNo|orderDate|updateTime|grossAmount")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "desc";

  /** 返回订单号包含条件。 */
  public String getOrderNo() {
    return orderNo;
  }

  /** 设置订单号包含条件。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo == null || orderNo.isBlank() ? null : orderNo.strip();
  }

  /** 返回供应商条件。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商条件。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回状态条件。 */
  public String getStatus() {
    return status;
  }

  /** 设置状态条件。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
  }

  /** 返回起始订单日期。 */
  public LocalDate getBeginDate() {
    return beginDate;
  }

  /** 设置起始订单日期。 */
  public void setBeginDate(LocalDate beginDate) {
    this.beginDate = beginDate;
  }

  /** 返回截止订单日期。 */
  public LocalDate getEndDate() {
    return endDate;
  }

  /** 设置截止订单日期。 */
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
