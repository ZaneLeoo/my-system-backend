package com.ruoyi.business.masterdata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 基础资料引用检查；不依赖料品 Service，避免循环依赖。 */
public interface MasterDataReferenceMapper {
  /** 查询分类被未删除料品引用的数量，停用时只检查启用料品。 */
  @Select(
      "SELECT COUNT(*) FROM md_item WHERE category_id=#{id} AND del_flag='0' AND (#{enabledOnly}=false OR status='0')")
  long categoryItems(@Param("id") Long id, @Param("enabledOnly") boolean enabledOnly);

  /** 三种单位角色任一被引用都纳入约束。 */
  @Select(
      "SELECT COUNT(*) FROM md_item WHERE (base_unit_id=#{id} OR purchase_unit_id=#{id} OR sales_unit_id=#{id}) AND del_flag='0' AND (#{enabledOnly}=false OR status='0')")
  long unitItems(@Param("id") Long id, @Param("enabledOnly") boolean enabledOnly);

  /** 查询订单和到货的供应商引用，未完成范围包含草稿及待入库到货。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_order WHERE supplier_id=#{id} AND del_flag='0' AND (#{openOnly}=false OR status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival a WHERE a.supplier_id=#{id} AND a.del_flag='0' AND (#{openOnly}=false OR (a.status IN ('DRAFT','SUBMITTED') OR (a.status='APPROVED' AND EXISTS(SELECT 1 FROM purchase_arrival_line pending WHERE pending.arrival_id=a.arrival_id AND pending.del_flag='0' AND pending.received_quantity<pending.quantity))))) + (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line rl ON rl.receipt_id=r.receipt_id WHERE r.supplier_id=#{id} AND r.del_flag='0' AND rl.del_flag='0' AND (#{openOnly}=false OR r.status IN ('DRAFT','SUBMITTED')))")
  long supplierPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 查询有效订单及到货中的料品引用，来源关闭后仍检查待入库到货。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.item_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival_line l JOIN purchase_arrival a ON a.arrival_id=l.arrival_id WHERE l.item_id=#{id} AND l.del_flag='0' AND a.del_flag='0' AND (#{openOnly}=false OR (a.status IN ('DRAFT','SUBMITTED') OR (a.status='APPROVED' AND l.received_quantity<l.quantity)))) + (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line rl ON rl.receipt_id=r.receipt_id WHERE rl.item_id=#{id} AND r.del_flag='0' AND rl.del_flag='0' AND (#{openOnly}=false OR r.status IN ('DRAFT','SUBMITTED'))) + (SELECT COUNT(*) FROM inventory_transaction it WHERE it.item_id=#{id} AND #{openOnly}=false)")
  long itemPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 单位即使不再被当前档案引用，也不能删除历史采购单据引用。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.unit_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival_line l JOIN purchase_arrival a ON a.arrival_id=l.arrival_id WHERE l.unit_id=#{id} AND l.del_flag='0' AND a.del_flag='0' AND (#{openOnly}=false OR (a.status IN ('DRAFT','SUBMITTED') OR (a.status='APPROVED' AND l.received_quantity<l.quantity)))) + (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line rl ON rl.receipt_id=r.receipt_id WHERE rl.unit_id=#{id} AND r.del_flag='0' AND rl.del_flag='0' AND (#{openOnly}=false OR r.status IN ('DRAFT','SUBMITTED'))) + (SELECT COUNT(*) FROM inventory_transaction it WHERE it.unit_id=#{id} AND #{openOnly}=false)")
  long unitPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 已审核尚待入库的到货单仍占用仓库，作废单保留历史删除约束。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_arrival a WHERE a.warehouse_id=#{id} AND a.del_flag='0' AND (#{openOnly}=false OR (a.status IN ('DRAFT','SUBMITTED') OR (a.status='APPROVED' AND EXISTS(SELECT 1 FROM purchase_arrival_line pending WHERE pending.arrival_id=a.arrival_id AND pending.del_flag='0' AND pending.received_quantity<pending.quantity))))) + (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line rl ON rl.receipt_id=r.receipt_id WHERE rl.warehouse_id=#{id} AND r.del_flag='0' AND rl.del_flag='0' AND (#{openOnly}=false OR r.status IN ('DRAFT','SUBMITTED'))) + (SELECT COUNT(*) FROM inventory_transaction it WHERE it.warehouse_id=#{id} AND #{openOnly}=false)")
  long warehouseArrivals(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 未作废下游入库单（包括草稿）阻止来源到货作废。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line l ON l.receipt_id=r.receipt_id WHERE l.arrival_id=#{id} AND r.del_flag='0' AND l.del_flag='0' AND r.status!='VOIDED'")
  long arrivalReceipts(@Param("id") Long id);

  /** 正库存阻止仓库停用。 */
  @Select("SELECT COUNT(*) FROM inventory_balance WHERE warehouse_id=#{id} AND quantity>0")
  long warehouseStock(@Param("id") Long id);

  /** 正库存阻止料品停用，但不单独限制采购开关。 */
  @Select("SELECT COUNT(*) FROM inventory_balance WHERE item_id=#{id} AND quantity>0")
  long itemStock(@Param("id") Long id);

  /** 正库存阻止库存单位停用。 */
  @Select("SELECT COUNT(*) FROM inventory_balance WHERE unit_id=#{id} AND quantity>0")
  long unitStock(@Param("id") Long id);

  /** 货位未完成业务及正库存约束，历史业务在删除时继续占用。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line l ON l.receipt_id=r.receipt_id WHERE l.location_id=#{id} AND r.del_flag='0' AND l.del_flag='0' AND (#{openOnly}=false OR r.status IN ('DRAFT','SUBMITTED'))) + (SELECT COUNT(*) FROM inventory_balance WHERE location_id=#{id} AND quantity>0) + (SELECT COUNT(*) FROM inventory_transaction WHERE location_id=#{id} AND #{openOnly}=false)")
  long locationReferences(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 已保存单据或库存历史固定仓库货位管理方式。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_receipt r JOIN purchase_receipt_line l ON l.receipt_id=r.receipt_id WHERE l.warehouse_id=#{id} AND r.del_flag='0' AND l.del_flag='0') + (SELECT COUNT(*) FROM inventory_transaction WHERE warehouse_id=#{id})")
  long warehouseModeReferences(@Param("id") Long id);
}
