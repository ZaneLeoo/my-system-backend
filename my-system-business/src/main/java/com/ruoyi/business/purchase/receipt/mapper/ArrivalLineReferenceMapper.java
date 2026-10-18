package com.ruoyi.business.purchase.receipt.mapper;

import com.ruoyi.business.purchase.receipt.dto.ArrivalLineReferenceQuery;
import com.ruoyi.business.purchase.receipt.vo.ArrivalLineReference;
import java.util.List;
import org.apache.ibatis.annotations.*;

/** 到货参照仅返回来源选择必需字段，不返回金额或审计。 */
public interface ArrivalLineReferenceMapper {
  String FIELDS =
      "SELECT a.arrival_id,l.line_id AS arrival_line_id,a.arrival_no,l.line_no AS arrival_line_no,a.arrival_date,a.supplier_id,a.supplier_code,a.supplier_name,a.warehouse_id,a.warehouse_code,a.warehouse_name,w.location_enabled,l.order_id,l.order_line_id,l.order_no,l.order_line_no,l.item_id,l.item_code,l.item_name,l.specification,l.unit_id,l.unit_name,l.quantity_decimal_places,l.quantity AS arrival_quantity,l.pending_receipt_quantity,l.received_quantity,(l.quantity-l.pending_receipt_quantity-l.received_quantity) AS available_quantity FROM purchase_arrival a JOIN purchase_arrival_line l ON a.arrival_id=l.arrival_id LEFT JOIN md_warehouse w ON w.warehouse_id=a.warehouse_id ";
  String ELIGIBLE =
      " WHERE a.del_flag='0' AND l.del_flag='0' AND a.status='APPROVED' AND l.quantity>l.pending_receipt_quantity+l.received_quantity ";

  /** 条件绑定且排序列由静态分支选择。 */
  @Select({
    "<script>",
    FIELDS,
    ELIGIBLE,
    "<if test='q.supplierId != null'>AND a.supplier_id=#{q.supplierId}</if>",
    "<if test='q.warehouseId != null'>AND a.warehouse_id=#{q.warehouseId}</if>",
    "<if test='q.arrivalId != null'>AND a.arrival_id=#{q.arrivalId}</if>",
    "<if test='keyword != null'>AND (a.arrival_no LIKE CONCAT('%',#{keyword},'%') OR l.item_code LIKE CONCAT('%',#{keyword},'%') OR l.item_name LIKE CONCAT('%',#{keyword},'%') OR l.specification LIKE CONCAT('%',#{keyword},'%'))</if>",
    "ORDER BY <choose><when test='q.sortField == &quot;itemCode&quot;'>l.item_code</when><when test='q.sortField == &quot;itemName&quot;'>l.item_name</when><otherwise>a.arrival_no</otherwise></choose> <choose><when test='q.sortDirection == &quot;desc&quot;'>DESC</when><otherwise>ASC</otherwise></choose>,a.arrival_id,l.line_no,l.line_id</script>"
  })
  List<ArrivalLineReference> search(
      @Param("q") ArrivalLineReferenceQuery q, @Param("keyword") String keyword);

  /** 确认前重新核验来源状态及剩余额度。 */
  @Select({
    "<script>",
    FIELDS,
    ELIGIBLE,
    "AND l.line_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"
  })
  List<ArrivalLineReference> resolve(@Param("ids") List<Long> ids);
}
