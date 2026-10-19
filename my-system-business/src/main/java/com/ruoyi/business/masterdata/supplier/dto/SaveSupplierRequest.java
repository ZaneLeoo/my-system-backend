package com.ruoyi.business.masterdata.supplier.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 供应商保存参数，不接受客户端审计与删除标记。 */
@Getter
@Setter
public class SaveSupplierRequest {
  /** 供应商 ID，新增时不指定。 */
  @Positive private Long supplierId;

  /** 供应商编码。 */
  @NotBlank(message = "供应商编码不能为空")
  @Size(max = 64, message = "供应商编码最多 64 个字符")
  private String supplierCode;

  /** 供应商名称。 */
  @NotBlank(message = "供应商名称不能为空")
  @Size(max = 200, message = "供应商名称最多 200 个字符")
  private String supplierName;

  /** 简称。 */
  @Size(max = 100, message = "简称最多 100 个字符")
  private String shortName = "";

  /** 主要联系人。 */
  @Size(max = 64, message = "主要联系人最多 64 个字符")
  private String contactName = "";

  /** 联系电话。 */
  @Size(max = 64, message = "联系电话最多 64 个字符")
  private String contactPhone = "";

  /** 地址。 */
  @Size(max = 500, message = "地址最多 500 个字符")
  private String address = "";

  /** 启停状态。 */
  @NotNull(message = "供应商状态不能为空")
  private EnableStatus status;

  /** 备注。 */
  @Size(max = 500, message = "备注最多 500 个字符")
  private String remark = "";

  /** 编辑版本号。 */
  @PositiveOrZero private Integer version;

  /** 设置供应商编码。 */
  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode == null ? null : supplierCode.strip();
  }

  /** 设置供应商名称。 */
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName == null ? null : supplierName.strip();
  }

  /** 设置简称。 */
  public void setShortName(String shortName) {
    this.shortName = shortName == null ? "" : shortName.strip();
  }

  /** 设置主要联系人。 */
  public void setContactName(String contactName) {
    this.contactName = contactName == null ? "" : contactName.strip();
  }

  /** 设置联系电话。 */
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone == null ? "" : contactPhone.strip();
  }

  /** 设置地址。 */
  public void setAddress(String address) {
    this.address = address == null ? "" : address.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
