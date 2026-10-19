package com.ruoyi.business.purchase.receipt.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** ArrivalLineReference 的显式数据约定。 */
@Getter
@Setter
public class ArrivalLineReference {
  /** 到货单 ID。 */
  private Long arrivalId;

  /** 到货行 ID。 */
  private Long arrivalLineId;

  /** 供应商 ID。 */
  private Long supplierId;

  /** 仓库 ID。 */
  private Long warehouseId;

  /** 来源订单 ID。 */
  private Long orderId;

  /** 来源订单行 ID。 */
  private Long orderLineId;

  /** 料品 ID。 */
  private Long itemId;

  /** 单位 ID。 */
  private Long unitId;

  /** 到货单号。 */
  private String arrivalNo;

  /** 供应商编码。 */
  private String supplierCode;

  /** 供应商名称。 */
  private String supplierName;

  /** 仓库编码。 */
  private String warehouseCode;

  /** 仓库名称。 */
  private String warehouseName;

  /** 订单号。 */
  private String orderNo;

  /** 料品编码。 */
  private String itemCode;

  /** 料品名称。 */
  private String itemName;

  /** 规格。 */
  private String specification;

  /** 单位名称。 */
  private String unitName;

  /** 是否使用货位。 */
  private Boolean locationEnabled;

  /** 到货行号。 */
  private Integer arrivalLineNo;

  /** 订单行号。 */
  private Integer orderLineNo;

  /** 数量精度。 */
  private Integer quantityDecimalPlaces;

  /** 到货日期。 */
  private LocalDate arrivalDate;

  /** 到货数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal arrivalQuantity;

  /** 待审核入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal pendingReceiptQuantity;

  /** 已入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal receivedQuantity;

  /** 可入库数量。 */
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal availableQuantity;
}
