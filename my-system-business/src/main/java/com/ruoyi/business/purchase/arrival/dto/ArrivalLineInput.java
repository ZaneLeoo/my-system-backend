package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 到货行输入，来源快照和执行数量不由客户端指定 */
@Getter
@Setter
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

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
