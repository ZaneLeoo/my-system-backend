package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 订单对外响应，金额用十进制字符串传输。 */
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

  /** 返回待审核到货数量。 */
  public BigDecimal getPendingArrivalQuantity() {
    return pendingArrivalQuantity;
  }

  /** 设置待审核到货数量。 */
  public void setPendingArrivalQuantity(BigDecimal value) {
    this.pendingArrivalQuantity = value;
  }

  /** 返回已确认到货数量。 */
  public BigDecimal getArrivedQuantity() {
    return arrivedQuantity;
  }

  /** 设置已确认到货数量。 */
  public void setArrivedQuantity(BigDecimal value) {
    this.arrivedQuantity = value;
  }

  /** 返回当前可新增到货数量。 */
  public BigDecimal getAvailableQuantity() {
    return availableQuantity;
  }

  /** 设置当前可新增到货数量。 */
  public void setAvailableQuantity(BigDecimal value) {
    this.availableQuantity = value;
  }

  /** 返回明细行 ID。 */
  public Long getLineId() {
    return lineId;
  }

  /** 设置明细行 ID。 */
  public void setLineId(Long lineId) {
    this.lineId = lineId;
  }

  /** 返回订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回业务行号。 */
  public Integer getLineNo() {
    return lineNo;
  }

  /** 设置业务行号。 */
  public void setLineNo(Integer lineNo) {
    this.lineNo = lineNo;
  }

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回料品编码快照。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码快照。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /** 返回料品名称快照。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称快照。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回规格快照。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格快照。 */
  public void setSpecification(String specification) {
    this.specification = specification;
  }

  /** 返回库存单位 ID。 */
  public Long getUnitId() {
    return unitId;
  }

  /** 设置库存单位 ID。 */
  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  /** 返回单位名称快照。 */
  public String getUnitName() {
    return unitName;
  }

  /** 设置单位名称快照。 */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /** 返回数量。 */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /** 设置数量。 */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /** 返回未税单价。 */
  public BigDecimal getNetPrice() {
    return netPrice;
  }

  /** 设置未税单价。 */
  public void setNetPrice(BigDecimal netPrice) {
    this.netPrice = netPrice;
  }

  /** 返回含税单价。 */
  public BigDecimal getGrossPrice() {
    return grossPrice;
  }

  /** 设置含税单价。 */
  public void setGrossPrice(BigDecimal grossPrice) {
    this.grossPrice = grossPrice;
  }

  /** 返回税率百分数。 */
  public BigDecimal getTaxRate() {
    return taxRate;
  }

  /** 设置税率百分数。 */
  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }

  /** 返回未税金额。 */
  public BigDecimal getNetAmount() {
    return netAmount;
  }

  /** 设置未税金额。 */
  public void setNetAmount(BigDecimal netAmount) {
    this.netAmount = netAmount;
  }

  /** 返回税额。 */
  public BigDecimal getTax() {
    return tax;
  }

  /** 设置税额。 */
  public void setTax(BigDecimal tax) {
    this.tax = tax;
  }

  /** 返回价税合计。 */
  public BigDecimal getGrossAmount() {
    return grossAmount;
  }

  /** 设置价税合计。 */
  public void setGrossAmount(BigDecimal grossAmount) {
    this.grossAmount = grossAmount;
  }

  /** 返回预计到货日期。 */
  public LocalDate getExpectedDate() {
    return expectedDate;
  }

  /** 设置预计到货日期。 */
  public void setExpectedDate(LocalDate expectedDate) {
    this.expectedDate = expectedDate;
  }

  /** 返回行备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置行备注。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 返回创建人。 */
  public String getCreateBy() {
    return createBy;
  }

  /** 设置创建人。 */
  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  /** 返回创建时间。 */
  public LocalDateTime getCreateTime() {
    return createTime;
  }

  /** 设置创建时间。 */
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  /** 返回修改人。 */
  public String getUpdateBy() {
    return updateBy;
  }

  /** 设置修改人。 */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  /** 返回修改时间。 */
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  /** 设置修改时间。 */
  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

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

  /** 返回待审核入库数量。 */
  public BigDecimal getPendingReceiptQuantity() {
    return pendingReceiptQuantity;
  }

  /** 设置待审核入库数量。 */
  public void setPendingReceiptQuantity(BigDecimal pendingReceiptQuantity) {
    this.pendingReceiptQuantity = pendingReceiptQuantity;
  }

  /** 返回累计已入库数量。 */
  public BigDecimal getReceivedQuantity() {
    return receivedQuantity;
  }

  /** 设置累计已入库数量。 */
  public void setReceivedQuantity(BigDecimal receivedQuantity) {
    this.receivedQuantity = receivedQuantity;
  }

  /** 返回已到货未入库数量（含待审核入库）。 */
  public BigDecimal getArrivedNotReceivedQuantity() {
    return arrivedNotReceivedQuantity;
  }

  /** 设置已到货未入库数量（含待审核入库）。 */
  public void setArrivedNotReceivedQuantity(BigDecimal arrivedNotReceivedQuantity) {
    this.arrivedNotReceivedQuantity = arrivedNotReceivedQuantity;
  }
}
