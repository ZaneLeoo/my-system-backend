package com.ruoyi.business.masterdata.location.vo;

import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;

/** 货位维护响应。 */
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

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 返回编辑版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置编辑版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回创建人。 */
  public String getCreateBy() {
    return createBy;
  }

  /** 设置创建人。 */
  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  /** 返回创建时间。 */
  public LocalDateTime getCreateTime() {
    return createTime;
  }

  /** 设置创建时间。 */
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  /** 返回修改人。 */
  public String getUpdateBy() {
    return updateBy;
  }

  /** 设置修改人。 */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  /** 返回修改时间。 */
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  /** 设置修改时间。 */
  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

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
