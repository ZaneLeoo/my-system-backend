package com.ruoyi.business.masterdata.warehouse.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;

/** 仓库维护参数，不接受审计和删除标记。 */
public class SaveWarehouseRequest {
  /** 仓库 ID。 */
  @Positive private Long warehouseId;

  /** 仓库编码。 */
  @NotBlank
  @Size(max = 64)
  private String warehouseCode;

  /** 仓库名称。 */
  @NotBlank
  @Size(max = 100)
  private String warehouseName;

  /** 是否启用货位管理。 */
  @NotNull private Boolean locationEnabled = false;

  /** 启停状态。 */
  @NotNull private EnableStatus status = EnableStatus.ENABLED;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 返回仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回仓库编码。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode == null ? null : warehouseCode.strip();
  }

  /** 返回仓库名称。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName == null ? null : warehouseName.strip();
  }

  /** 返回是否启用货位管理。 */
  public Boolean getLocationEnabled() {
    return locationEnabled;
  }

  /** 设置是否启用货位管理。 */
  public void setLocationEnabled(Boolean locationEnabled) {
    this.locationEnabled = locationEnabled;
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
