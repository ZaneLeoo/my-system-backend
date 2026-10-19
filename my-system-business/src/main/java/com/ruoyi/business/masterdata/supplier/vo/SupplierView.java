package com.ruoyi.business.masterdata.supplier.vo;

import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 供应商列表与详情响应。 */
@Getter
@Setter
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
