package com.ruoyi.business.inventory.mapper;

import com.ruoyi.business.inventory.dto.InventoryQuery;
import com.ruoyi.business.inventory.vo.*;
import java.util.List;
import org.apache.ibatis.annotations.*;

/** 库存只读查询，不过滤停用档案，避免历史库存失联。 */
public interface InventoryReadMapper {
  /**
   * 当前余额分页及数量排序。
   *
   * @param q 业务筛选、分页及白名单排序条件
   * @param keyword 已转义的料品编码、名称包含匹配条件
   * @return 符合条件的库存列表
   */
  @Select({
    "<script>",
    "SELECT b.*,w.warehouse_code,w.warehouse_name,l.location_code,l.location_name,i.item_code,i.item_name,i.specification,u.unit_name FROM inventory_balance b JOIN md_warehouse w ON w.warehouse_id=b.warehouse_id LEFT JOIN md_location l ON l.location_id=b.location_id JOIN md_item i ON i.item_id=b.item_id JOIN md_unit u ON u.unit_id=b.unit_id WHERE 1=1",
    "<if test='q.keyword != null'>AND (i.item_code LIKE CONCAT('%',#{keyword},'%') OR i.item_name LIKE CONCAT('%',#{keyword},'%'))</if>",
    "<if test='q.warehouseCode != null'>AND w.warehouse_code=#{q.warehouseCode}</if>",
    "<if test='q.locationCode != null'>AND l.location_code=#{q.locationCode}</if>",
    "<if test='q.warehouseId != null'>AND b.warehouse_id=#{q.warehouseId}</if>",
    "<if test='q.itemId != null'>AND b.item_id=#{q.itemId}</if>",
    "<if test='q.locationId != null'>AND b.location_id=#{q.locationId}</if>",
    "<if test='q.withoutLocation == true'>AND b.location_id IS NULL</if>",
    "<if test='q.onlyPositive == true'>AND b.quantity>0</if>",
    "ORDER BY <if test='q.sortField == &quot;quantity&quot;'> b.quantity <choose><when test='q.sortDirection == &quot;desc&quot;'>DESC</when><otherwise>ASC</otherwise></choose>,</if>w.warehouse_code,l.location_code,i.item_code,b.balance_id",
    "</script>"
  })
  List<BalanceView> balances(@Param("q") InventoryQuery q, @Param("keyword") String keyword);

  /**
   * 流水查询全部使用历史快照，实际记账顺序稳定。
   *
   * @param q 业务筛选、分页及白名单排序条件
   * @param keyword 已转义的料品编码、名称包含匹配条件
   * @param receiptNo 已转义的入库单号包含匹配条件
   * @return 符合条件的库存列表
   */
  @Select({
    "<script>",
    "SELECT b.* FROM inventory_transaction b WHERE 1=1",
    "<if test='q.keyword != null'>AND (b.item_code LIKE CONCAT('%',#{keyword},'%') OR b.item_name LIKE CONCAT('%',#{keyword},'%'))</if>",
    "<if test='q.warehouseCode != null'>AND b.warehouse_code=#{q.warehouseCode}</if>",
    "<if test='q.locationCode != null'>AND b.location_code=#{q.locationCode}</if>",
    "<if test='q.warehouseId != null'>AND b.warehouse_id=#{q.warehouseId}</if>",
    "<if test='q.itemId != null'>AND b.item_id=#{q.itemId}</if>",
    "<if test='q.locationId != null'>AND b.location_id=#{q.locationId}</if>",
    "<if test='q.withoutLocation == true'>AND b.location_id IS NULL</if>",
    "<if test='q.receiptNo != null'>AND b.receipt_no LIKE CONCAT('%',#{receiptNo},'%')</if>",
    "<if test='q.businessType != null'>AND b.business_type=#{q.businessType}</if>",
    "<if test='q.beginTime != null'>AND b.posted_time &gt;=#{q.beginTime}</if>",
    "<if test='q.endTime != null'>AND b.posted_time &lt;=#{q.endTime}</if>",
    "ORDER BY b.posted_time <choose><when test='q.sortDirection == &quot;asc&quot;'>ASC</when><otherwise>DESC</otherwise></choose>,b.transaction_id <choose><when test='q.sortDirection == &quot;asc&quot;'>ASC</when><otherwise>DESC</otherwise></choose>",
    "</script>"
  })
  List<TransactionView> transactions(
      @Param("q") InventoryQuery q,
      @Param("keyword") String keyword,
      @Param("receiptNo") String receiptNo);
}
