package com.ruoyi.business.masterdata.supplier.vo;

import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.common.enums.EnableStatus;

/** 供应商参照，只提供选择与单据回填需要的字段。 */
public class SupplierOption {
  /** 供应商 ID。 */
  private Long supplierId;

  /** 返回供应商 ID。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商 ID。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 供应商编码。 */
  private String supplierCode;

  /** 返回供应商编码。 */
  public String getSupplierCode() {
    return supplierCode;
  }

  /** 设置供应商编码。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode;
  }

  /** 供应商名称。 */
  private String supplierName;

  /** 返回供应商名称。 */
  public String getSupplierName() {
    return supplierName;
  }

  /** 设置供应商名称。 */
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  /** 简称。 */
  private String shortName;

  /** 返回简称。 */
  public String getShortName() {
    return shortName;
  }

  /** 设置简称。 */
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  /** 主要联系人。 */
  private String contactName;

  /** 返回主要联系人。 */
  public String getContactName() {
    return contactName;
  }

  /** 设置主要联系人。 */
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /** 联系电话。 */
  private String contactPhone;

  /** 返回联系电话。 */
  public String getContactPhone() {
    return contactPhone;
  }

  /** 设置联系电话。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  /** 地址。 */
  private String address;

  /** 返回地址。 */
  public String getAddress() {
    return address;
  }

  /** 设置地址。 */
  public void setAddress(String address) {
    this.address = address;
  }

  /** 启停状态。 */
  private EnableStatus status;

  /** 返回启停状态。 */
  public EnableStatus getStatus() {
    return status;
  }

  /** 设置启停状态。 */
  public void setStatus(EnableStatus status) {
    this.status = status;
  }

  /** 显式映射参照字段，避免暴露内部持久化信息。 */
  public static SupplierOption from(Supplier entity) {
    SupplierOption result = new SupplierOption();
    result.setSupplierId(entity.getSupplierId());
    result.setSupplierCode(entity.getSupplierCode());
    result.setSupplierName(entity.getSupplierName());
    result.setShortName(entity.getShortName());
    result.setContactName(entity.getContactName());
    result.setContactPhone(entity.getContactPhone());
    result.setAddress(entity.getAddress());
    result.setStatus(entity.getStatus());
    return result;
  }
}
