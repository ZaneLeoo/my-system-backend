package com.ruoyi.business.masterdata.reference.vo;

import com.ruoyi.business.masterdata.location.vo.LocationView;
import com.ruoyi.common.enums.EnableStatus;

/** 货位只读参照。 */
public class LocationReference {
  /** 货位 ID。 */
  private Long locationId;

  /** 所属仓库 ID。 */
  private Long warehouseId;

  /** 货位编码。 */
  private String locationCode;

  /** 货位名称。 */
  private String locationName;

  /** 启停状态。 */
  private EnableStatus status;

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
    this.locationCode = locationCode;
  }

  /** 返回货位名称。 */
  public String getLocationName() {
    return locationName;
  }

  /** 设置货位名称。 */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
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
  public static LocationReference from(LocationView source) {
    LocationReference result = new LocationReference();
    result.setLocationId(source.getLocationId());
    result.setWarehouseId(source.getWarehouseId());
    result.setLocationCode(source.getLocationCode());
    result.setLocationName(source.getLocationName());
    result.setStatus(source.getStatus());
    return result;
  }
}
