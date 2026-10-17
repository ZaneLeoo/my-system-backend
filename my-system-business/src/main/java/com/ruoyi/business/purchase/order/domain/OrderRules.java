package com.ruoyi.business.purchase.order.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;

/** 采购一期的明确边界，不从前端接收精度及币种覆盖。 */
public final class OrderRules {
  public static final int MAX_LINES = 200;
  public static final int MAX_DECIMALS = 6;
  public static final int MONEY_DECIMALS = 2;
  public static final String CURRENCY = "CNY";
  public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
  public static final ZoneId TIME_ZONE = ZoneId.of("Asia/Shanghai");
  public static final BigDecimal MONEY_LIMIT = new BigDecimal("1000000000000000000");
  public static final BigDecimal QUANTITY_LIMIT = new BigDecimal("1000000000");
  public static final BigDecimal PRICE_LIMIT = new BigDecimal("1000000000000");
  public static final String QUANTITY_CONFIG = "purchase.order.quantityDecimalPlaces";
  public static final String PRICE_CONFIG = "purchase.order.priceDecimalPlaces";

  /** 常量类不允许实例化。 */
  private OrderRules() {}
}
