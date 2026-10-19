package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 分页筛选与白名单排序。 */
@Getter
@Setter
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

  /** 设置订单号包含条件。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo == null || orderNo.isBlank() ? null : orderNo.strip();
  }

  /** 设置状态条件。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }
}
