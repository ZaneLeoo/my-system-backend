package com.ruoyi.business.masterdata.supplier.vo;

import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.common.enums.EnableStatus;
import lombok.Getter;
import lombok.Setter;

/** 供应商参照，只提供选择与单据回填需要的字段。 */
@Getter
@Setter
public class SupplierOption {
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
