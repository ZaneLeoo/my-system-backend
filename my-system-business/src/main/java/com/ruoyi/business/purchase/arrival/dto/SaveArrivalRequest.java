package com.ruoyi.business.purchase.arrival.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 整单草稿保存参数 */
@Getter
@Setter
public class SaveArrivalRequest {
  /** 已有到货单 ID。 */
  @Positive private Long arrivalId;

  /** 旧页面版本。 */
  @PositiveOrZero private Integer version;

  /** 实际到货日期。 */
  @NotNull private LocalDate arrivalDate;

  /** 供应商 ID。 */
  @NotNull @Positive private Long supplierId;

  /** 预计入库仓库 ID。 */
  @Positive private Long warehouseId;

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

  /** 设置供应商送货单号。 */
  public void setSupplierDeliveryNo(String supplierDeliveryNo) {
    this.supplierDeliveryNo = supplierDeliveryNo == null ? "" : supplierDeliveryNo.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
