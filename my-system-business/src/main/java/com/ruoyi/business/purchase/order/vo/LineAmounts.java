package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 行金额计算结果，换算单价不参与金额反算。 */
@Getter
@Setter
public class LineAmounts {
  /** 未税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netPrice;

  /** 含税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossPrice;

  /** 未税金额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netAmount;

  /** 税额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal tax;

  /** 价税合计。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossAmount;
}
