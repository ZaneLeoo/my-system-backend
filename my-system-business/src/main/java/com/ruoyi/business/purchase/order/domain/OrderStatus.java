package com.ruoyi.business.purchase.order.domain;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** 采购单据状态，与后续到货履约进度分开。 */
public enum OrderStatus {
  DRAFT("DRAFT"),
  SUBMITTED("SUBMITTED"),
  APPROVED("APPROVED"),
  CLOSED("CLOSED");
  @EnumValue private final String code;

  /** 绑定持久化与接口编码。 */
  OrderStatus(String code) {
    this.code = code;
  }

  /** 返回编码。 */
  @JsonValue
  public String getCode() {
    return code;
  }

  /** 拒绝未知编码，避免静默降级。 */
  @JsonCreator
  public static OrderStatus fromCode(String value) {
    return Arrays.stream(values())
        .filter(v -> v.code.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的OrderStatus编码"));
  }
}
