package com.ruoyi.business.purchase.arrival.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 采购到货明细，稳定行 ID 供后续入库引用 */
@Getter
@Setter
@TableName("purchase_arrival_line")
public class PurchaseArrivalLine extends BusinessEntity {
  /** 待审核入库数量。 */
  private BigDecimal pendingReceiptQuantity = BigDecimal.ZERO;

  /** 已入库数量。 */
  private BigDecimal receivedQuantity = BigDecimal.ZERO;

  /** 到货明细 ID。 */
  @TableId(type = IdType.AUTO)
  private Long lineId;

  /** 到货单 ID。 */
  private Long arrivalId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 来源订单号快照。 */
  private String orderNo;

  /** 来源订单行号快照。 */
  private Integer orderLineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 本次到货数量。 */
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;
}
