package com.ruoyi.business.masterdata.supplier.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 供应商档案实体，继承公共审计与逻辑删除字段。 */
@Getter
@Setter
@TableName("md_supplier")
public class Supplier extends BusinessEntity {
  /** 供应商 ID。 */
  @TableId(type = IdType.AUTO)
  private Long supplierId;

  /** 供应商编码。 */
  private String supplierCode;

  /** 供应商名称。 */
  private String supplierName;

  /** 简称。 */
  private String shortName;

  /** 主要联系人。 */
  private String contactName;

  /** 联系电话。 */
  private String contactPhone;

  /** 地址。 */
  private String address;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;
}
