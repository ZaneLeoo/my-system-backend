package com.ruoyi.business.purchase.order.dto;

import com.ruoyi.business.purchase.order.domain.PriceTaxMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 整单保存参数，主表与明细在同一事务保存。 */
@Getter
@Setter
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

  /** 设置联系人。 */
  public void setContactName(String contactName) {
    this.contactName = contactName == null ? "" : contactName.strip();
  }

  /** 设置联系电话。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone == null ? "" : contactPhone.strip();
  }

  /** 设置联系地址。 */
  public void setAddress(String address) {
    this.address = address == null ? "" : address.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
