package com.ruoyi.business.masterdata.item.dto;

import com.ruoyi.common.enums.EnableStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 料品档案SaveItemRequest。 */
@Getter
@Setter
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

  /** 设置料品编码。 */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode == null ? null : itemCode.strip();
  }

  /** 设置料品名称。 */
  public void setItemName(String itemName) {
    this.itemName = itemName == null ? null : itemName.strip();
  }

  /** 设置规格型号。 */
  public void setSpecification(String specification) {
    this.specification = specification == null ? "" : specification.strip();
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }
}
