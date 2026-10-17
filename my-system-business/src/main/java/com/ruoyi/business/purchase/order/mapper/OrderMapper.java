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
}
