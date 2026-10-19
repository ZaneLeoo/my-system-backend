package com.ruoyi.business.masterdata.warehouse.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 仓库分页查询。 */
@Getter
@Setter
public class WarehouseQuery extends PageQuery {
  /** 编码包含条件。 */
  @Size(max = 64)
  private String warehouseCode;

  /** 名称包含条件。 */
  @Size(max = 100)
  private String warehouseName;

  /** 编码或名称关键词。 */
  @Size(max = 100)
  private String keyword;

  /** 状态条件。 */
  @Pattern(regexp = "[01]")
  private String status;

  /** 货位管理条件。 */
  private Boolean locationEnabled;

  /** 排序字段。 */
  @Pattern(regexp = "warehouseCode|warehouseName|updateTime")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 设置编码包含条件。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode =
        warehouseCode == null || warehouseCode.isBlank() ? null : warehouseCode.strip();
  }

  /** 设置名称包含条件。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName =
        warehouseName == null || warehouseName.isBlank() ? null : warehouseName.strip();
  }

  /** 设置编码或名称关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
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
