package com.ruoyi.business.purchase.order.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 采购订单持久化实体，业务快照与审计分开维护。 */
@Getter
@Setter
@TableName("purchase_order")
public class PurchaseOrder extends BusinessEntity {
  /** 订单 ID。 */
  @TableId(type = IdType.AUTO)
  private Long orderId;

  /** 系统订单号。 */
  private String orderNo;

  /** 订单日期。 */
  private LocalDate orderDate;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 供应商编码快照。 */
  private String supplierCode;

  /** 供应商名称快照。 */
  private String supplierName;

  /** 联系人快照。 */
  private String contactName;

  /** 联系电话快照。 */
  private String contactPhone;

  /** 联系地址快照。 */
  private String address;

  /** 采购员 ID。 */
  private Long buyerId;

  /** 采购员姓名快照。 */
  private String buyerName;

  /** 采购部门 ID。 */
  private Long deptId;

  /** 采购部门名称快照。 */
  private String deptName;

  /** 数量小数位。 */
  private Integer quantityDecimalPlaces;

  /** 单价小数位。 */
  private Integer priceDecimalPlaces;

  /** 金额小数位。 */
  private Integer amountDecimalPlaces;

  /** 舍入方式。 */
  private String roundingMode;

  /** 币种。 */
  private String currency;

  /** 单价计价方式。 */
  private PriceTaxMode priceTaxMode;

  /** 单据状态。 */
  private OrderStatus status;

  /** 明细行数。 */
  private Integer lineCount;

  /** 未税金额。 */
  private BigDecimal netAmount;

  /** 税额。 */
  private BigDecimal tax;

  /** 价税合计。 */
  private BigDecimal grossAmount;

  /** 备注。 */
  private String remark;

  /** 并发版本。 */
  private Integer version;

  /** 提交人。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String submittedBy;

  /** 提交时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime submittedTime;

  /** 审核人。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String reviewedBy;

  /** 审核时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime reviewedTime;

  /** 最近审核意见。 */
  private String reviewRemark;

  /** 关闭人。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String closedBy;

  /** 关闭时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime closedTime;

  /** 关闭说明。 */
  private String closeReason;
}
