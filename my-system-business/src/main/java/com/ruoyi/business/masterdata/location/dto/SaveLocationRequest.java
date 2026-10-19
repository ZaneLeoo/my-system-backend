package com.ruoyi.business.masterdata.location.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 货位维护参数，不接受审计和删除标记。 */
@Getter
@Setter
public class SaveLocationRequest {
  /** 货位 ID。 */
  @Positive private Long locationId;

  /** 所属仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 货位编码。 */
  @NotBlank
  @Size(max = 64)
  private String locationCode;

  /** 货位名称。 */
  @NotBlank
  @Size(max = 100)
  private String locationName;

  /** 启停状态。 */
  @NotNull private EnableStatus status = EnableStatus.ENABLED;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 设置货位编码。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode == null ? null : locationCode.strip();
  }

  /** 设置货位名称。 */
  public void setLocationName(String locationName) {
    this.locationName = locationName == null ? null : locationName.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
