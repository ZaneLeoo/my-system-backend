package com.ruoyi.business.masterdata.unit.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 单位保存参数，不接受客户端审计与删除标记。 */
public class SaveUnitRequest {
  /** 单位 ID，新增时不指定。 */
  @Positive private Long unitId;

  /** 单位编码。 */
  @NotBlank(message = "单位编码不能为空")
  @Size(max = 64, message = "单位编码最多 64 个字符")
  private String unitCode;

  /** 单位名称。 */
  @NotBlank(message = "单位名称不能为空")
  @Size(max = 100, message = "单位名称最多 100 个字符")
  private String unitName;

  /** 启停状态。 */
  @NotNull(message = "单位状态不能为空")
  private EnableStatus status;

  /** 备注。 */
  @Size(max = 500, message = "备注最多 500 个字符")
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 返回单位 ID，新增时不指定。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置单位 ID，新增时不指定。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回单位编码。 */
  public String getUnitCode() {
    return unitCode;
  }

  /** 设置单位编码。 */
  public void setUnitCode(String unitCode) {
    this.unitCode = unitCode == null ? null : unitCode.strip();
  }

  /** 返回单位名称。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位名称。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName == null ? null : unitName.strip();
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
    this.remark = remark == null ? "" : remark.strip();
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
