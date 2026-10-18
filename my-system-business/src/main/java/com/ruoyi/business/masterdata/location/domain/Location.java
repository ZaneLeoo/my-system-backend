package com.ruoyi.business.masterdata.location.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;

/** 货位实体，继承公共审计及逻辑删除。 */
@TableName("md_location")
public class Location extends BusinessEntity {
  /** 货位 ID。 */
  @TableId(type = IdType.AUTO)
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
}
