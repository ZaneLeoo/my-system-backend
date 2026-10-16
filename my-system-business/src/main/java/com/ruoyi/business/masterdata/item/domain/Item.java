package com.ruoyi.business.masterdata.item.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;

/** 料品档案Item。 */
@TableName("md_item")
public class Item extends BusinessEntity {
  /** 料品 ID。 */
  @TableId(type = IdType.AUTO)
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
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Long purchaseUnitId;

  /** 默认销售单位 ID。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Long salesUnitId;

  /** 允许采购。 */
  private Boolean purchaseEnabled;

  /** 默认采购税率，百分数。 */
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private BigDecimal defaultTaxRate;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 并发版本号。 */
  private Integer version;

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
}
