package com.ruoyi.business.purchase.order.vo;

import com.ruoyi.business.purchase.order.domain.ArrivalProgress;

/** 订单分页批量读取的执行进度。 */
public class OrderArrivalProgress {
  private Long orderId;
  private ArrivalProgress arrivalProgress;

  /** 返回订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回到货进度。 */
  public ArrivalProgress getArrivalProgress() {
    return arrivalProgress;
  }

  /** 设置到货进度。 */
  public void setArrivalProgress(ArrivalProgress arrivalProgress) {
    this.arrivalProgress = arrivalProgress;
  }
}
