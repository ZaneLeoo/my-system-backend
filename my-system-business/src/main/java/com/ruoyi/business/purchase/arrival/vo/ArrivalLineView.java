package com.ruoyi.business.purchase.arrival.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 到货行快照及来源订单的当前执行数量 */
@Getter
@Setter
public class ArrivalLineView {
  /** 待审核入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingReceiptQuantity = BigDecimal.ZERO;

  /** 已入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal receivedQuantity = BigDecimal.ZERO;

  /** 可新增入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableReceiptQuantity;

  /** 到货明细 ID。 */
  private Long lineId;

  /** 到货单 ID。 */
  private Long arrivalId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 来源订单号快照。 */
  private String orderNo;

  /** 来源订单行号快照。 */
  private Integer orderLineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 本次到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal quantity;

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 行备注。 */
  private String remark;

  /** 订单数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal orderQuantity;

  /** 待审核到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingArrivalQuantity;

  /** 已确认到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivedQuantity;

  /** 可新增到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;

  /** 显式映射业务快照，不返回内部删除标记。 */
  public static ArrivalLineView from(PurchaseArrivalLine entity) {
    ArrivalLineView result = new ArrivalLineView();
    result.setPendingReceiptQuantity(entity.getPendingReceiptQuantity());
    result.setReceivedQuantity(entity.getReceivedQuantity());
    result.setAvailableReceiptQuantity(
        entity
            .getQuantity()
            .subtract(entity.getPendingReceiptQuantity())
            .subtract(entity.getReceivedQuantity()));
    result.setLineId(entity.getLineId());
    result.setArrivalId(entity.getArrivalId());
    result.setLineNo(entity.getLineNo());
    result.setOrderId(entity.getOrderId());
    result.setOrderLineId(entity.getOrderLineId());
    result.setOrderNo(entity.getOrderNo());
    result.setOrderLineNo(entity.getOrderLineNo());
    result.setItemId(entity.getItemId());
    result.setItemCode(entity.getItemCode());
    result.setItemName(entity.getItemName());
    result.setSpecification(entity.getSpecification());
    result.setUnitId(entity.getUnitId());
    result.setUnitName(entity.getUnitName());
    result.setQuantity(entity.getQuantity());
    result.setQuantityDecimalPlaces(entity.getQuantityDecimalPlaces());
    result.setRemark(entity.getRemark());
    return result;
  }
}
