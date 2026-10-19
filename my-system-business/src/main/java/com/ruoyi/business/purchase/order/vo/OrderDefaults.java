package com.ruoyi.business.purchase.order.vo;

import com.ruoyi.business.purchase.order.domain.*;
import lombok.Getter;
import lombok.Setter;

/** 当前账号的默认采购归属。 */
@Getter
@Setter
public class OrderDefaults {
  /** 数量小数位。 */
  private Integer quantityDecimalPlaces;

  /** 单价小数位。 */
  private Integer priceDecimalPlaces;

  /** 金额小数位。 */
  private Integer amountDecimalPlaces;

  /** 舍入方式。 */
  private String roundingMode;

  /** 采购员 ID。 */
  private Long buyerId;

  /** 采购员名称。 */
  private String buyerName;

  /** 采购部门 ID。 */
  private Long deptId;

  /** 采购部门名称。 */
  private String deptName;
}
