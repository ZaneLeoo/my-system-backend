package com.ruoyi.business.masterdata.supplier.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.enums.EnableStatus;

/** 供应商档案实体，继承公共审计与逻辑删除字段。 */
@TableName("md_supplier")
public class Supplier extends BusinessEntity {
  /** 供应商 ID。 */
  @TableId(type = IdType.AUTO)
  private Long supplierId;

  /** 供应商编码。 */
  private String supplierCode;

  /** 供应商名称。 */
  private String supplierName;

  /** 简称。 */
  private String shortName;

  /** 主要联系人。 */
  private String contactName;

  /** 联系电话。 */
  private String contactPhone;

  /** 地址。 */
  private String address;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;

  /** 返回供应商 ID。 */
  public Long getSupplierId() {
    return supplierId;
  }

  /** 设置供应商 ID。 */
  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  /** 返回供应商编码。 */
  public String getSupplierCode() {
    return supplierCode;
  }

  /** 设置供应商编码。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode;
  }

  /** 返回供应商名称。 */
  public String getSupplierName() {
    return supplierName;
  }

  /** 设置供应商名称。 */
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  /** 返回简称。 */
  public String getShortName() {
    return shortName;
  }

  /** 设置简称。 */
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  /** 返回主要联系人。 */
  public String getContactName() {
    return contactName;
  }

  /** 设置主要联系人。 */
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /** 返回联系电话。 */
  public String getContactPhone() {
    return contactPhone;
  }

  /** 设置联系电话。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  /** 返回地址。 */
  public String getAddress() {
    return address;
  }

  /** 设置地址。 */
  public void setAddress(String address) {
    this.address = address;
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

  /** 返回编辑版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置编辑版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }
}
