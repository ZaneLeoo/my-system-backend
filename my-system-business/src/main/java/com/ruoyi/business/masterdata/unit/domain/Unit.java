package com.ruoyi.business.masterdata.unit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;

/** 计量单位实体，继承公共审计与逻辑删除字段。 */
@TableName("md_unit")
public class Unit extends BusinessEntity {
  /** 单位 ID。 */
  @TableId(type = IdType.AUTO)
  private Long unitId;

  /** 单位编码。 */
  private String unitCode;

  /** 单位名称。 */
  private String unitName;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;

  /** 返回单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回单位编码。 */
  public String getUnitCode() {
    return unitCode;
  }

  /** 设置单位编码。 */
  public void setUnitCode(String unitCode) {
    this.unitCode = unitCode;
  }

  /** 返回单位名称。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位名称。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
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
