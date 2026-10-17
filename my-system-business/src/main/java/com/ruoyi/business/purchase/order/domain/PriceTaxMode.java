package com.ruoyi.business.purchase.order.domain;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** 订单价格口径，同单据统一且保存后不可切换。 */
public enum PriceTaxMode {
  TAX_INCLUDED("TAX_INCLUDED"),
  TAX_EXCLUDED("TAX_EXCLUDED");
  @EnumValue private final String code;

  /** 绑定持久化与接口编码。 */
  PriceTaxMode(String code) {
    this.code = code;
  }

  /** 返回编码。 */
  @JsonValue
  public String getCode() {
    return code;
  }

  /** 拒绝未知编码，避免静默降级。 */
  @JsonCreator
  public static PriceTaxMode fromCode(String value) {
    return Arrays.stream(values())
        .filter(v -> v.code.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的PriceTaxMode编码"));
  }
}
