package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;

/** 可新增到货的订单行分页参照 */
public class OrderLineReferenceQuery extends PageQuery {
  /** 所属供应商。 */
  @NotNull @Positive private Long supplierId;

  /** 指定来源订单。 */
  @Positive private Long orderId;

  /** 订单号、料品编码、名称或规格关键词。 */
  @Size(max = 200)
  private String keyword;

  /** 排序字段。 */
  @Pattern(regexp = "orderNo|itemCode|itemName")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 返回所属供应商。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置所属供应商。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回指定来源订单。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置指定来源订单。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回订单号、料品编码、名称或规格关键词。 */
  public String getKeyword() {
    return keyword;
  }

  /** 设置订单号、料品编码、名称或规格关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
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
