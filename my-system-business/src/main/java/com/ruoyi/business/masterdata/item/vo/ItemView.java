package com.ruoyi.business.masterdata.item.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 料品档案ItemView。 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ItemView {
  /** 料品 ID。 */
  private Long itemId;

  /** 料品编码。 */
  private String itemCode;

  /** 料品名称。 */
  private String itemName;

  /** 规格型号。 */
  private String specification;

  /** 分类 ID。 */
  private Long categoryId;

  /** 库存基本单位 ID。 */
  private Long baseUnitId;

  /** 默认采购单位 ID。 */
  private Long purchaseUnitId;

  /** 默认销售单位 ID。 */
  private Long salesUnitId;

  /** 允许采购。 */
  private Boolean purchaseEnabled;

  /** 默认采购税率，百分数。 */
  private BigDecimal defaultTaxRate;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 并发版本号。 */
  private Integer version;

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 分类名称。 */
  private String categoryName;

  /** 分类路径。 */
  private String categoryPath;

  /** 基本单位名称。 */
  private String baseUnitName;

  /** 采购单位名称。 */
  private String purchaseUnitName;

  /** 销售单位名称。 */
  private String salesUnitName;

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
    this.itemCode = itemCode;
  }

  /** 返回料品名称。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回规格型号。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置规格型号。 */
  public void setSpecification(String specification) {
    this.specification = specification;
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
    this.remark = remark;
  }

  /** 返回并发版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置并发版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回创建人。 */
  public String getCreateBy() {
    return createBy;
  }

  /** 设置创建人。 */
  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  /** 返回创建时间。 */
  public LocalDateTime getCreateTime() {
    return createTime;
  }

  /** 设置创建时间。 */
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  /** 返回修改人。 */
  public String getUpdateBy() {
    return updateBy;
  }

  /** 设置修改人。 */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  /** 返回修改时间。 */
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  /** 设置修改时间。 */
  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  /** 返回分类名称。 */
  public String getCategoryName() {
    return categoryName;
  }

  /** 设置分类名称。 */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  /** 返回分类路径。 */
  public String getCategoryPath() {
    return categoryPath;
  }

  /** 设置分类路径。 */
  public void setCategoryPath(String categoryPath) {
    this.categoryPath = categoryPath;
  }

  /** 返回基本单位名称。 */
  public String getBaseUnitName() {
    return baseUnitName;
  }

  /** 设置基本单位名称。 */
  public void setBaseUnitName(String baseUnitName) {
    this.baseUnitName = baseUnitName;
  }

  /** 返回采购单位名称。 */
  public String getPurchaseUnitName() {
    return purchaseUnitName;
  }

  /** 设置采购单位名称。 */
  public void setPurchaseUnitName(String purchaseUnitName) {
    this.purchaseUnitName = purchaseUnitName;
  }

  /** 返回销售单位名称。 */
  public String getSalesUnitName() {
    return salesUnitName;
  }

  /** 设置销售单位名称。 */
  public void setSalesUnitName(String salesUnitName) {
    this.salesUnitName = salesUnitName;
  }
}
