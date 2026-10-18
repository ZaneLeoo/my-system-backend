package com.ruoyi.business.purchase.receipt.domain;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** 入库确认状态，与后续入库进度分开。 */
public enum ReceiptStatus {
  DRAFT("DRAFT"),
  SUBMITTED("SUBMITTED"),
  APPROVED("APPROVED"),
  VOIDED("VOIDED");
  @EnumValue private final String code;

  /** 绑定持久化与接口编码。 */
  ReceiptStatus(String code) {
    this.code = code;
  }

  /** 返回编码。 */
  @JsonValue
  public String getCode() {
    return code;
  }

  /** 拒绝未知编码，避免静默降级。 */
  @JsonCreator
  public static ReceiptStatus fromCode(String value) {
    return Arrays.stream(values())
        .filter(v -> v.code.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的ReceiptStatus编码"));
  }
}
