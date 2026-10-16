package com.ruoyi.business.masterdata.unit.vo;

import com.ruoyi.common.enums.EnableStatus;

/** 料品档案UnitOption。 */
public class UnitOption {
  /** 单位 ID。 */
  private Long unitId;

  /** 单位编码。 */
  private String unitCode;

  /** 单位名称。 */
  private String unitName;

  /** 启停状态。 */
  private EnableStatus status;

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
}
