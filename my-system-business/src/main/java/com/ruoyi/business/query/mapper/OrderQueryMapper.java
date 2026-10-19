package com.ruoyi.business.query.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ruoyi.business.purchase.order.domain.PurchaseOrder;
import java.util.List;
import org.apache.ibatis.annotations.*;

/** 静态主表和返回投影，只有编译器生成的 Wrapper 条件可变。 */
public interface OrderQueryMapper {
  /** 超时会传递到 PageHelper 生成的 count 查询。 */
  @Select("SELECT o.order_id FROM purchase_order o ${ew.customSqlSegment}")
  @Options(timeout = 3)
  List<Long> searchIds(@Param(Constants.WRAPPER) Wrapper<PurchaseOrder> wrapper);
}
