package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** ReceiptLineInput 的显式数据约定。 */
public class ReceiptLineInput {
  /** 实际入库仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 已有行 ID。 */
  @Positive private Long lineId;

  /** 来源到货行 ID。 */
  @NotNull @Positive private Long arrivalLineId;

  /** 货位 ID。 */
  @Positive private Long locationId;

  /** 入库数量。 */
  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  @Digits(integer = 9, fraction = 6)
  private BigDecimal quantity;

  /** 行备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 返回已有行 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置已有行 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回来源到货行 ID。 */
  public Long getArrivalLineId() {
    return arrivalLineId;
  }

  /** 设置来源到货行 ID。 */
  public void setArrivalLineId(Long arrivalLineId) {
    this.arrivalLineId = arrivalLineId;
  }

  /** 返回货位 ID。 */
  public Long getLocationId() {
    return locationId;
  }

  /** 设置货位 ID。 */
  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  /** 返回入库数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置入库数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回行备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置行备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回实际入库仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置实际入库仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }
}
