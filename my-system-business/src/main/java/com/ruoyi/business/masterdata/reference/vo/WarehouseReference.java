package com.ruoyi.business.masterdata.reference.vo;

import com.ruoyi.business.masterdata.warehouse.vo.WarehouseView;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 仓库只读参照。 */
@Getter
@Setter
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
