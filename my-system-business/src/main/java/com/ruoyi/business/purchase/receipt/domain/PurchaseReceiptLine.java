package com.ruoyi.business.purchase.receipt.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** PurchaseReceiptLine 的显式数据约定。 */
@Getter
@Setter
@TableName("purchase_receipt_line")
public class PurchaseReceiptLine extends BusinessEntity {
  /** 实际入库仓库 ID。 */
  private Long warehouseId;

  /** 仓库编码快照。 */
  private String warehouseCode;

  /** 仓库名称快照。 */
  private String warehouseName;

  /** 本行货位管理方式快照。 */
  private Boolean locationEnabled;

  /** 入库明细 ID。 */
  @TableId(type = IdType.AUTO)
  private Long lineId;

  /** 入库单 ID。 */
  private Long receiptId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 来源到货单 ID。 */
  private Long arrivalId;

  /** 来源到货明细 ID。 */
  private Long arrivalLineId;

  /** 来源到货单号。 */
  private String arrivalNo;

  /** 来源到货行号。 */
  private Integer arrivalLineNo;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 来源订单号。 */
  private String orderNo;

  /** 来源订单行号。 */
  private Integer orderLineNo;

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

  /** 单位快照。 */
  private String unitName;

  /** 货位 ID。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Long locationId;

  /** 货位编码快照。 */
  private String locationCode;

  /** 货位名称快照。 */
  private String locationName;

  /** 本次入库数量。 */
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;
}
