package com.ruoyi.business.masterdata.warehouse.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 仓库实体，继承公共审计及逻辑删除。 */
@Getter
@Setter
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
}
