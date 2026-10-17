package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.business.purchase.order.domain.PriceTaxMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/** 整单保存参数，主表与明细在同一事务保存。 */
public class SaveOrderRequest {
  /** 订单 ID，新增不指定。 */
  @Positive private Long orderId;

  /** 旧页面版本。 */
  @PositiveOrZero private Integer version;

  /** 订单日期。 */
  @NotNull private LocalDate orderDate;

  /** 供应商 ID。 */
  @NotNull @Positive private Long supplierId;

  /** 计价方式。 */
  @NotNull private PriceTaxMode priceTaxMode;

  /** 联系人。 */
  @Size(max = 64)
  private String contactName = "";

  /** 联系电话。 */
  @Size(max = 64)
  private String contactPhone = "";

  /** 联系地址。 */
  @Size(max = 500)
  private String address = "";

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 订单明细。 */
  @NotEmpty
  @Size(max = 200)
  private List<@NotNull @Valid OrderLineInput> lines;

  /** 返回订单 ID，新增不指定。 */
  public Long getOrderId() {
    return orderId;
  }

  /** 设置订单 ID，新增不指定。 */
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  /** 返回旧页面版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置旧页面版本。 */
  public void setVersion(Integer version) {
    this.version = version;
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

  /** 返回计价方式。 */
  public PriceTaxMode getPriceTaxMode() {
    return priceTaxMode;
  }

  /** 设置计价方式。 */
  public void setPriceTaxMode(PriceTaxMode priceTaxMode) {
    this.priceTaxMode = priceTaxMode;
  }

  /** 返回联系人。 */
  public String getContactName() {
    return contactName;
  }

  /** 设置联系人。 */
  public void setContactName(String contactName) {
    this.contactName = contactName == null ? "" : contactName.strip();
  }

  /** 返回联系电话。 */
  public String getContactPhone() {
    return contactPhone;
  }

  /** 设置联系电话。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone == null ? "" : contactPhone.strip();
  }

  /** 返回联系地址。 */
  public String getAddress() {
    return address;
  }

  /** 设置联系地址。 */
  public void setAddress(String address) {
    this.address = address == null ? "" : address.strip();
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回订单明细。 */
  public List<@NotNull @Valid OrderLineInput> getLines() {
    return lines;
  }

  /** 设置订单明细。 */
  public void setLines(List<@NotNull @Valid OrderLineInput> lines) {
    this.lines = lines;
  }
}
