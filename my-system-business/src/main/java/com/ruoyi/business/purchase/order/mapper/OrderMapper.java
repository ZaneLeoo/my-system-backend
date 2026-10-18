package com.ruoyi.business.purchase.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.purchase.order.domain.PurchaseOrder;
import com.ruoyi.business.purchase.order.vo.OrderDefaults;
import org.apache.ibatis.annotations.*;

/** 订单主表访问，默认 MP 查询过滤逻辑删除。 */
public interface OrderMapper extends BaseMapper<PurchaseOrder> {
  /** 当前登录员工及部门均需有效，不开放完整人员档案。 */
  @Select(
      "SELECT u.user_id AS buyerId, COALESCE(NULLIF(u.nick_name,''), u.user_name) AS buyerName, d.dept_id AS deptId, d.dept_name AS deptName FROM sys_user u JOIN sys_dept d ON d.dept_id=u.dept_id WHERE u.user_id=#{id} AND u.dept_id=#{deptId} AND u.status='0' AND u.del_flag='0' AND d.status='0' AND d.del_flag='0'")
  OrderDefaults defaults(@Param("id") Long id, @Param("deptId") Long deptId);

  /** 读取已存在的系统参数，直接使用参数管理保存的值。 */
  @Select("SELECT config_value FROM sys_config WHERE config_key=#{key} ORDER BY config_id LIMIT 1")
  String configValue(@Param("key") String key);

  /** 关闭前检查仍在准备或待审核的到货单，调用方已持分类事务锁。 */
  @Select(
      "SELECT COUNT(*) FROM purchase_arrival_line l JOIN purchase_arrival a ON a.arrival_id=l.arrival_id WHERE l.order_id=#{id} AND l.del_flag='0' AND a.del_flag='0' AND a.status IN ('DRAFT','SUBMITTED')")
  long unfinishedArrivals(@Param("id") Long id);

  /** 一次批量读取当前页各单的行级到货进度，不加总不同计量单位。 */
  @Select({
    "<script>",
    "SELECT order_id,CASE WHEN MAX(arrived_quantity)=0 THEN 'NOT_ARRIVED' WHEN MIN(CASE WHEN arrived_quantity=quantity THEN 1 ELSE 0 END)=1 THEN 'FULLY_ARRIVED' ELSE 'PARTIALLY_ARRIVED' END AS arrival_progress FROM purchase_order_line WHERE del_flag='0' AND order_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY order_id",
    "</script>"
  })
  java.util.List<com.ruoyi.business.purchase.order.vo.OrderArrivalProgress> arrivalProgress(
      @Param("ids") java.util.List<Long> ids);
}
