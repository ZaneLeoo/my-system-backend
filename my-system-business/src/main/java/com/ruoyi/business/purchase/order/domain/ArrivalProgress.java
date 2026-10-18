package com.ruoyi.business.purchase.order.domain;

/** 根据各订单行的已确认到货数量计算，不改变订单审批或关闭状态。 */
public enum ArrivalProgress {
  NOT_ARRIVED,
  PARTIALLY_ARRIVED,
  FULLY_ARRIVED
}
