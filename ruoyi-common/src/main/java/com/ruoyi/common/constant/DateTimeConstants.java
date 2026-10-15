package com.ruoyi.common.constant;

import java.time.ZoneId;

/** 统一业务时间的时区和接口输出格式。 */
public final class DateTimeConstants {
  public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

  public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /** 常量类不允许实例化。 */
  private DateTimeConstants() {}
}
