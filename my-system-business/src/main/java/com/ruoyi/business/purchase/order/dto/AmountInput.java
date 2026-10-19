package com.ruoyi.business.purchase.order.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 金额计算输入，单价仅接受当前口径的一种价格。 */
@Getter
@Setter
public class AmountInput {
  /** 数量。 */
  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  @Digits(integer = 9, fraction = 6)
  private BigDecimal quantity;

  /** 所选计价口径的原始单价。 */
  @NotNull
  @DecimalMin("0")
  @Digits(integer = 12, fraction = 6)
  private BigDecimal unitPrice;

  /** 税率百分数。 */
  @NotNull
  @DecimalMin("0")
  @DecimalMax("100")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal taxRate;
}
