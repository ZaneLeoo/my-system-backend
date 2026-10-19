package com.ruoyi.business.masterdata.reference.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.common.enums.EnableStatus;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** 料品选择与单据回填的最小信息，不暴露审计及删除标记。 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ItemReference {
  /** itemId 的参照值。 */
  private Long itemId;

  /** itemCode 的参照值。 */
  private String itemCode;

  /** itemName 的参照值。 */
  private String itemName;

  /** specification 的参照值。 */
  private String specification;

  /** categoryId 的参照值。 */
  private Long categoryId;

  /** categoryName 的参照值。 */
  private String categoryName;

  /** categoryPath 的参照值。 */
  private String categoryPath;

  /** baseUnitId 的参照值。 */
  private Long baseUnitId;

  /** baseUnitName 的参照值。 */
  private String baseUnitName;

  /** purchaseUnitId 的参照值。 */
  private Long purchaseUnitId;

  /** purchaseUnitName 的参照值。 */
  private String purchaseUnitName;

  /** salesUnitId 的参照值。 */
  private Long salesUnitId;

  /** salesUnitName 的参照值。 */
  private String salesUnitName;

  /** purchaseEnabled 的参照值。 */
  private Boolean purchaseEnabled;

  /** defaultTaxRate 的参照值。 */
  private BigDecimal defaultTaxRate;

  /** status 的参照值。 */
  private EnableStatus status;

  /** 从查询结果显式映射允许返回的字段。 */
  public static ItemReference from(ItemView view) {
    ItemReference result = new ItemReference();
    result.setItemId(view.getItemId());
    result.setItemCode(view.getItemCode());
    result.setItemName(view.getItemName());
    result.setSpecification(view.getSpecification());
    result.setCategoryId(view.getCategoryId());
    result.setCategoryName(view.getCategoryName());
    result.setCategoryPath(view.getCategoryPath());
    result.setBaseUnitId(view.getBaseUnitId());
    result.setBaseUnitName(view.getBaseUnitName());
    result.setPurchaseUnitId(view.getPurchaseUnitId());
    result.setPurchaseUnitName(view.getPurchaseUnitName());
    result.setSalesUnitId(view.getSalesUnitId());
    result.setSalesUnitName(view.getSalesUnitName());
    result.setPurchaseEnabled(view.getPurchaseEnabled());
    result.setDefaultTaxRate(view.getDefaultTaxRate());
    result.setStatus(view.getStatus());
    return result;
  }
}
