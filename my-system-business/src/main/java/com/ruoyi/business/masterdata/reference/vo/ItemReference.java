package com.ruoyi.business.masterdata.reference.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;

/** 料品选择与单据回填的最小信息，不暴露审计及删除标记。 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ItemReference {
  /** itemId 的参照值。 */
  private Long itemId;

  /** itemCode 的参照值。 */
  private String itemCode;

  /** itemName 的参照值。 */
  private String itemName;

  /** specification 的参照值。 */
  private String specification;

  /** categoryId 的参照值。 */
  private Long categoryId;

  /** categoryName 的参照值。 */
  private String categoryName;

  /** categoryPath 的参照值。 */
  private String categoryPath;

  /** baseUnitId 的参照值。 */
  private Long baseUnitId;

  /** baseUnitName 的参照值。 */
  private String baseUnitName;

  /** purchaseUnitId 的参照值。 */
  private Long purchaseUnitId;

  /** purchaseUnitName 的参照值。 */
  private String purchaseUnitName;

  /** salesUnitId 的参照值。 */
  private Long salesUnitId;

  /** salesUnitName 的参照值。 */
  private String salesUnitName;

  /** purchaseEnabled 的参照值。 */
  private Boolean purchaseEnabled;

  /** defaultTaxRate 的参照值。 */
  private BigDecimal defaultTaxRate;

  /** status 的参照值。 */
  private EnableStatus status;

  /** 从查询结果显式映射允许返回的字段。 */
  public static ItemReference from(ItemView view) {
    ItemReference result = new ItemReference();
    result.setItemId(view.getItemId());
    result.setItemCode(view.getItemCode());
    result.setItemName(view.getItemName());
    result.setSpecification(view.getSpecification());
    result.setCategoryId(view.getCategoryId());
    result.setCategoryName(view.getCategoryName());
    result.setCategoryPath(view.getCategoryPath());
    result.setBaseUnitId(view.getBaseUnitId());
    result.setBaseUnitName(view.getBaseUnitName());
    result.setPurchaseUnitId(view.getPurchaseUnitId());
    result.setPurchaseUnitName(view.getPurchaseUnitName());
    result.setSalesUnitId(view.getSalesUnitId());
    result.setSalesUnitName(view.getSalesUnitName());
    result.setPurchaseEnabled(view.getPurchaseEnabled());
    result.setDefaultTaxRate(view.getDefaultTaxRate());
    result.setStatus(view.getStatus());
    return result;
  }

  /** 返回 itemId。 */
  public Long getItemId() {
    return itemId;
  }

  /** 设置 itemId。 */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /** 返回 itemCode。 */
  public String getItemCode() {
    return itemCode;
  }

  /** 设置 itemCode。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /** 返回 itemName。 */
  public String getItemName() {
    return itemName;
  }

  /** 设置 itemName。 */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /** 返回 specification。 */
  public String getSpecification() {
    return specification;
  }

  /** 设置 specification。 */
  public void setSpecification(String specification) {
    this.specification = specification;
  }

  /** 返回 categoryId。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置 categoryId。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回 categoryName。 */
  public String getCategoryName() {
    return categoryName;
  }

  /** 设置 categoryName。 */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  /** 返回 categoryPath。 */
  public String getCategoryPath() {
    return categoryPath;
  }

  /** 设置 categoryPath。 */
  public void setCategoryPath(String categoryPath) {
    this.categoryPath = categoryPath;
  }

  /** 返回 baseUnitId。 */
  public Long getBaseUnitId() {
    return baseUnitId;
  }

  /** 设置 baseUnitId。 */
  public void setBaseUnitId(Long baseUnitId) {
    this.baseUnitId = baseUnitId;
  }

  /** 返回 baseUnitName。 */
  public String getBaseUnitName() {
    return baseUnitName;
  }

  /** 设置 baseUnitName。 */
  public void setBaseUnitName(String baseUnitName) {
    this.baseUnitName = baseUnitName;
  }

  /** 返回 purchaseUnitId。 */
  public Long getPurchaseUnitId() {
    return purchaseUnitId;
  }

  /** 设置 purchaseUnitId。 */
  public void setPurchaseUnitId(Long purchaseUnitId) {
    this.purchaseUnitId = purchaseUnitId;
  }

  /** 返回 purchaseUnitName。 */
  public String getPurchaseUnitName() {
    return purchaseUnitName;
  }

  /** 设置 purchaseUnitName。 */
  public void setPurchaseUnitName(String purchaseUnitName) {
    this.purchaseUnitName = purchaseUnitName;
  }

  /** 返回 salesUnitId。 */
  public Long getSalesUnitId() {
    return salesUnitId;
  }

  /** 设置 salesUnitId。 */
  public void setSalesUnitId(Long salesUnitId) {
    this.salesUnitId = salesUnitId;
  }

  /** 返回 salesUnitName。 */
  public String getSalesUnitName() {
    return salesUnitName;
  }

  /** 设置 salesUnitName。 */
  public void setSalesUnitName(String salesUnitName) {
    this.salesUnitName = salesUnitName;
  }

  /** 返回 purchaseEnabled。 */
  public Boolean getPurchaseEnabled() {
    return purchaseEnabled;
  }

  /** 设置 purchaseEnabled。 */
  public void setPurchaseEnabled(Boolean purchaseEnabled) {
    this.purchaseEnabled = purchaseEnabled;
  }

  /** 返回 defaultTaxRate。 */
  public BigDecimal getDefaultTaxRate() {
    return defaultTaxRate;
  }

  /** 设置 defaultTaxRate。 */
  public void setDefaultTaxRate(BigDecimal defaultTaxRate) {
    this.defaultTaxRate = defaultTaxRate;
  }

  /** 返回 status。 */
  public EnableStatus getStatus() {
    return status;
  }

  /** 设置 status。 */
  public void setStatus(EnableStatus status) {
    this.status = status;
  }
}
