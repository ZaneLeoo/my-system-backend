package com.ruoyi.business.masterdata.warehouse.vo;

import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 仓库维护响应。 */
@Getter
@Setter
public class WarehouseView {
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

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 从实体构建维护响应，不泄露内部删除标记。 */
  public static WarehouseView from(Warehouse entity) {
    WarehouseView view = new WarehouseView();
    view.setWarehouseId(entity.getWarehouseId());
    view.setWarehouseCode(entity.getWarehouseCode());
    view.setWarehouseName(entity.getWarehouseName());
    view.setLocationEnabled(entity.getLocationEnabled());
    view.setStatus(entity.getStatus());
    view.setRemark(entity.getRemark());
    view.setVersion(entity.getVersion());
    view.setCreateBy(entity.getCreateBy());
    view.setCreateTime(entity.getCreateTime());
    view.setUpdateBy(entity.getUpdateBy());
    view.setUpdateTime(entity.getUpdateTime());
    return view;
  }
}
