package com.ruoyi.business.inventory.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** InventoryTransaction 的显式数据约定。 */
@Getter
@Setter
@TableName("inventory_transaction")
public class InventoryTransaction extends AuditEntity {
  /** 库存流水 ID。 */
  @TableId(type = IdType.AUTO)
  private Long transactionId;

  /** 库存维度 ID。 */
  private Long balanceId;

  /** 业务类型。 */
  private String businessType;

  /** 入库单 ID。 */
  private Long receiptId;

  /** 入库单号。 */
  private String receiptNo;

  /** 入库行 ID。 */
  private Long receiptLineId;

  /** 仓库 ID。 */
  private Long warehouseId;

  /** 货位 ID。 */
  private Long locationId;

  /** 料品 ID。 */
  private Long itemId;

  /** 单位 ID。 */
  private Long unitId;

  /** 仓库编码快照。 */
  private String warehouseCode;

  /** 仓库名称快照。 */
  private String warehouseName;

  /** 货位编码快照。 */
  private String locationCode;

  /** 货位名称快照。 */
  private String locationName;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 库存单位名称快照。 */
  private String unitName;

  /** 数量变化。 */
  private BigDecimal quantityChange;

  /** 变动前数量。 */
  private BigDecimal quantityBefore;

  /** 变动后数量。 */
  private BigDecimal quantityAfter;

  /** 业务日期。 */
  private LocalDate businessDate;

  /** 实际记账时间。 */
  private LocalDateTime postedTime;

  /** 被冲销流水 ID。 */
  private Long reversalOf;

  /** 业务说明。 */
  private String remark;
}
