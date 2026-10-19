package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 可新增到货的订单行分页参照 */
@Getter
@Setter
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

  /** 设置订单号、料品编码、名称或规格关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }
}
