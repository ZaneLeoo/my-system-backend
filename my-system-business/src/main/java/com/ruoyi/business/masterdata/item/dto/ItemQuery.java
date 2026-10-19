package com.ruoyi.business.masterdata.item.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 料品档案ItemQuery。 */
@Getter
@Setter
public class ItemQuery extends PageQuery {
  /** 跨编码、名称、规格的包含关键词。 */
  @Size(max = 200)
  private String keyword;

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

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode == null || itemCode.isBlank() ? null : itemCode.strip();
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName == null || itemName.isBlank() ? null : itemName.strip();
  }

  /** 设置规格型号。 */
  public void setSpecification(String specification) {
    this.specification =
        specification == null || specification.isBlank() ? null : specification.strip();
  }

  /** 设置启停筛选。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }
}
