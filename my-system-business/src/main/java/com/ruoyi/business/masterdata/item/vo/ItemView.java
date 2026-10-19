package com.ruoyi.business.masterdata.item.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 料品档案ItemView。 */
@Getter
@Setter
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
}
