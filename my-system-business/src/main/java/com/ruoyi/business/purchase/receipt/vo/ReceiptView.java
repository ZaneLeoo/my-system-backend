package com.ruoyi.business.purchase.receipt.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 入库单快照响应 */
@Getter
@Setter
public class ReceiptView {
  /** 实际入库仓库快照摘要。 */
  private List<String> warehouseNames;

  /** 入库单 ID。 */
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
  private String submittedBy;

  /** 提交时间。 */
  private LocalDateTime submittedTime;

  /** 最近审核账号。 */
  private String reviewedBy;

  /** 最近审核时间。 */
  private LocalDateTime reviewedTime;

  /** 作废账号。 */
  private String voidedBy;

  /** 作废时间。 */
  private LocalDateTime voidedTime;

  /** 最近审核意见。 */
  private String reviewRemark;

  /** 作废原因。 */
  private String voidReason;

  /** 创建账号。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改账号。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 整单明细，列表不加载。 */
  private List<ReceiptLineView> lines;

  /** 显式映射业务快照，不返回内部删除标记。 */
  public static ReceiptView from(PurchaseReceipt entity) {
    ReceiptView result = new ReceiptView();
    result.setReceiptId(entity.getReceiptId());
    result.setReceiptNo(entity.getReceiptNo());
    result.setReceiptDate(entity.getReceiptDate());
    result.setSupplierId(entity.getSupplierId());
    result.setSupplierCode(entity.getSupplierCode());
    result.setSupplierName(entity.getSupplierName());
    result.setReceiverId(entity.getReceiverId());
    result.setReceiverName(entity.getReceiverName());
    result.setDeptId(entity.getDeptId());
    result.setDeptName(entity.getDeptName());
    result.setStatus(entity.getStatus());
    result.setLineCount(entity.getLineCount());
    result.setRemark(entity.getRemark());
    result.setVersion(entity.getVersion());
    result.setSubmittedBy(entity.getSubmittedBy());
    result.setSubmittedTime(entity.getSubmittedTime());
    result.setReviewedBy(entity.getReviewedBy());
    result.setReviewedTime(entity.getReviewedTime());
    result.setVoidedBy(entity.getVoidedBy());
    result.setVoidedTime(entity.getVoidedTime());
    result.setReviewRemark(entity.getReviewRemark());
    result.setVoidReason(entity.getVoidReason());
    result.setCreateBy(entity.getCreateBy());
    result.setCreateTime(entity.getCreateTime());
    result.setUpdateBy(entity.getUpdateBy());
    result.setUpdateTime(entity.getUpdateTime());
    return result;
  }
}
