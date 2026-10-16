package com.ruoyi.business.masterdata.reference.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;

/** 参照查询仅暴露可控的只读筛选，不接受启停或删除状态。 */
public class ItemReferenceQuery extends PageQuery {
  @Size(max = 200)
  private String keyword;

  @Positive private Long categoryId;
  @NotNull private Boolean purchaseOnly = false;

  @Pattern(regexp = "itemCode|itemName|specification")
  private String sortField;

  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 返回 keyword。 */
  public String getKeyword() {
    return keyword;
  }

  /** 设置 keyword。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }

  /** 返回 categoryId。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置 categoryId。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回 purchaseOnly。 */
  public Boolean getPurchaseOnly() {
    return purchaseOnly;
  }

  /** 设置 purchaseOnly。 */
  public void setPurchaseOnly(Boolean purchaseOnly) {
    this.purchaseOnly = purchaseOnly;
  }

  /** 返回 sortField。 */
  public String getSortField() {
    return sortField;
  }

  /** 设置 sortField。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }

  /** 返回 sortDirection。 */
  public String getSortDirection() {
    return sortDirection;
  }

  /** 设置 sortDirection。 */
  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection;
  }
}
