-- 增量初始化；不重建数据、不预置演示料品。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS md_item (
  item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '料品ID',
  item_code VARCHAR(64) NOT NULL COMMENT '料品编码，删除后仍占用',
  item_name VARCHAR(200) NOT NULL COMMENT '料品名称，可重复',
  specification VARCHAR(200) NOT NULL DEFAULT '' COMMENT '规格型号',
  category_id BIGINT NOT NULL COMMENT '直接归属分类ID',
  base_unit_id BIGINT NOT NULL COMMENT '库存基本单位ID',
  purchase_unit_id BIGINT DEFAULT NULL COMMENT '默认采购单位ID，不隐含换算',
  sales_unit_id BIGINT DEFAULT NULL COMMENT '默认销售单位ID，不隐含换算',
  purchase_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '允许采购',
  default_tax_rate DECIMAL(5,2) DEFAULT NULL COMMENT '默认采购税率，百分数；NULL未设置',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '0启用，1停用',
  remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '并发版本号，非物料业务版本',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '0正常，2删除',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY(item_id),
  UNIQUE KEY uk_md_item_code(item_code),
  KEY idx_md_item_category(category_id, del_flag, status),
  KEY idx_md_item_status(del_flag, status, item_code),
  KEY idx_md_item_base_unit(base_unit_id, del_flag, status),
  KEY idx_md_item_purchase_unit(purchase_unit_id, del_flag, status),
  KEY idx_md_item_sales_unit(sales_unit_id, del_flag, status),
  CONSTRAINT chk_md_item_status CHECK(status IN ('0','1')),
  CONSTRAINT chk_md_item_deleted CHECK(del_flag IN ('0','2')),
  CONSTRAINT chk_md_item_version CHECK(version >= 0),
  CONSTRAINT chk_md_item_purchase CHECK(purchase_enabled IN (FALSE,TRUE)),
  CONSTRAINT chk_md_item_tax CHECK(default_tax_rate IS NULL OR default_tax_rate BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='料品档案';
START TRANSACTION;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, create_time)
SELECT '基础资料', 0, 5, 'masterdata', NULL, 'M', '0', '0', 'component', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata');
SET @masterdata_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '料品档案', @masterdata_id, 3, 'item', 'masterdata/item/index', 'MasterDataItem', 'C', '0', '0', 'masterdata:item:list', 'list', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'masterdata:item:list');
SET @item_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'masterdata:item:list' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, create_by, create_time)
SELECT definitions.menu_name, @item_menu_id, definitions.sort_order, '#', 'F', definitions.perms, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
FROM (
  SELECT '料品查询' AS menu_name, 1 AS sort_order, 'masterdata:item:query' AS perms
  UNION ALL SELECT '料品新增', 2, 'masterdata:item:add'
  UNION ALL SELECT '料品修改', 3, 'masterdata:item:edit'
  UNION ALL SELECT '料品删除', 4, 'masterdata:item:remove'
) AS definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms = definitions.perms);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id = @masterdata_id OR menu_id = @item_menu_id OR parent_id = @item_menu_id)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id = 1 AND assigned.menu_id = sys_menu.menu_id);
COMMIT;
