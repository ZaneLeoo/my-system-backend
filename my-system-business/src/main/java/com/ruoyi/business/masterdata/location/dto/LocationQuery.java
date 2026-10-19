package com.ruoyi.business.masterdata.location.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 货位分页查询。 */
@Getter
@Setter
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

  /** 设置编码包含条件。 */
  public void setLocationCode(String locationCode) {
    this.locationCode =
        locationCode == null || locationCode.isBlank() ? null : locationCode.strip();
  }

  /** 设置名称包含条件。 */
  public void setLocationName(String locationName) {
    this.locationName =
        locationName == null || locationName.isBlank() ? null : locationName.strip();
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
