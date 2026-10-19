package com.ruoyi.business.masterdata.location.vo;

import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 货位维护响应。 */
@Getter
@Setter
public class LocationView {
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
  public static LocationView from(Location entity) {
    LocationView view = new LocationView();
    view.setLocationId(entity.getLocationId());
    view.setWarehouseId(entity.getWarehouseId());
    view.setLocationCode(entity.getLocationCode());
    view.setLocationName(entity.getLocationName());
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
