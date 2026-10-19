package com.ruoyi.business.query;

import com.ruoyi.query.spi.MetadataTableProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 收录现有基础资料、采购及库存业务表，排除系统敏感表和内部事务锁表。 */
@Component
public class BusinessMetadataProvider implements MetadataTableProvider {
  /** 返回业务目录清单；新增表需显式登记，查询接入仍由独立适配器负责。 */
  @Override
  public Map<String, String> tables() {
    return Map.ofEntries(
        Map.entry("md_item_category", "料品分类"),
        Map.entry("md_unit", "计量单位"),
        Map.entry("md_item", "料品档案"),
        Map.entry("md_supplier", "供应商档案"),
        Map.entry("md_warehouse", "仓库档案"),
        Map.entry("md_location", "货位档案"),
        Map.entry("purchase_order", "采购订单"),
        Map.entry("purchase_order_line", "采购订单明细"),
        Map.entry("purchase_arrival", "采购到货单"),
        Map.entry("purchase_arrival_line", "采购到货明细"),
        Map.entry("purchase_receipt", "采购入库单"),
        Map.entry("purchase_receipt_line", "采购入库明细"),
        Map.entry("inventory_balance", "库存余额"),
        Map.entry("inventory_transaction", "库存流水"));
  }
}
