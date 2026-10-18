package com.ruoyi.business.masterdata.warehouse.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;

/** 仓库实体，继承公共审计及逻辑删除。 */
@TableName("md_warehouse")
public class Warehouse extends BusinessEntity {
  /** 仓库 ID。 */
  @TableId(type = IdType.AUTO)
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
