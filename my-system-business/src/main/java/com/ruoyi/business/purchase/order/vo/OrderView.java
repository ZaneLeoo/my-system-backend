package com.ruoyi.business.purchase.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.purchase.arrival.domain.ReceiptProgress;
import com.ruoyi.business.purchase.order.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 订单对外响应，金额用十进制字符串传输。 */
@Getter
@Setter
public class OrderView {
  /** 以订单各行订购数量判断的入库进度。 */
  private ReceiptProgress receiptProgress = ReceiptProgress.NOT_RECEIVED;

  /** 与审批状态独立的行级到货进度。 */
  private ArrivalProgress arrivalProgress = ArrivalProgress.NOT_ARRIVED;

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
}
