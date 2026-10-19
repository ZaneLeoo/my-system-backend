package com.ruoyi.business.purchase.arrival.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.BusinessEntity;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 采购到货单，审核后确认到货但不产生库存 */
@Getter
@Setter
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

  /** 预计入库仓库 ID。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
}
