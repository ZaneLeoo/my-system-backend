package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 整单草稿保存参数 */
@Getter
@Setter
public class SaveReceiptRequest {
  /** 已有入库单 ID。 */
  @Positive private Long receiptId;

  /** 旧页面版本。 */
  @PositiveOrZero private Integer version;

  /** 实际入库日期。 */
  @NotNull private LocalDate receiptDate;

  /** 供应商 ID。 */
  @NotNull @Positive private Long supplierId;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 入库明细。 */
  @NotEmpty
  @Size(max = ReceiptRules.MAX_LINES)
  private List<@NotNull @Valid ReceiptLineInput> lines;

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
