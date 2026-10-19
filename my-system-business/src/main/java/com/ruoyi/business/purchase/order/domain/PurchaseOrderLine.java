package com.ruoyi.business.purchase.order.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 采购订单持久化实体，业务快照与审计分开维护。 */
@Getter
@Setter
@TableName("purchase_order_line")
public class PurchaseOrderLine extends BusinessEntity {
  /** 明细行 ID。 */
  @TableId(type = IdType.AUTO)
  private Long lineId;

  /** 订单 ID。 */
  private Long orderId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 库存单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 数量。 */
  private BigDecimal quantity;

  /** 未税单价。 */
  private BigDecimal netPrice;

  /** 含税单价。 */
  private BigDecimal grossPrice;

  /** 税率百分数。 */
  private BigDecimal taxRate;

  /** 未税金额。 */
  private BigDecimal netAmount;

  /** 税额。 */
  private BigDecimal tax;

  /** 价税合计。 */
  private BigDecimal grossAmount;

  /** 预计到货日期。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDate expectedDate;

  /** 行备注。 */
  private String remark;

  /** 待审核到货数量，由到货事务维护。 */
  private BigDecimal pendingArrivalQuantity = BigDecimal.ZERO;

  /** 已确认到货数量，由到货事务维护。 */
  private BigDecimal arrivedQuantity = BigDecimal.ZERO;
}
