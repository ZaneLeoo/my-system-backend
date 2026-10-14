-- 增量初始化：保留已有数据，可重复执行；不得替代若依全量初始化脚本。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS md_item_category (
  category_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '存货分类ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级分类ID，0为顶级',
  category_code VARCHAR(64) NOT NULL COMMENT '分类编码，删除后仍保留占用',
  category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同级显示顺序',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '编辑版本号，防止旧页面覆盖',
  create_by VARCHAR(64) NOT NULL COMMENT '创建账号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT NULL COMMENT '最后修改账号',
  update_time DATETIME DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_md_item_category_code (category_code),
  KEY idx_md_item_category_parent (parent_id, del_flag, sort_order),
  CONSTRAINT chk_md_item_category_status CHECK (status IN ('0', '1')),
  CONSTRAINT chk_md_item_category_deleted CHECK (del_flag IN ('0', '2')),
  CONSTRAINT chk_md_item_category_sort CHECK (sort_order >= 0),
  CONSTRAINT chk_md_item_category_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存货分类';

-- 分类写操作先锁此行，空树也能安全进行跨实例并发校验。
CREATE TABLE IF NOT EXISTS md_item_category_lock (
  lock_id INT NOT NULL PRIMARY KEY COMMENT '分类树锁，固定为1',
  CONSTRAINT chk_md_item_category_lock CHECK (lock_id = 1)
) ENGINE=InnoDB COMMENT='存货分类树事务锁';
INSERT IGNORE INTO md_item_category_lock (lock_id) VALUES (1);

START TRANSACTION;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, create_time)
SELECT '基础资料', 0, 5, 'masterdata', NULL, 'M', '0', '0', 'component', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata');
SET @masterdata_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'masterdata' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '料品分类', @masterdata_id, 1, 'item-category', 'masterdata/item-category/index', 'ItemCategory', 'C', '0', '0', 'masterdata:category:list', 'tree', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'masterdata:category:list');
SET @category_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'masterdata:category:list' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, create_by, create_time)
SELECT definitions.menu_name, @category_menu_id, definitions.sort_order, '#', 'F', definitions.perms, 'admin', NOW()
FROM (
  SELECT '分类查询' AS menu_name, 1 AS sort_order, 'masterdata:category:query' AS perms
  UNION ALL SELECT '分类新增', 2, 'masterdata:category:add'
  UNION ALL SELECT '分类修改', 3, 'masterdata:category:edit'
  UNION ALL SELECT '分类删除', 4, 'masterdata:category:remove'
) AS definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms = definitions.perms);

-- 仅给管理员角色授权；其他角色由权限管理界面显式分配。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id = @masterdata_id OR menu_id = @category_menu_id OR parent_id = @category_menu_id)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id = 1 AND assigned.menu_id = sys_menu.menu_id);
COMMIT;
