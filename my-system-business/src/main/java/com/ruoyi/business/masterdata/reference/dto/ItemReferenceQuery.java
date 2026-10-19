package com.ruoyi.business.masterdata.reference.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 参照查询仅暴露可控的只读筛选，不接受启停或删除状态。 */
@Getter
@Setter
public class ItemReferenceQuery extends PageQuery {
  /** 编码、名称或规格的搜索关键字，空白按当前查询规则处理。 */
  @Size(max = 200)
  private String keyword;

  /** 分类筛选 ID，范围包含选中分类及其后代。 */
  @Positive private Long categoryId;

  /** 是否只查询允许采购的料品。 */
  @NotNull private Boolean purchaseOnly = false;

  /** 已开放的稳定排序字段标识，不接受 SQL 表达式。 */
  @Pattern(regexp = "itemCode|itemName|specification")
  private String sortField;

  /** 排序方向，取值为 asc 或 desc。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 设置 keyword。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }

  /** 设置 sortField。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }
}
