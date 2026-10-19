package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** ReceiptLineInput 的显式数据约定。 */
@Getter
@Setter
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

  /** 设置行备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
