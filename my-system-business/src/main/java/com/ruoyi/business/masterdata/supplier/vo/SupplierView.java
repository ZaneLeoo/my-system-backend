package com.ruoyi.business.masterdata.supplier.vo;

import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;

/** 供应商列表与详情响应。 */
public class SupplierView {
  /** 供应商 ID。 */
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

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

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

  /** 显式映射对外字段，避免返回内部删除标记。 */
  public static SupplierView from(Supplier entity) {
    SupplierView view = new SupplierView();
    view.setSupplierId(entity.getSupplierId());
    view.setSupplierCode(entity.getSupplierCode());
    view.setSupplierName(entity.getSupplierName());
    view.setShortName(entity.getShortName());
    view.setContactName(entity.getContactName());
    view.setContactPhone(entity.getContactPhone());
    view.setAddress(entity.getAddress());
    view.setStatus(entity.getStatus());
    view.setRemark(entity.getRemark());
    view.setVersion(entity.getVersion());
    view.setCreateBy(entity.getCreateBy());
    view.setCreateTime(entity.getCreateTime());
    view.setUpdateBy(entity.getUpdateBy());
    view.setUpdateTime(entity.getUpdateTime());
    return view;
  }
}
