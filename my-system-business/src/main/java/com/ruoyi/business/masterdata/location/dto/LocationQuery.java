package com.ruoyi.business.masterdata.location.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;

/** 货位分页查询。 */
public class LocationQuery extends PageQuery {
  /** 所属仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 编码包含条件。 */
  @Size(max = 64)
  private String locationCode;

  /** 名称包含条件。 */
  @Size(max = 100)
  private String locationName;

  /** 编码或名称关键词。 */
  @Size(max = 100)
  private String keyword;

  /** 状态条件。 */
  @Pattern(regexp = "[01]")
  private String status;

  /** 排序字段。 */
  @Pattern(regexp = "locationCode|locationName|updateTime")
  private String sortField;

  /** 排序方向。 */
  @NotNull
  @Pattern(regexp = "asc|desc")
  private String sortDirection = "asc";

  /** 返回所属仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置所属仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回编码包含条件。 */
  public String getLocationCode() {
    return locationCode;
  }

  /** 设置编码包含条件。 */
  public void setLocationCode(String locationCode) {
    this.locationCode =
        locationCode == null || locationCode.isBlank() ? null : locationCode.strip();
  }

  /** 返回名称包含条件。 */
  public String getLocationName() {
    return locationName;
  }

  /** 设置名称包含条件。 */
  public void setLocationName(String locationName) {
    this.locationName =
        locationName == null || locationName.isBlank() ? null : locationName.strip();
  }

  /** 返回编码或名称关键词。 */
  public String getKeyword() {
    return keyword;
  }

  /** 设置编码或名称关键词。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }

  /** 返回状态条件。 */
  public String getStatus() {
    return status;
  }

  /** 设置状态条件。 */
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
