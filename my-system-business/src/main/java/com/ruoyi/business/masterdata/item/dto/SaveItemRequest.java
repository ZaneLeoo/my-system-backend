package com.ruoyi.business.masterdata.item.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** 料品档案SaveItemRequest。 */
public class SaveItemRequest {
  /** 料品 ID。 */
  @Positive private Long itemId;

  /** 料品编码。 */
  @NotBlank
  @Size(max = 64)
  private String itemCode;

  /** 料品名称。 */
  @NotBlank
  @Size(max = 200)
  private String itemName;

  /** 规格型号。 */
  @Size(max = 200)
  private String specification = "";

  /** 分类 ID。 */
  @NotNull @Positive private Long categoryId;

  /** 库存基本单位 ID。 */
  @NotNull @Positive private Long baseUnitId;

  /** 默认采购单位 ID。 */
  @Positive private Long purchaseUnitId;

  /** 默认销售单位 ID。 */
  @Positive private Long salesUnitId;

  /** 允许采购。 */
  @NotNull private Boolean purchaseEnabled = true;

  /** 默认采购税率，百分数。 */
  @DecimalMin("0")
  @DecimalMax("100")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal defaultTaxRate;

  /** 启停状态。 */
  @NotNull private EnableStatus status;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 并发版本号。 */
  @PositiveOrZero private Integer version;

  /** 返回料品 ID。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置料品 ID。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回料品编码。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode == null ? null : itemCode.strip();
  }

  /** 返回料品名称。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName == null ? null : itemName.strip();
  }

  /** 返回规格型号。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格型号。 */
  public void setSpecification(String specification) {
    this.specification = specification == null ? "" : specification.strip();
  }

  /** 返回分类 ID。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置分类 ID。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回库存基本单位 ID。 */
  public Long getBaseUnitId() {
    return baseUnitId;
  }

  /** 设置库存基本单位 ID。 */
  public void setBaseUnitId(Long baseUnitId) {
    this.baseUnitId = baseUnitId;
  }

  /** 返回默认采购单位 ID。 */
  public Long getPurchaseUnitId() {
    return purchaseUnitId;
  }

  /** 设置默认采购单位 ID。 */
  public void setPurchaseUnitId(Long purchaseUnitId) {
    this.purchaseUnitId = purchaseUnitId;
  }

  /** 返回默认销售单位 ID。 */
  public Long getSalesUnitId() {
    return salesUnitId;
  }

  /** 设置默认销售单位 ID。 */
  public void setSalesUnitId(Long salesUnitId) {
    this.salesUnitId = salesUnitId;
  }

  /** 返回允许采购。 */
  public Boolean getPurchaseEnabled() {
    return purchaseEnabled;
  }

  /** 设置允许采购。 */
  public void setPurchaseEnabled(Boolean purchaseEnabled) {
    this.purchaseEnabled = purchaseEnabled;
  }

  /** 返回默认采购税率，百分数。 */
  public BigDecimal getDefaultTaxRate() {
    return defaultTaxRate;
  }

  /** 设置默认采购税率，百分数。 */
  public void setDefaultTaxRate(BigDecimal defaultTaxRate) {
    this.defaultTaxRate = defaultTaxRate;
  }

  /** 返回启停状态。 */
  public EnableStatus getStatus() {
    return status;
  }

  /** 设置启停状态。 */
  public void setStatus(EnableStatus status) {
    this.status = status;
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回并发版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置并发版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }
}
