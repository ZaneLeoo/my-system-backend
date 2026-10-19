package com.ruoyi.business.masterdata.location.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 货位实体，继承公共审计及逻辑删除。 */
@Getter
@Setter
@TableName("md_location")
public class Location extends BusinessEntity {
  /** 货位 ID。 */
  @TableId(type = IdType.AUTO)
  private Long locationId;

  /** 所属仓库 ID。 */
  private Long warehouseId;

  /** 货位编码。 */
  private String locationCode;

  /** 货位名称。 */
  private String locationName;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;
}
