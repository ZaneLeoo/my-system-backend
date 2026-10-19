package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.business.purchase.order.domain.PriceTaxMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 只读金额试算请求，不修改订单。 */
@Getter
@Setter
public class CalculationRequest {
  /** 已有订单 ID。 */
  @Positive private Long orderId;

  /** 计价方式。 */
  @NotNull private PriceTaxMode priceTaxMode;

  /** 计算行。 */
  @NotEmpty
  @Size(max = 200)
  private List<@NotNull @Valid AmountInput> lines;
}
