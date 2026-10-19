package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 入库单接口分页、筛选及排序白名单 */
@Getter
@Setter
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

  /** 设置入库单号包含条件。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo == null || receiptNo.isBlank() ? null : receiptNo.strip();
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
