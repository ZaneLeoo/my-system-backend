package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** ArrivalLineReferenceQuery 的显式数据约定。 */
@Getter
@Setter
public class ArrivalLineReferenceQuery extends PageQuery {
  /** 供应商 ID。 */
  @Positive private Long supplierId;

  /** 仓库 ID。 */
  @Positive private Long warehouseId;

  /** 指定到货单 ID。 */
  @Positive private Long arrivalId;

  /** 到货单或料品关键词。 */
  @Size(max = 200)
  private String keyword;

  /** 排序字段。 */
  @Pattern(regexp = "arrivalNo|itemCode|itemName")
  private String sortField;

  /** 排序方向。 */
  @Pattern(regexp = "asc|desc")
  private String sortDirection;

  /** 设置到货单或料品关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null ? null : keyword.strip();
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
