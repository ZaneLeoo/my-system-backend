package com.ruoyi.business.masterdata.unit.vo;

import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 料品档案UnitOption。 */
@Getter
@Setter
public class UnitOption {
  /** 单位 ID。 */
  private Long unitId;

  /** 单位编码。 */
  private String unitCode;

  /** 单位名称。 */
  private String unitName;

  /** 启停状态。 */
  private EnableStatus status;
}
