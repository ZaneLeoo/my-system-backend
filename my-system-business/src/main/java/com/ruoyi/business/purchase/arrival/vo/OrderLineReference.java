package com.ruoyi.business.purchase.arrival.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 来源订单行参照，只返回选择到货所需字段 */
@Getter
@Setter
public class OrderLineReference {
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

  /** 来源数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 来源供应商 ID。 */
  private Long supplierId;

  /** 订单日期。 */
  private LocalDate orderDate;

  /** 预计到货日期。 */
  private LocalDate expectedDate;

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
}
