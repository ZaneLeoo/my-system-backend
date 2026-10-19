package com.ruoyi.business.masterdata.reference.vo;

import com.ruoyi.business.masterdata.location.vo.LocationView;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 货位只读参照。 */
@Getter
@Setter
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
