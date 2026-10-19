package com.ruoyi.business.masterdata.unit.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 单位保存参数，不接受客户端审计与删除标记。 */
@Getter
@Setter
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

  /** 设置单位编码。 */
  public void setUnitCode(String unitCode) {
    this.unitCode = unitCode == null ? null : unitCode.strip();
  }

  /** 设置单位名称。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName == null ? null : unitName.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
