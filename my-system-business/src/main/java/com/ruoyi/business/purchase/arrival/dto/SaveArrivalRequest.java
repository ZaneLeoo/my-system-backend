package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/** 整单草稿保存参数 */
public class SaveArrivalRequest {
  /** 已有到货单 ID。 */
  @Positive private Long arrivalId;

  /** 旧页面版本。 */
  @PositiveOrZero private Integer version;

  /** 实际到货日期。 */
  @NotNull private LocalDate arrivalDate;

  /** 供应商 ID。 */
  @NotNull @Positive private Long supplierId;

  /** 收货仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 供应商送货单号。 */
  @Size(max = 100)
  private String supplierDeliveryNo = "";

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 到货明细。 */
  @NotEmpty
  @Size(max = ArrivalRules.MAX_LINES)
  private List<@NotNull @Valid ArrivalLineInput> lines;

  /** 返回已有到货单 ID。 */
  public Long getArrivalId() {
    return arrivalId;
  }

  /** 设置已有到货单 ID。 */
  public void setArrivalId(Long arrivalId) {
    this.arrivalId = arrivalId;
  }

  /** 返回旧页面版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置旧页面版本。 */
  public void setVersion(Integer version) {
    this.version = version;
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

  /** 返回收货仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置收货仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回供应商送货单号。 */
  public String getSupplierDeliveryNo() {
    return supplierDeliveryNo;
  }

  /** 设置供应商送货单号。 */
  public void setSupplierDeliveryNo(String supplierDeliveryNo) {
    this.supplierDeliveryNo = supplierDeliveryNo == null ? "" : supplierDeliveryNo.strip();
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回到货明细。 */
  public List<@NotNull @Valid ArrivalLineInput> getLines() {
    return lines;
  }

  /** 设置到货明细。 */
  public void setLines(List<@NotNull @Valid ArrivalLineInput> lines) {
    this.lines = lines;
  }
}
