package com.ruoyi.business.purchase.arrival.mapper;

import com.ruoyi.business.purchase.arrival.dto.OrderLineReferenceQuery;
import com.ruoyi.business.purchase.arrival.vo.OrderLineReference;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 单次联查来源订单行，不向参照用户开放订单金额及内部字段。 */
public interface OrderLineReferenceMapper {
  String FIELDS =
      "SELECT l.line_id AS order_line_id,l.order_id,o.order_no,l.line_no AS order_line_no,o.order_date,o.supplier_id,l.item_id,l.item_code,l.item_name,l.specification,l.unit_id,l.unit_name,l.expected_date,o.quantity_decimal_places,l.quantity AS order_quantity,l.pending_arrival_quantity,l.arrived_quantity,(l.quantity-l.pending_arrival_quantity-l.arrived_quantity) AS available_quantity FROM purchase_order_line l JOIN purchase_order o ON o.order_id=l.order_id ";
  String ELIGIBLE =
      " WHERE o.del_flag='0' AND l.del_flag='0' AND o.status='APPROVED' AND l.quantity>l.pending_arrival_quantity+l.arrived_quantity ";

  /**
   * 分页条件全部使用绑定参数，排序由静态 SQL 分支选择。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @param keyword 已转义的料品编码、名称包含匹配条件
   * @return 符合条件的采购订单列表
   */
  @Select({
    "<script>",
    FIELDS,
    ELIGIBLE,
    "AND o.supplier_id=#{query.supplierId}",
    "<if test='query.orderId != null'>AND o.order_id=#{query.orderId}</if>",
    "<if test='keyword != null'>AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR l.item_code LIKE CONCAT('%',#{keyword},'%') OR l.item_name LIKE CONCAT('%',#{keyword},'%') OR l.specification LIKE CONCAT('%',#{keyword},'%'))</if>",
    "ORDER BY <choose><when test='query.sortField == &quot;itemCode&quot;'>l.item_code</when><when test='query.sortField == &quot;itemName&quot;'>l.item_name</when><otherwise>o.order_no</otherwise></choose>",
    "<choose><when test='query.sortDirection == &quot;desc&quot;'>DESC</when><otherwise>ASC</otherwise></choose>,o.order_id,l.line_no,l.line_id",
    "</script>"
  })
  List<OrderLineReference> search(
      @Param("query") OrderLineReferenceQuery query, @Param("keyword") String keyword);

  /**
   * 确认前复核最多一百个来源行，已关闭或无可用额度的行不再返回。
   *
   * @param ids 需要批量回显的记录 ID 集合
   * @return 匹配的未删除记录，包含允许回显的停用旧值
   */
  @Select({
    "<script>",
    FIELDS,
    ELIGIBLE,
    "AND l.line_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"
  })
  List<OrderLineReference> resolve(@Param("ids") List<Long> ids);
}
