package com.ruoyi.business.masterdata.unit.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 单位列表查询参数，筛选和排序均由后端执行。 */
@Getter
@Setter
public class UnitQuery extends PageQuery {
  /** 编码包含条件。 */
  @Size(max = 64)
  private String unitCode;

  /** 名称包含条件。 */
  @Size(max = 100)
  private String unitName;

  /** 启停条件。 */
  @Pattern(regexp = "[01]", message = "状态必须为 0 或 1")
  private String status;

  /** 排序字段。 */
  @Pattern(regexp = "unitCode|unitName|updateTime", message = "不支持的排序字段")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc", message = "排序方向必须为 asc 或 desc")
  private String sortDirection = "asc";

  /** 设置编码包含条件。 */
  public void setUnitCode(String unitCode) {
    this.unitCode = unitCode == null || unitCode.isBlank() ? null : unitCode.strip();
  }

  /** 设置名称包含条件。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName == null || unitName.isBlank() ? null : unitName.strip();
  }

  /** 设置启停条件。 */
  public void setStatus(String status) {
    this.status = status == null || status.isBlank() ? null : status.strip();
  }

  /** 设置排序字段。 */
  public void setSortField(String sortField) {
    this.sortField = sortField == null || sortField.isBlank() ? null : sortField.strip();
  }
}
