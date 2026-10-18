package com.ruoyi.business.purchase.order.vo;

import com.ruoyi.business.purchase.arrival.domain.ReceiptProgress;
import com.ruoyi.business.purchase.order.domain.ArrivalProgress;

/** 订单分页批量读取的到货和入库执行进度。 */
public class OrderExecutionProgress {
  private Long orderId;
  private ArrivalProgress arrivalProgress;

  /** 入库进度。 */
  private ReceiptProgress receiptProgress;

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

  /** 返回入库进度。 */
  public ReceiptProgress getReceiptProgress() {
    return receiptProgress;
  }

  /** 设置入库进度。 */
  public void setReceiptProgress(ReceiptProgress receiptProgress) {
    this.receiptProgress = receiptProgress;
  }
}
