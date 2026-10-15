package com.ruoyi.business.masterdata.unit.vo;

import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;

/** 单位列表与详情响应。 */
public class UnitView {
  /** 单位 ID。 */
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

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

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

  /** 显式映射对外字段，避免返回内部删除标记。 */
  public static UnitView from(Unit entity) {
    UnitView view = new UnitView();
    view.setUnitId(entity.getUnitId());
    view.setUnitCode(entity.getUnitCode());
    view.setUnitName(entity.getUnitName());
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
