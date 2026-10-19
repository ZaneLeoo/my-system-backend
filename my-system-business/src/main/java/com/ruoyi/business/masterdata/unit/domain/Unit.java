package com.ruoyi.business.masterdata.unit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 计量单位实体，继承公共审计与逻辑删除字段。 */
@Getter
@Setter
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
}
