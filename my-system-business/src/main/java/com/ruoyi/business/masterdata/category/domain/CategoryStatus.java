package com.ruoyi.business.masterdata.category.domain;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** 分类启停状态，同时用于数据库和 API 编码。 */
public enum CategoryStatus {
  ENABLED("0"),
  DISABLED("1");

  @EnumValue private final String code;

  /** 绑定与现有若依启停字典一致的状态编码。 */
  CategoryStatus(String code) {
    this.code = code;
  }

  /** 返回对外状态编码。 */
  @JsonValue
  public String getCode() {
    return code;
  }

  /** 从 API 编码解析状态，拒绝未知值。 */
  @JsonCreator
  public static CategoryStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("分类状态必须为 0（启用）或 1（停用）"));
  }
}
