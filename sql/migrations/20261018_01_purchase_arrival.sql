-- 采购到货单增量，保留现有数据库及业务数据。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS purchase_arrival (
  arrival_id BIGINT NOT NULL AUTO_INCREMENT,
  arrival_no VARCHAR(64) NOT NULL,
  arrival_date DATE NOT NULL,
  supplier_id BIGINT NOT NULL,
  supplier_code VARCHAR(64) NOT NULL,
  supplier_name VARCHAR(200) NOT NULL,
  supplier_delivery_no VARCHAR(100) NOT NULL DEFAULT '',
  warehouse_id BIGINT NOT NULL,
  warehouse_code VARCHAR(64) NOT NULL,
  warehouse_name VARCHAR(100) NOT NULL,
  receiver_id BIGINT NOT NULL,
  receiver_name VARCHAR(100) NOT NULL,
  dept_id BIGINT NOT NULL,
  dept_name VARCHAR(100) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  line_count INT NOT NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  version INT NOT NULL DEFAULT 0,
  submitted_by VARCHAR(64) NULL,
  submitted_time DATETIME NULL,
  reviewed_by VARCHAR(64) NULL,
  reviewed_time DATETIME NULL,
  review_remark VARCHAR(500) NOT NULL DEFAULT '',
  voided_by VARCHAR(64) NULL,
  voided_time DATETIME NULL,
  void_reason VARCHAR(500) NOT NULL DEFAULT '',
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  PRIMARY KEY (arrival_id),
  UNIQUE KEY uk_purchase_arrival_no (arrival_no),
  KEY idx_pa_supplier (supplier_id, del_flag, status),
  KEY idx_pa_warehouse (warehouse_id, del_flag, status),
  KEY idx_pa_date (del_flag, arrival_date, arrival_id),
  CONSTRAINT chk_pa_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','VOIDED')),
  CONSTRAINT chk_pa_version CHECK (version>=0),
  CONSTRAINT chk_pa_count CHECK (line_count BETWEEN 1 AND 200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购到货单';

CREATE TABLE IF NOT EXISTS purchase_arrival_line (
  line_id BIGINT NOT NULL AUTO_INCREMENT,
  arrival_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  order_id BIGINT NOT NULL,
  order_line_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  order_line_no INT NOT NULL,
  item_id BIGINT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  specification VARCHAR(200) NOT NULL DEFAULT '',
  unit_id BIGINT NOT NULL,
  unit_name VARCHAR(100) NOT NULL,
  quantity DECIMAL(15,6) NOT NULL,
  quantity_decimal_places TINYINT NOT NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  PRIMARY KEY (line_id),
  KEY idx_pa_line_arrival (arrival_id, del_flag, line_no),
  KEY idx_pa_line_source (order_line_id, del_flag, arrival_id),
  KEY idx_pa_line_order (order_id, del_flag, arrival_id),
  KEY idx_pa_line_item (item_id, del_flag, arrival_id),
  KEY idx_pa_line_unit (unit_id, del_flag, arrival_id),
  CONSTRAINT chk_pa_line_quantity CHECK (quantity>0),
  CONSTRAINT chk_pa_line_precision CHECK (quantity_decimal_places BETWEEN 0 AND 6),
  CONSTRAINT chk_pa_line_no CHECK (line_no>0 AND order_line_no>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购到货明细';

-- ORDER EXECUTION COLUMNS: MySQL 按元数据判断增量，重复执行不重置执行数量。
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='purchase_order_line' AND COLUMN_NAME='pending_arrival_quantity'), 'SELECT 1', 'ALTER TABLE purchase_order_line ADD COLUMN pending_arrival_quantity DECIMAL(15,6) NOT NULL DEFAULT 0 COMMENT ''待审核到货数量''');
PREPARE arrival_migration FROM @ddl;
EXECUTE arrival_migration;
DEALLOCATE PREPARE arrival_migration;
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='purchase_order_line' AND COLUMN_NAME='arrived_quantity'), 'SELECT 1', 'ALTER TABLE purchase_order_line ADD COLUMN arrived_quantity DECIMAL(15,6) NOT NULL DEFAULT 0 COMMENT ''已确认到货数量''');
PREPARE arrival_migration FROM @ddl;
EXECUTE arrival_migration;
DEALLOCATE PREPARE arrival_migration;
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='purchase_order_line' AND CONSTRAINT_NAME='chk_po_line_arrival'), 'SELECT 1', 'ALTER TABLE purchase_order_line ADD CONSTRAINT chk_po_line_arrival CHECK (pending_arrival_quantity>=0 AND arrived_quantity>=0 AND pending_arrival_quantity+arrived_quantity<=quantity)');
PREPARE arrival_migration FROM @ddl;
EXECUTE arrival_migration;
DEALLOCATE PREPARE arrival_migration;

START TRANSACTION;
SET @purchase_id=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='purchase' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '采购到货单',@purchase_id,2,'arrival','purchase/arrival/index','PurchaseArrival','C','0','0','purchase:arrival:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='purchase:arrival:list');
SET @arrival_menu_id=(SELECT menu_id FROM sys_menu WHERE perms='purchase:arrival:list' ORDER BY menu_id LIMIT 1);
SET @order_menu_id=(SELECT menu_id FROM sys_menu WHERE perms='purchase:order:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT d.name,@arrival_menu_id,d.n,'#','F',d.perms,'admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR FROM (
SELECT '到货查询' AS name,1 AS n,'purchase:arrival:query' AS perms
UNION ALL SELECT '到货新增',2,'purchase:arrival:add'
UNION ALL SELECT '到货修改',3,'purchase:arrival:edit'
UNION ALL SELECT '到货删除',4,'purchase:arrival:remove'
UNION ALL SELECT '到货提交',5,'purchase:arrival:submit'
UNION ALL SELECT '到货撤回',6,'purchase:arrival:withdraw'
UNION ALL SELECT '到货审核',7,'purchase:arrival:approve'
UNION ALL SELECT '到货驳回',8,'purchase:arrival:reject'
UNION ALL SELECT '到货作废',9,'purchase:arrival:void'
) d WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms=d.perms);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT '订单行参照',@order_menu_id,10,'#','F','purchase:order:reference','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='purchase:order:reference');
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT 1,menu_id FROM sys_menu WHERE (menu_id=@arrival_menu_id OR parent_id=@arrival_menu_id OR perms='purchase:order:reference')
AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=sys_menu.menu_id);
COMMIT;
