package com.ruoyi.business.purchase.arrival.domain;

/** 到货单的行数及参照批量边界。 */
public final class ArrivalRules {
  public static final int MAX_LINES = 200;
  public static final int MAX_REFERENCE_IDS = 100;
  public static final String NUMBER_PREFIX = "PA";

  /** 常量类禁止实例化。 */
  private ArrivalRules() {}
}
