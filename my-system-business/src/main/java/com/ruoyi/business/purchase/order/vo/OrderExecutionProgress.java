package com.ruoyi.business.purchase.order.vo;

import com.ruoyi.business.purchase.arrival.domain.ReceiptProgress;
import com.ruoyi.business.purchase.order.domain.ArrivalProgress;
import lombok.Getter;
import lombok.Setter;

/** 订单分页批量读取的到货和入库执行进度。 */
@Getter
@Setter
public class OrderExecutionProgress {
  /** 采购订单 ID，用于归属当前订单的执行进度。 */
  private Long orderId;

  /** 根据订单明细累计到货数量派生的到货进度。 */
  private ArrivalProgress arrivalProgress;

  /** 入库进度。 */
  private ReceiptProgress receiptProgress;
}
