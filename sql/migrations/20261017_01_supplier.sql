-- 增量初始化，可重复执行；不得替代若依全量初始化脚本。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS md_supplier (
  supplier_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商档案ID',
  supplier_code VARCHAR(64) NOT NULL COMMENT '供应商编码，删除后仍保留占用',
  supplier_name VARCHAR(200) NOT NULL COMMENT '供应商名称，允许重复',
  short_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '简称',
  contact_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '主要联系人',
  contact_phone VARCHAR(64) NOT NULL DEFAULT '' COMMENT '联系电话',
  address VARCHAR(500) NOT NULL DEFAULT '' COMMENT '地址',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '编辑版本号，防止旧页面覆盖',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  create_by VARCHAR(64) NOT NULL COMMENT '创建账号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT NULL COMMENT '最后修改账号',
  update_time DATETIME DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (supplier_id),
  UNIQUE KEY uk_md_supplier_code (supplier_code),
  KEY idx_md_supplier_name (supplier_name),
  KEY idx_md_supplier_status (del_flag, status, supplier_code),
  CONSTRAINT chk_md_supplier_status CHECK (status IN ('0', '1')),
  CONSTRAINT chk_md_supplier_deleted CHECK (del_flag IN ('0', '2')),
  CONSTRAINT chk_md_supplier_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商档案';

START TRANSACTION;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, create_time)
SELECT '基础资料', 0, 5, 'masterdata', NULL, 'M', '0', '0', 'component', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata');
SET @masterdata_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '供应商档案', @masterdata_id, 4, 'supplier', 'masterdata/supplier/index', 'MasterDataSupplier', 'C', '0', '0', 'masterdata:supplier:list', 'list', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'masterdata:supplier:list');
SET @supplier_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'masterdata:supplier:list' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, create_by, create_time)
SELECT definitions.menu_name, @supplier_menu_id, definitions.sort_order, '#', 'F', definitions.perms, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
FROM (
  SELECT '供应商查询' AS menu_name, 1 AS sort_order, 'masterdata:supplier:query' AS perms
  UNION ALL SELECT '供应商新增', 2, 'masterdata:supplier:add'
  UNION ALL SELECT '供应商修改', 3, 'masterdata:supplier:edit'
  UNION ALL SELECT '供应商删除', 4, 'masterdata:supplier:remove'
  UNION ALL SELECT '供应商参照', 20, 'masterdata:supplier:reference'
) AS definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms = definitions.perms);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id = @masterdata_id OR menu_id = @supplier_menu_id OR parent_id = @supplier_menu_id)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id = 1 AND assigned.menu_id = sys_menu.menu_id);
COMMIT;
