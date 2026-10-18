package com.ruoyi.business.purchase.receipt.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/** 整单草稿保存参数 */
public class SaveReceiptRequest {
  /** 已有入库单 ID。 */
  @Positive private Long receiptId;

  /** 旧页面版本。 */
  @PositiveOrZero private Integer version;

  /** 实际入库日期。 */
  @NotNull private LocalDate receiptDate;

  /** 供应商 ID。 */
  @NotNull @Positive private Long supplierId;

  /** 收货仓库 ID。 */
  @NotNull @Positive private Long warehouseId;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 入库明细。 */
  @NotEmpty
  @Size(max = ReceiptRules.MAX_LINES)
  private List<@NotNull @Valid ReceiptLineInput> lines;

  /** 返回已有入库单 ID。 */
  public Long getReceiptId() {
    return receiptId;
  }

  /** 设置已有入库单 ID。 */
  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  /** 返回旧页面版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置旧页面版本。 */
  public void setVersion(Integer version) {
    this.version = version;
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

  /** 返回收货仓库 ID。 */
  public Long getWarehouseId() {
    return warehouseId;
  }

  /** 设置收货仓库 ID。 */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回入库明细。 */
  public List<@NotNull @Valid ReceiptLineInput> getLines() {
    return lines;
  }

  /** 设置入库明细。 */
  public void setLines(List<@NotNull @Valid ReceiptLineInput> lines) {
    this.lines = lines;
  }
}
