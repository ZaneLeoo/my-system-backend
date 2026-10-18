package com.ruoyi.business.inventory.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** InventoryBalance 的显式数据约定。 */
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

  /** 返回库存余额 ID。 */
  public Long getBalanceId() {
    return balanceId;
  }

  /** 设置库存余额 ID。 */
  public void setBalanceId(Long balanceId) {
    this.balanceId = balanceId;
  }

  /** 返回仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回货位 ID。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置货位 ID。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回库存单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置库存单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回当前库存数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置当前库存数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回记账版本。 */
  public Long getVersion() {
    return version;
  }

  /** 设置记账版本。 */
  public void setVersion(Long version) {
    this.version = version;
  }
}
