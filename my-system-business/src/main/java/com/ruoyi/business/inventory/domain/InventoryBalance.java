package com.ruoyi.business.inventory.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** InventoryBalance 的显式数据约定。 */
@Getter
@Setter
@TableName("inventory_balance")
public class InventoryBalance extends AuditEntity {
  /** 库存余额 ID。 */
  @TableId(type = IdType.AUTO)
  private Long balanceId;

  /** 仓库 ID。 */
  private Long warehouseId;

  /** 货位 ID。 */
  private Long locationId;

  /** 料品 ID。 */
  private Long itemId;

  /** 库存单位 ID。 */
  private Long unitId;

  /** 当前库存数量。 */
  private BigDecimal quantity = BigDecimal.ZERO;

  /** 记账版本。 */
  private Long version = 0L;
}
