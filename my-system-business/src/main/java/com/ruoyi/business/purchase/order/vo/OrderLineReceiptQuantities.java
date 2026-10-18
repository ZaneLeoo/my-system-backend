package com.ruoyi.business.purchase.order.vo;

import java.math.BigDecimal;

/** 按稳定订单行汇总的入库执行量，不向客户端开放持久化实体。 */
public class OrderLineReceiptQuantities {
  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 待审核入库数量。 */
  private BigDecimal pendingReceiptQuantity = BigDecimal.ZERO;

  /** 已入库数量。 */
  private BigDecimal receivedQuantity = BigDecimal.ZERO;

  /** 返回来源订单行 ID。 */
  public Long getOrderLineId() {
    return orderLineId;
  }

  /** 设置来源订单行 ID。 */
  public void setOrderLineId(Long orderLineId) {
    this.orderLineId = orderLineId;
  }

  /** 返回待审核入库数量。 */
  public BigDecimal getPendingReceiptQuantity() {
    return pendingReceiptQuantity;
  }

  /** 设置待审核入库数量。 */
  public void setPendingReceiptQuantity(BigDecimal pendingReceiptQuantity) {
    this.pendingReceiptQuantity = pendingReceiptQuantity;
  }

  /** 返回已入库数量。 */
  public BigDecimal getReceivedQuantity() {
    return receivedQuantity;
  }

  /** 设置已入库数量。 */
  public void setReceivedQuantity(BigDecimal receivedQuantity) {
    this.receivedQuantity = receivedQuantity;
  }
}
