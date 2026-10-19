package com.ruoyi.business.masterdata.supplier.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 供应商列表查询参数，筛选和排序均由后端执行。 */
@Getter
@Setter
public class SupplierQuery extends PageQuery {
  /** 编码包含条件。 */
  @Size(max = 64)
  private String supplierCode;

  /** 名称包含条件。 */
  @Size(max = 200)
  private String supplierName;

  /** 启停条件。 */
  @Pattern(regexp = "[01]", message = "状态必须为 0 或 1")
  private String status;

  /** 排序字段。 */
  @Pattern(regexp = "supplierCode|supplierName|updateTime", message = "不支持的排序字段")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc", message = "排序方向必须为 asc 或 desc")
  private String sortDirection = "asc";

  /** 设置编码包含条件。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode =
        supplierCode == null || supplierCode.isBlank() ? null : supplierCode.strip();
  }

  /** 设置名称包含条件。 */
  public void setSupplierName(String supplierName) {
    this.supplierName =
        supplierName == null || supplierName.isBlank() ? null : supplierName.strip();
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
