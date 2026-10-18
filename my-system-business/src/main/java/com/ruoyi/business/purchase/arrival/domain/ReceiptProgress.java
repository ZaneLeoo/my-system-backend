package com.ruoyi.business.purchase.arrival.domain;

/** 到货单按行派生的入库进度，与单据状态独立。 */
public enum ReceiptProgress {
  NOT_RECEIVED,
  PARTIALLY_RECEIVED,
  FULLY_RECEIVED
}
