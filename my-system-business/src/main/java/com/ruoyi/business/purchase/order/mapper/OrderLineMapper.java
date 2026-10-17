package com.ruoyi.business.purchase.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.purchase.order.domain.PurchaseOrderLine;

/** 明细持久化，行 ID 稳定，移除行执行实体逻辑删除。 */
public interface OrderLineMapper extends BaseMapper<PurchaseOrderLine> {}
