package com.ruoyi.business.query;

import static org.junit.jupiter.api.Assertions.*;

import com.ruoyi.query.metadata.MetadataRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 业务目录只收录公开登记的基础资料和业务表。 */
class BusinessMetadataProviderTest {
  /** 14 张表都有中文名称，系统表和内部锁表不会随业务前缀自动收录。 */
  @Test
  void includesExistingBusinessScopeAndExcludesInternalTables() {
    BusinessMetadataProvider provider = new BusinessMetadataProvider();
    MetadataRegistry registry = new MetadataRegistry(List.of(provider));
    assertEquals(14, registry.tables().size());
    assertTrue(
        registry
            .tables()
            .containsAll(
                List.of(
                    "md_unit",
                    "md_item_category",
                    "purchase_receipt_line",
                    "inventory_transaction")));
    assertFalse(registry.tables().contains("md_item_category_lock"));
    assertFalse(registry.tables().contains("sys_user"));
    assertFalse(registry.tables().contains("sys_query_definition"));
    assertEquals("采购入库单", registry.displayName("purchase_receipt"));
    assertEquals("库存余额", registry.displayName("inventory_balance"));
  }
}
