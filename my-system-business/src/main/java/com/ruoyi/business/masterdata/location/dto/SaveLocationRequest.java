package com.ruoyi.business.masterdata.location.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;

/** 货位维护参数，不接受审计和删除标记。 */
public class SaveLocationRequest {
  /** 货位 ID。 */
  @Positive private Long locationId;

  /** 所属仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 货位编码。 */
  @NotBlank
  @Size(max = 64)
  private String locationCode;

  /** 货位名称。 */
  @NotBlank
  @Size(max = 100)
  private String locationName;

  /** 启停状态。 */
  @NotNull private EnableStatus status = EnableStatus.ENABLED;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 返回货位 ID。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置货位 ID。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  /** 返回所属仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置所属仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回货位编码。 */
  public String getLocationCode() {
    return locationCode;
  }

  /** 设置货位编码。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode == null ? null : locationCode.strip();
  }

  /** 返回货位名称。 */
  public String getLocationName() {
    return locationName;
  }

  /** 设置货位名称。 */
  public void setLocationName(String locationName) {
    this.locationName = locationName == null ? null : locationName.strip();
  }

  /** 返回启停状态。 */
  public EnableStatus getStatus() {
    return status;
  }

  /** 设置启停状态。 */
  public void setStatus(EnableStatus status) {
    this.status = status;
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回编辑版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置编辑版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }
}
