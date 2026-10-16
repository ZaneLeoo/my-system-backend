package com.ruoyi.business.masterdata.item.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;

/** 料品档案ItemQuery。 */
public class ItemQuery extends PageQuery {
  /** 跨编码、名称、规格的包含关键词。 */
  @Size(max = 200)
  private String keyword;

  /** 返回已清理的关键词。 */
  public String getKeyword() {
    return keyword;
  }

  /** 空白关键词按未筛选处理。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }

  /** 料品编码。 */
  @Size(max = 64)
  private String itemCode;

  /** 料品名称。 */
  @Size(max = 200)
  private String itemName;

  /** 规格型号。 */
  @Size(max = 200)
  private String specification;

  /** 分类 ID。 */
  @Positive private Long categoryId;

  /** 库存基本单位 ID。 */
  @Positive private Long baseUnitId;

  /** 默认采购单位 ID。 */
  @Positive private Long purchaseUnitId;

  /** 默认销售单位 ID。 */
  @Positive private Long salesUnitId;

  /** 允许采购。 */
  private Boolean purchaseEnabled;

  /** 启停筛选。 */
  @Pattern(regexp = "[01]")
  private String status;

  /** 排序字段。 */
  @Pattern(regexp = "itemCode|itemName|specification|updateTime")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 返回料品编码。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode == null || itemCode.isBlank() ? null : itemCode.strip();
  }

  /** 返回料品名称。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName == null || itemName.isBlank() ? null : itemName.strip();
  }

  /** 返回规格型号。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格型号。 */
  public void setSpecification(String specification) {
    this.specification =
        specification == null || specification.isBlank() ? null : specification.strip();
  }

  /** 返回分类 ID。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置分类 ID。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回库存基本单位 ID。 */
  public Long getBaseUnitId() {
    return baseUnitId;
  }

  /** 设置库存基本单位 ID。 */
  public void setBaseUnitId(Long baseUnitId) {
    this.baseUnitId = baseUnitId;
  }

  /** 返回默认采购单位 ID。 */
  public Long getPurchaseUnitId() {
    return purchaseUnitId;
  }

  /** 设置默认采购单位 ID。 */
  public void setPurchaseUnitId(Long purchaseUnitId) {
    this.purchaseUnitId = purchaseUnitId;
  }

  /** 返回默认销售单位 ID。 */
  public Long getSalesUnitId() {
    return salesUnitId;
  }

  /** 设置默认销售单位 ID。 */
  public void setSalesUnitId(Long salesUnitId) {
    this.salesUnitId = salesUnitId;
  }

  /** 返回允许采购。 */
  public Boolean getPurchaseEnabled() {
    return purchaseEnabled;
  }

  /** 设置允许采购。 */
  public void setPurchaseEnabled(Boolean purchaseEnabled) {
    this.purchaseEnabled = purchaseEnabled;
  }

  /** 返回启停筛选。 */
  public String getStatus() {
    return status;
  }

  /** 设置启停筛选。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
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
