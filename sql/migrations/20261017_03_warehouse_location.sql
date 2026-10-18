-- 仓库与货位增量初始化，允许重复执行，不覆盖已有档案。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS md_warehouse (
  warehouse_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  warehouse_code VARCHAR(64) NOT NULL COMMENT '仓库编码',
  warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
  location_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否启用货位管理',
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  version INT NOT NULL DEFAULT 0,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (warehouse_id),
  UNIQUE KEY uk_md_warehouse_code (warehouse_code),
  KEY idx_md_warehouse_status (del_flag, status, warehouse_code),
  CONSTRAINT chk_md_warehouse_status CHECK (status IN ('0', '1')),
  CONSTRAINT chk_md_warehouse_deleted CHECK (del_flag IN ('0', '2')),
  CONSTRAINT chk_md_warehouse_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库档案';

CREATE TABLE IF NOT EXISTS md_location (
  location_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '货位ID',
  warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
  location_code VARCHAR(64) NOT NULL COMMENT '货位编码',
  location_name VARCHAR(100) NOT NULL COMMENT '货位名称',
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  version INT NOT NULL DEFAULT 0,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (location_id),
  UNIQUE KEY uk_md_location_code (warehouse_id, location_code),
  KEY idx_md_location_status (warehouse_id, del_flag, status, location_code),
  CONSTRAINT chk_md_location_status CHECK (status IN ('0', '1')),
  CONSTRAINT chk_md_location_deleted CHECK (del_flag IN ('0', '2')),
  CONSTRAINT chk_md_location_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货位档案';

START TRANSACTION;
SET @masterdata_id=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='masterdata' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '仓库档案',@masterdata_id,5,'warehouse','masterdata/warehouse/index','MasterDataWarehouse','C','0','0','masterdata:warehouse:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='masterdata:warehouse:list');
SET @warehouse_menu=(SELECT menu_id FROM sys_menu WHERE perms='masterdata:warehouse:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT d.title,@warehouse_menu,d.n,'#','F',d.perms,'admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR FROM (
SELECT '仓库查询' AS title,2 AS n,'masterdata:warehouse:query' AS perms
UNION ALL SELECT '仓库新增' AS title,3 AS n,'masterdata:warehouse:add' AS perms
UNION ALL SELECT '仓库修改' AS title,4 AS n,'masterdata:warehouse:edit' AS perms
UNION ALL SELECT '仓库删除' AS title,5 AS n,'masterdata:warehouse:remove' AS perms
UNION ALL SELECT '仓库参照' AS title,6 AS n,'masterdata:warehouse:reference' AS perms
UNION ALL SELECT '货位列表' AS title,11 AS n,'masterdata:location:list' AS perms
UNION ALL SELECT '货位查询' AS title,12 AS n,'masterdata:location:query' AS perms
UNION ALL SELECT '货位新增' AS title,13 AS n,'masterdata:location:add' AS perms
UNION ALL SELECT '货位修改' AS title,14 AS n,'masterdata:location:edit' AS perms
UNION ALL SELECT '货位删除' AS title,15 AS n,'masterdata:location:remove' AS perms
UNION ALL SELECT '货位参照' AS title,16 AS n,'masterdata:location:reference' AS perms
) d WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms=d.perms);
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT 1,m.menu_id FROM sys_menu m WHERE (m.menu_id=@warehouse_menu OR m.parent_id=@warehouse_menu)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);
COMMIT;
