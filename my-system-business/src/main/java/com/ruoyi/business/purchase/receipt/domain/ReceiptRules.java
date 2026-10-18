package com.ruoyi.business.purchase.receipt.domain;

/** 入库单的行数及参照批量边界。 */
public final class ReceiptRules {
  public static final int MAX_LINES = 200;
  public static final int MAX_REFERENCE_IDS = 100;
  public static final String NUMBER_PREFIX = "PR";

  /** 常量类禁止实例化。 */
  private ReceiptRules() {}
}
