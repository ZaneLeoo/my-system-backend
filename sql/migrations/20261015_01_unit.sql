-- 增量初始化，可重复执行；不得替代若依全量初始化脚本。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS md_unit (
  unit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计量单位ID',
  unit_code VARCHAR(64) NOT NULL COMMENT '单位编码，删除后仍保留占用',
  unit_name VARCHAR(100) NOT NULL COMMENT '单位名称，删除后仍保留占用',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '编辑版本号，防止旧页面覆盖',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  create_by VARCHAR(64) NOT NULL COMMENT '创建账号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT NULL COMMENT '最后修改账号',
  update_time DATETIME DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (unit_id),
  UNIQUE KEY uk_md_unit_code (unit_code),
  UNIQUE KEY uk_md_unit_name (unit_name),
  KEY idx_md_unit_status (del_flag, status, unit_code),
  CONSTRAINT chk_md_unit_status CHECK (status IN ('0', '1')),
  CONSTRAINT chk_md_unit_deleted CHECK (del_flag IN ('0', '2')),
  CONSTRAINT chk_md_unit_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计量单位';

START TRANSACTION;
-- 显式填写 SQL 初始化操作者与北京时间；不覆盖人工维护或复活已删除单位。
INSERT INTO md_unit (unit_code, unit_name, status, remark, version, create_by, create_time, update_by, update_time)
SELECT definitions.code, definitions.name, '0', '', 0, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
FROM (
  SELECT 'PCS' AS code, '件' AS name
  UNION ALL SELECT 'EA', '个'
  UNION ALL SELECT 'BOX', '箱'
  UNION ALL SELECT 'KG', '千克'
  UNION ALL SELECT 'G', '克'
  UNION ALL SELECT 'T', '吨'
  UNION ALL SELECT 'M', '米'
  UNION ALL SELECT 'M2', '平方米'
  UNION ALL SELECT 'M3', '立方米'
  UNION ALL SELECT 'L', '升'
) AS definitions
WHERE NOT EXISTS (SELECT 1 FROM md_unit existing WHERE existing.unit_code = definitions.code OR existing.unit_name = definitions.name);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, create_time)
SELECT '基础资料', 0, 5, 'masterdata', NULL, 'M', '0', '0', 'component', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata');
SET @masterdata_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '计量单位', @masterdata_id, 2, 'unit', 'masterdata/unit/index', 'MasterDataUnit', 'C', '0', '0', 'masterdata:unit:list', 'list', 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'masterdata:unit:list');
SET @unit_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'masterdata:unit:list' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, create_by, create_time)
SELECT definitions.menu_name, @unit_menu_id, definitions.sort_order, '#', 'F', definitions.perms, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
FROM (
  SELECT '单位查询' AS menu_name, 1 AS sort_order, 'masterdata:unit:query' AS perms
  UNION ALL SELECT '单位新增', 2, 'masterdata:unit:add'
  UNION ALL SELECT '单位修改', 3, 'masterdata:unit:edit'
  UNION ALL SELECT '单位删除', 4, 'masterdata:unit:remove'
) AS definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms = definitions.perms);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id = @masterdata_id OR menu_id = @unit_menu_id OR parent_id = @unit_menu_id)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id = 1 AND assigned.menu_id = sys_menu.menu_id);
COMMIT;
