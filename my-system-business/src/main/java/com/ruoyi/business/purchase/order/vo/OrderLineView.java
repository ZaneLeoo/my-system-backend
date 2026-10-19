package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 订单对外响应，金额用十进制字符串传输。 */
@Getter
@Setter
public class OrderLineView {
  /** 待审核入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingReceiptQuantity = BigDecimal.ZERO;

  /** 累计已入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal receivedQuantity = BigDecimal.ZERO;

  /** 已到货未入库数量（含待审核入库）。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivedNotReceivedQuantity = BigDecimal.ZERO;

  /** 明细行 ID。 */
  private Long lineId;

  /** 订单 ID。 */
  private Long orderId;

  /** 业务行号。 */
  private Integer lineNo;

  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码快照。 */
  private String itemCode;

  /** 料品名称快照。 */
  private String itemName;

  /** 规格快照。 */
  private String specification;

  /** 库存单位 ID。 */
  private Long unitId;

  /** 单位名称快照。 */
  private String unitName;

  /** 数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal quantity;

  /** 未税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netPrice;

  /** 含税单价。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossPrice;

  /** 税率百分数。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal taxRate;

  /** 未税金额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netAmount;

  /** 税额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal tax;

  /** 价税合计。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossAmount;

  /** 预计到货日期。 */
  private LocalDate expectedDate;

  /** 行备注。 */
  private String remark;

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 待审核到货数量，由到货事务维护。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingArrivalQuantity;

  /** 已确认到货数量，由到货事务维护。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivedQuantity;

  /** 当前可新增到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;

  /** 显式映射快照与业务字段，不暴露内部删除标记。 */
  public static OrderLineView from(PurchaseOrderLine entity) {
    OrderLineView result = new OrderLineView();
    result.setLineId(entity.getLineId());
    result.setOrderId(entity.getOrderId());
    result.setLineNo(entity.getLineNo());
    result.setItemId(entity.getItemId());
    result.setItemCode(entity.getItemCode());
    result.setItemName(entity.getItemName());
    result.setSpecification(entity.getSpecification());
    result.setUnitId(entity.getUnitId());
    result.setUnitName(entity.getUnitName());
    result.setQuantity(entity.getQuantity());
    result.setNetPrice(entity.getNetPrice());
    result.setGrossPrice(entity.getGrossPrice());
    result.setTaxRate(entity.getTaxRate());
    result.setNetAmount(entity.getNetAmount());
    result.setTax(entity.getTax());
    result.setGrossAmount(entity.getGrossAmount());
    result.setExpectedDate(entity.getExpectedDate());
    result.setRemark(entity.getRemark());
    result.setCreateBy(entity.getCreateBy());
    result.setCreateTime(entity.getCreateTime());
    result.setUpdateBy(entity.getUpdateBy());
    result.setUpdateTime(entity.getUpdateTime());
    result.setPendingArrivalQuantity(entity.getPendingArrivalQuantity());
    result.setArrivedQuantity(entity.getArrivedQuantity());
    result.setAvailableQuantity(
        entity
            .getQuantity()
            .subtract(entity.getPendingArrivalQuantity())
            .subtract(entity.getArrivedQuantity()));
    return result;
  }
}
