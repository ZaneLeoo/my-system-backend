package com.ruoyi.business.purchase.receipt.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 采购入库单表头；实际仓库和货位由明细确定 */
@TableName("purchase_receipt")
public class PurchaseReceipt extends BusinessEntity {
  /** 入库单 ID。 */
  @TableId(type = IdType.AUTO)
  private Long receiptId;

  /** 入库单号。 */
  private String receiptNo;

  /** 实际入库日期。 */
  private LocalDate receiptDate;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 供应商编码快照。 */
  private String supplierCode;

  /** 供应商名称快照。 */
  private String supplierName;

  /** 收货经办人 ID。 */
  private Long receiverId;

  /** 经办人姓名快照。 */
  private String receiverName;

  /** 经办部门 ID。 */
  private Long deptId;

  /** 经办部门名称快照。 */
  private String deptName;

  /** 单据状态。 */
  private ReceiptStatus status;

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

  /** 返回入库单 ID。 */
  public Long getReceiptId() {
    return receiptId;
  }

  /** 设置入库单 ID。 */
  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  /** 返回入库单号。 */
  public String getReceiptNo() {
    return receiptNo;
  }

  /** 设置入库单号。 */
  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo;
  }

  /** 返回实际入库日期。 */
  public LocalDate getReceiptDate() {
    return receiptDate;
  }

  /** 设置实际入库日期。 */
  public void setReceiptDate(LocalDate receiptDate) {
    this.receiptDate = receiptDate;
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
  public ReceiptStatus getStatus() {
    return status;
  }

  /** 设置单据状态。 */
  public void setStatus(ReceiptStatus status) {
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
