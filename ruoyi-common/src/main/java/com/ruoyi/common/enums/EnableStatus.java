package com.ruoyi.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** 基础资料共用的启停状态，数据库与接口均使用相同编码。 */
public enum EnableStatus {
  ENABLED("0"),
  DISABLED("1");

  @EnumValue private final String code;

  /** 绑定状态编码。 */
  EnableStatus(String code) {
    this.code = code;
  }

  /** 返回数据库和接口使用的状态编码。 */
  @JsonValue
  public String getCode() {
    return code;
  }

  /** 从接口编码解析状态，拒绝未知值。 */
  @JsonCreator
  public static EnableStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("状态必须为 0（启用）或 1（停用）"));
  }
}
