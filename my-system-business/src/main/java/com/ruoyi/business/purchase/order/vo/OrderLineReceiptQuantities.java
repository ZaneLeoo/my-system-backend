package com.ruoyi.business.purchase.order.vo;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 按稳定订单行汇总的入库执行量，不向客户端开放持久化实体。 */
@Getter
@Setter
public class OrderLineReceiptQuantities {
  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 待审核入库数量。 */
  private BigDecimal pendingReceiptQuantity = BigDecimal.ZERO;

  /** 已入库数量。 */
  private BigDecimal receivedQuantity = BigDecimal.ZERO;
}
