package com.ruoyi.business.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.inventory.domain.InventoryTransaction;

/** 库存持久化，仅由记账服务写入。 */
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {}
