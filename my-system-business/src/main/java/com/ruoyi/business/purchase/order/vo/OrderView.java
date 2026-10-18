package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.ReceiptProgress;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 订单对外响应，金额用十进制字符串传输。 */
public class OrderView {
  /** 以订单各行订购数量判断的入库进度。 */
  private ReceiptProgress receiptProgress = ReceiptProgress.NOT_RECEIVED;

  /** 与审批状态独立的行级到货进度。 */
  private ArrivalProgress arrivalProgress = ArrivalProgress.NOT_ARRIVED;

  /** 返回到货执行进度。 */
  public ArrivalProgress getArrivalProgress() {
    return arrivalProgress;
  }

  /** 设置到货执行进度。 */
  public void setArrivalProgress(ArrivalProgress arrivalProgress) {
    this.arrivalProgress = arrivalProgress;
  }

  /** 订单 ID。 */
  private Long orderId;

  /** 系统订单号。 */
  private String orderNo;

  /** 订单日期。 */
  private LocalDate orderDate;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 供应商编码快照。 */
  private String supplierCode;

  /** 供应商名称快照。 */
  private String supplierName;

  /** 联系人快照。 */
  private String contactName;

  /** 联系电话快照。 */
  private String contactPhone;

  /** 联系地址快照。 */
  private String address;

  /** 采购员 ID。 */
  private Long buyerId;

  /** 采购员姓名快照。 */
  private String buyerName;

  /** 采购部门 ID。 */
  private Long deptId;

  /** 采购部门名称快照。 */
  private String deptName;

  /** 数量小数位。 */
  private Integer quantityDecimalPlaces;

  /** 单价小数位。 */
  private Integer priceDecimalPlaces;

  /** 金额小数位。 */
  private Integer amountDecimalPlaces;

  /** 舍入方式。 */
  private String roundingMode;

  /** 币种。 */
  private String currency;

  /** 单价计价方式。 */
  private PriceTaxMode priceTaxMode;

  /** 单据状态。 */
  private OrderStatus status;

  /** 明细行数。 */
  private Integer lineCount;

  /** 未税金额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal netAmount;

  /** 税额。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal tax;

  /** 价税合计。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal grossAmount;

  /** 备注。 */
  private String remark;

  /** 并发版本。 */
  private Integer version;

  /** 提交人。 */
  private String submittedBy;

  /** 提交时间。 */
  private LocalDateTime submittedTime;

  /** 审核人。 */
  private String reviewedBy;

  /** 审核时间。 */
  private LocalDateTime reviewedTime;

  /** 最近审核意见。 */
  private String reviewRemark;

  /** 关闭人。 */
  private String closedBy;

  /** 关闭时间。 */
  private LocalDateTime closedTime;

  /** 关闭说明。 */
  private String closeReason;

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 详情明细，列表不装载。 */
  private List<OrderLineView> lines;

  /** 返回订单 ID。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置订单 ID。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回系统订单号。 */
  public String getOrderNo() {
    return orderNo;
  }

  /** 设置系统订单号。 */
  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  /** 返回订单日期。 */
  public LocalDate getOrderDate() {
    return orderDate;
  }

  /** 设置订单日期。 */
  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  /** 返回供应商 ID。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商 ID。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回供应商编码快照。 */
  public String getSupplierCode() {
    return supplierCode;
  }

  /** 设置供应商编码快照。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode;
  }

  /** 返回供应商名称快照。 */
  public String getSupplierName() {
    return supplierName;
  }

  /** 设置供应商名称快照。 */
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  /** 返回联系人快照。 */
  public String getContactName() {
    return contactName;
  }

  /** 设置联系人快照。 */
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /** 返回联系电话快照。 */
  public String getContactPhone() {
    return contactPhone;
  }

  /** 设置联系电话快照。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  /** 返回联系地址快照。 */
  public String getAddress() {
    return address;
  }

  /** 设置联系地址快照。 */
  public void setAddress(String address) {
    this.address = address;
  }

  /** 返回采购员 ID。 */
  public Long getBuyerId() {
    return buyerId;
  }

  /** 设置采购员 ID。 */
  public void setBuyerId(Long buyerId) {
    this.buyerId = buyerId;
  }

  /** 返回采购员姓名快照。 */
  public String getBuyerName() {
    return buyerName;
  }

  /** 设置采购员姓名快照。 */
  public void setBuyerName(String buyerName) {
    this.buyerName = buyerName;
  }

  /** 返回采购部门 ID。 */
  public Long getDeptId() {
    return deptId;
  }

  /** 设置采购部门 ID。 */
  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  /** 返回采购部门名称快照。 */
  public String getDeptName() {
    return deptName;
  }

  /** 设置采购部门名称快照。 */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  /** 返回数量小数位。 */
  public Integer getQuantityDecimalPlaces() {
    return quantityDecimalPlaces;
  }

  /** 设置数量小数位。 */
  public void setQuantityDecimalPlaces(Integer quantityDecimalPlaces) {
    this.quantityDecimalPlaces = quantityDecimalPlaces;
  }

  /** 返回单价小数位。 */
  public Integer getPriceDecimalPlaces() {
    return priceDecimalPlaces;
  }

  /** 设置单价小数位。 */
  public void setPriceDecimalPlaces(Integer priceDecimalPlaces) {
    this.priceDecimalPlaces = priceDecimalPlaces;
  }

  /** 返回金额小数位。 */
  public Integer getAmountDecimalPlaces() {
    return amountDecimalPlaces;
  }

  /** 设置金额小数位。 */
  public void setAmountDecimalPlaces(Integer amountDecimalPlaces) {
    this.amountDecimalPlaces = amountDecimalPlaces;
  }

