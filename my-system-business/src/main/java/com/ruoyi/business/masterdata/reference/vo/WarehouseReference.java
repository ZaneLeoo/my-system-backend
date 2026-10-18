package com.ruoyi.business.masterdata.reference.vo;

import com.ruoyi.business.masterdata.warehouse.vo.WarehouseView;
import com.ruoyi.common.enums.EnableStatus;

/** 仓库只读参照。 */
public class WarehouseReference {
  /** 仓库 ID。 */
  private Long warehouseId;

  /** 仓库编码。 */
  private String warehouseCode;

  /** 仓库名称。 */
  private String warehouseName;

  /** 是否启用货位管理。 */
  private Boolean locationEnabled;

  /** 启停状态。 */
  private EnableStatus status;

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
    this.warehouseCode = warehouseCode;
  }

  /** 返回仓库名称。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
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

  /** 仅投影选择器需要的公开字段。 */
  public static WarehouseReference from(WarehouseView source) {
    WarehouseReference result = new WarehouseReference();
    result.setWarehouseId(source.getWarehouseId());
    result.setWarehouseCode(source.getWarehouseCode());
    result.setWarehouseName(source.getWarehouseName());
    result.setLocationEnabled(source.getLocationEnabled());
    result.setStatus(source.getStatus());
    return result;
  }
}
