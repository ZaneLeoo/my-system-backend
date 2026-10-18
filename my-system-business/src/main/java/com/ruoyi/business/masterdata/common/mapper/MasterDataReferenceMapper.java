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
      "SELECT (SELECT COUNT(*) FROM purchase_order WHERE supplier_id=#{id} AND del_flag='0' AND (#{openOnly}=false OR status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival WHERE supplier_id=#{id} AND del_flag='0' AND (#{openOnly}=false OR status!='VOIDED'))")
  long supplierPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 查询有效订单及到货中的料品引用，来源关闭后仍检查待入库到货。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.item_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival_line l JOIN purchase_arrival a ON a.arrival_id=l.arrival_id WHERE l.item_id=#{id} AND l.del_flag='0' AND a.del_flag='0' AND (#{openOnly}=false OR a.status!='VOIDED'))")
  long itemPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 单位即使不再被当前档案引用，也不能删除历史采购单据引用。 */
  @Select(
      "SELECT (SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.unit_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')) + (SELECT COUNT(*) FROM purchase_arrival_line l JOIN purchase_arrival a ON a.arrival_id=l.arrival_id WHERE l.unit_id=#{id} AND l.del_flag='0' AND a.del_flag='0' AND (#{openOnly}=false OR a.status!='VOIDED'))")
  long unitPurchaseDocuments(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 已审核尚待入库的到货单仍占用仓库，作废单保留历史删除约束。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_arrival WHERE warehouse_id=#{id} AND del_flag='0' AND (#{openOnly}=false OR status!='VOIDED')")
  long warehouseArrivals(@Param("id") Long id, @Param("openOnly") boolean openOnly);
}
