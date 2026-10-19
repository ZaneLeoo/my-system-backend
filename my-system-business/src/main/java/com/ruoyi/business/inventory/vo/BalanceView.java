package com.ruoyi.business.inventory.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** BalanceView 的显式数据约定。 */
@Getter
@Setter
public class BalanceView {
  /** 库存余额 ID。 */
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
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal quantity;

  /** 记账版本。 */
  private Long version;

  /** 仓库编码。 */
  private String warehouseCode;

  /** 仓库名称。 */
  private String warehouseName;

  /** 货位编码。 */
  private String locationCode;

  /** 货位名称。 */
  private String locationName;

  /** 料品编码。 */
  private String itemCode;

  /** 料品名称。 */
  private String itemName;

  /** 规格。 */
  private String specification;

  /** 库存单位名称。 */
  private String unitName;
}
