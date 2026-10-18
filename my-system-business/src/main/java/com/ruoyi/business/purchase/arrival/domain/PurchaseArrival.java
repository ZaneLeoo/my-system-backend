package com.ruoyi.business.purchase.arrival.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 采购到货单，审核后确认到货但不产生库存 */
@TableName("purchase_arrival")
public class PurchaseArrival extends BusinessEntity {
  /** 到货单 ID。 */
  @TableId(type = IdType.AUTO)
  private Long arrivalId;

  /** 到货单号。 */
  private String arrivalNo;

  /** 实际到货日期。 */
  private LocalDate arrivalDate;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 供应商编码快照。 */
  private String supplierCode;

  /** 供应商名称快照。 */
  private String supplierName;

  /** 供应商送货单号。 */
  private String supplierDeliveryNo;

  /** 收货仓库 ID。 */
  private Long warehouseId;

  /** 仓库编码快照。 */
  private String warehouseCode;

  /** 仓库名称快照。 */
  private String warehouseName;

  /** 收货经办人 ID。 */
  private Long receiverId;

  /** 经办人姓名快照。 */
  private String receiverName;

  /** 经办部门 ID。 */
  private Long deptId;

  /** 经办部门名称快照。 */
  private String deptName;

  /** 单据状态。 */
  private ArrivalStatus status;

  /** 明细行数。 */
  private Integer lineCount;

  /** 备注。 */
  private String remark;

  /** 整单版本。 */
  private Integer version;

  /** 提交账号。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String submittedBy;

  /** 提交时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime submittedTime;

  /** 最近审核账号。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String reviewedBy;

  /** 最近审核时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime reviewedTime;

  /** 作废账号。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String voidedBy;

  /** 作废时间。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private LocalDateTime voidedTime;

  /** 最近审核意见。 */
  private String reviewRemark;

  /** 作废原因。 */
  private String voidReason;

  /** 返回到货单 ID。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置到货单 ID。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回到货单号。 */
  public String getArrivalNo() {
    return arrivalNo;
  }

  /** 设置到货单号。 */
  public void setArrivalNo(String arrivalNo) {
    this.arrivalNo = arrivalNo;
  }

  /** 返回实际到货日期。 */
  public LocalDate getArrivalDate() {
    return arrivalDate;
  }

  /** 设置实际到货日期。 */
  public void setArrivalDate(LocalDate arrivalDate) {
    this.arrivalDate = arrivalDate;
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

  /** 返回供应商送货单号。 */
  public String getSupplierDeliveryNo() {
    return supplierDeliveryNo;
  }

  /** 设置供应商送货单号。 */
  public void setSupplierDeliveryNo(String supplierDeliveryNo) {
    this.supplierDeliveryNo = supplierDeliveryNo;
  }

  /** 返回收货仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置收货仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回仓库编码快照。 */
  public String getWarehouseCode() {
    return warehouseCode;
  }

  /** 设置仓库编码快照。 */
  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode;
  }

  /** 返回仓库名称快照。 */
  public String getWarehouseName() {
    return warehouseName;
  }

  /** 设置仓库名称快照。 */
  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
  }

  /** 返回收货经办人 ID。 */
  public Long getReceiverId() {
    return receiverId;
  }

  /** 设置收货经办人 ID。 */
  public void setReceiverId(Long receiverId) {
    this.receiverId = receiverId;
  }

  /** 返回经办人姓名快照。 */
  public String getReceiverName() {
    return receiverName;
  }

  /** 设置经办人姓名快照。 */
  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  /** 返回经办部门 ID。 */
  public Long getDeptId() {
    return deptId;
  }

  /** 设置经办部门 ID。 */
  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  /** 返回经办部门名称快照。 */
  public String getDeptName() {
    return deptName;
  }

  /** 设置经办部门名称快照。 */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  /** 返回单据状态。 */
  public ArrivalStatus getStatus() {
    return status;
  }

  /** 设置单据状态。 */
  public void setStatus(ArrivalStatus status) {
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

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 返回整单版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置整单版本。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回提交账号。 */
  public String getSubmittedBy() {
    return submittedBy;
  }

  /** 设置提交账号。 */
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

  /** 返回最近审核账号。 */
  public String getReviewedBy() {
    return reviewedBy;
  }

  /** 设置最近审核账号。 */
  public void setReviewedBy(String reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  /** 返回最近审核时间。 */
  public LocalDateTime getReviewedTime() {
    return reviewedTime;
  }

  /** 设置最近审核时间。 */
  public void setReviewedTime(LocalDateTime reviewedTime) {
    this.reviewedTime = reviewedTime;
  }

  /** 返回作废账号。 */
  public String getVoidedBy() {
    return voidedBy;
  }

  /** 设置作废账号。 */
  public void setVoidedBy(String voidedBy) {
    this.voidedBy = voidedBy;
  }

  /** 返回作废时间。 */
  public LocalDateTime getVoidedTime() {
    return voidedTime;
  }

  /** 设置作废时间。 */
  public void setVoidedTime(LocalDateTime voidedTime) {
    this.voidedTime = voidedTime;
  }

  /** 返回最近审核意见。 */
  public String getReviewRemark() {
    return reviewRemark;
  }

  /** 设置最近审核意见。 */
  public void setReviewRemark(String reviewRemark) {
    this.reviewRemark = reviewRemark;
  }

  /** 返回作废原因。 */
  public String getVoidReason() {
    return voidReason;
  }

  /** 设置作废原因。 */
  public void setVoidReason(String voidReason) {
    this.voidReason = voidReason;
  }
}
