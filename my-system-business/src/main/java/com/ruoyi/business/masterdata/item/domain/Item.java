package com.ruoyi.business.masterdata.item.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 料品档案Item。 */
@Getter
@Setter
@TableName("md_item")
public class Item extends BusinessEntity {
  /** 料品 ID。 */
  @TableId(type = IdType.AUTO)
  private Long itemId;

  /** 料品编码。 */
  private String itemCode;

  /** 料品名称。 */
  private String itemName;

  /** 规格型号。 */
  private String specification;

  /** 分类 ID。 */
  private Long categoryId;

  /** 库存基本单位 ID。 */
  private Long baseUnitId;

  /** 默认采购单位 ID。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Long purchaseUnitId;

  /** 默认销售单位 ID。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Long salesUnitId;

  /** 允许采购。 */
  private Boolean purchaseEnabled;

  /** 默认采购税率，百分数。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private BigDecimal defaultTaxRate;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 并发版本号。 */
  private Integer version;
}
