package com.ruoyi.business.masterdata.warehouse.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 仓库维护参数，不接受审计和删除标记。 */
@Getter
@Setter
public class SaveWarehouseRequest {
  /** 仓库 ID。 */
  @Positive private Long warehouseId;

  /** 仓库编码。 */
  @NotBlank
  @Size(max = 64)
  private String warehouseCode;

  /** 仓库名称。 */
  @NotBlank
  @Size(max = 100)
  private String warehouseName;

  /** 是否启用货位管理。 */
  @NotNull private Boolean locationEnabled = false;

  /** 启停状态。 */
  @NotNull private EnableStatus status = EnableStatus.ENABLED;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode == null ? null : warehouseCode.strip();
  }

  /** 设置仓库名称。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName == null ? null : warehouseName.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
