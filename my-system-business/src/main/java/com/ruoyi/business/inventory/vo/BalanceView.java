package com.ruoyi.business.inventory.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** BalanceView 的显式数据约定。 */
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

  /** 返回仓库编码。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode;
  }

  /** 返回仓库名称。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
  }

  /** 返回货位编码。 */
  public String getLocationCode() {
    return locationCode;
  }

  /** 设置货位编码。 */
  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode;
  }

  /** 返回货位名称。 */
  public String getLocationName() {
    return locationName;
  }

  /** 设置货位名称。 */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  /** 返回料品编码。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /** 返回料品名称。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回规格。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格。 */
  public void setSpecification(String specification) {
    this.specification = specification;
  }

  /** 返回库存单位名称。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置库存单位名称。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }
}
