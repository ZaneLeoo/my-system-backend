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

  /** 查询未删除采购订单的供应商引用；未关闭范围包含草稿。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_order WHERE supplier_id=#{id} AND del_flag='0' AND (#{openOnly}=false OR status!='CLOSED')")
  long supplierOrders(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 查询有效采购主表与明细中的料品引用。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.item_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')")
  long itemOrders(@Param("id") Long id, @Param("openOnly") boolean openOnly);

  /** 单位即使不再被当前档案引用，也不能删除历史订单引用。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id WHERE l.unit_id=#{id} AND l.del_flag='0' AND o.del_flag='0' AND (#{openOnly}=false OR o.status!='CLOSED')")
  long unitOrders(@Param("id") Long id, @Param("openOnly") boolean openOnly);
}