  /** 返回舍入方式。 */
  public String getRoundingMode() {
    return roundingMode;
  }

  /** 设置舍入方式。 */
  public void setRoundingMode(String roundingMode) {
    this.roundingMode = roundingMode;
  }

  /** 返回币种。 */
  public String getCurrency() {
    return currency;
  }

  /** 设置币种。 */
  public void setCurrency(String currency) {
    this.currency = currency;
  }

  /** 返回单价计价方式。 */
  public PriceTaxMode getPriceTaxMode() {
    return priceTaxMode;
  }

  /** 设置单价计价方式。 */
  public void setPriceTaxMode(PriceTaxMode priceTaxMode) {
    this.priceTaxMode = priceTaxMode;
  }

  /** 返回单据状态。 */
  public OrderStatus getStatus() {
    return status;
  }

  /** 设置单据状态。 */
  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  /** 返回明细行数。 */
  public Integer getLineCount() {
    return lineCount;
  }

  /** 设置明细行数。 */
  public void setLineCount(Integer lineCount) {
    this.lineCount = lineCount;
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

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 返回并发版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置并发版本。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回提交人。 */
  public String getSubmittedBy() {
    return submittedBy;
  }

  /** 设置提交人。 */
  public void setSubmittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
  }

  /** 返回提交时间。 */
  public LocalDateTime getSubmittedTime() {
    return submittedTime;
  }

  /** 设置提交时间。 */
  public void setSubmittedTime(LocalDateTime submittedTime) {
    this.submittedTime = submittedTime;
  }

  /** 返回审核人。 */
  public String getReviewedBy() {
    return reviewedBy;
  }

  /** 设置审核人。 */
  public void setReviewedBy(String reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  /** 返回审核时间。 */
  public LocalDateTime getReviewedTime() {
    return reviewedTime;
  }

  /** 设置审核时间。 */
  public void setReviewedTime(LocalDateTime reviewedTime) {
    this.reviewedTime = reviewedTime;
  }

  /** 返回最近审核意见。 */
  public String getReviewRemark() {
    return reviewRemark;
  }

  /** 设置最近审核意见。 */
  public void setReviewRemark(String reviewRemark) {
    this.reviewRemark = reviewRemark;
  }

  /** 返回关闭人。 */
  public String getClosedBy() {
    return closedBy;
  }

  /** 设置关闭人。 */
  public void setClosedBy(String closedBy) {
    this.closedBy = closedBy;
  }

  /** 返回关闭时间。 */
  public LocalDateTime getClosedTime() {
    return closedTime;
  }

  /** 设置关闭时间。 */
  public void setClosedTime(LocalDateTime closedTime) {
    this.closedTime = closedTime;
  }

  /** 返回关闭说明。 */
  public String getCloseReason() {
    return closeReason;
  }

  /** 设置关闭说明。 */
  public void setCloseReason(String closeReason) {
    this.closeReason = closeReason;
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

  /** 返回详情明细，列表不装载。 */
  public List<OrderLineView> getLines() {
    return lines;
  }

  /** 设置详情明细，列表不装载。 */
  public void setLines(List<OrderLineView> lines) {
    this.lines = lines;
  }

  /** 显式映射快照与业务字段，不暴露内部删除标记。 */
  public static OrderView from(PurchaseOrder entity) {
    OrderView result = new OrderView();
    result.setOrderId(entity.getOrderId());
    result.setOrderNo(entity.getOrderNo());
    result.setOrderDate(entity.getOrderDate());
    result.setSupplierId(entity.getSupplierId());
    result.setSupplierCode(entity.getSupplierCode());
    result.setSupplierName(entity.getSupplierName());
    result.setContactName(entity.getContactName());
    result.setContactPhone(entity.getContactPhone());
    result.setAddress(entity.getAddress());
    result.setBuyerId(entity.getBuyerId());
    result.setBuyerName(entity.getBuyerName());
    result.setDeptId(entity.getDeptId());
    result.setDeptName(entity.getDeptName());
    result.setQuantityDecimalPlaces(entity.getQuantityDecimalPlaces());
    result.setPriceDecimalPlaces(entity.getPriceDecimalPlaces());
    result.setAmountDecimalPlaces(entity.getAmountDecimalPlaces());
    result.setRoundingMode(entity.getRoundingMode());
    result.setCurrency(entity.getCurrency());
    result.setPriceTaxMode(entity.getPriceTaxMode());
    result.setStatus(entity.getStatus());
    result.setLineCount(entity.getLineCount());
    result.setNetAmount(entity.getNetAmount());
    result.setTax(entity.getTax());
    result.setGrossAmount(entity.getGrossAmount());
    result.setRemark(entity.getRemark());
    result.setVersion(entity.getVersion());
    result.setSubmittedBy(entity.getSubmittedBy());
    result.setSubmittedTime(entity.getSubmittedTime());
    result.setReviewedBy(entity.getReviewedBy());
    result.setReviewedTime(entity.getReviewedTime());
    result.setReviewRemark(entity.getReviewRemark());
    result.setClosedBy(entity.getClosedBy());
    result.setClosedTime(entity.getClosedTime());
    result.setCloseReason(entity.getCloseReason());
    result.setCreateBy(entity.getCreateBy());
    result.setCreateTime(entity.getCreateTime());
    result.setUpdateBy(entity.getUpdateBy());
    result.setUpdateTime(entity.getUpdateTime());
    return result;
  }

  /** 返回以订单各行订购数量判断的入库进度。 */
  public ReceiptProgress getReceiptProgress() {
    return receiptProgress;
  }

  /** 设置以订单各行订购数量判断的入库进度。 */
  public void setReceiptProgress(ReceiptProgress receiptProgress) {
    this.receiptProgress = receiptProgress;
  }
}
