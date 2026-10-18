package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** 到货行输入，来源快照和执行数量不由客户端指定 */
public class ArrivalLineInput {
  /** 已有到货行 ID。 */
  @Positive private Long lineId;

  /** 来源订单行 ID。 */
  @NotNull @Positive private Long orderLineId;

  /** 本次到货数量。 */
  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  @Digits(integer = 9, fraction = 6)
  private BigDecimal quantity;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 返回已有到货行 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置已有到货行 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回来源订单行 ID。 */
  public Long getOrderLineId() {
    return orderLineId;
  }

  /** 设置来源订单行 ID。 */
  public void setOrderLineId(Long orderLineId) {
    this.orderLineId = orderLineId;
  }

  /** 返回本次到货数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置本次到货数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
