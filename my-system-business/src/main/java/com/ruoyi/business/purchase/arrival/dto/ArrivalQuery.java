package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 到货单接口分页、筛选及排序白名单 */
@Getter
@Setter
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

  /** 设置到货单号包含条件。 */
  public void setArrivalNo(String arrivalNo) {
    this.arrivalNo = arrivalNo == null || arrivalNo.isBlank() ? null : arrivalNo.strip();
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
