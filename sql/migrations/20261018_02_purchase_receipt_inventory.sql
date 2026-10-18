-- 采购入库及数量库存；不重建数据库、不预置演示数据。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS purchase_receipt (
location_enabled BOOLEAN NOT NULL,
receipt_id BIGINT NOT NULL AUTO_INCREMENT,
receipt_no VARCHAR(64) NOT NULL,
receipt_date DATE NOT NULL,
supplier_id BIGINT NOT NULL,
supplier_code VARCHAR(64) NOT NULL,
supplier_name VARCHAR(200) NOT NULL,
warehouse_id BIGINT NOT NULL,
warehouse_code VARCHAR(64) NOT NULL,
warehouse_name VARCHAR(100) NOT NULL,
receiver_id BIGINT NOT NULL,
receiver_name VARCHAR(100) NOT NULL,
dept_id BIGINT NOT NULL,
dept_name VARCHAR(100) NOT NULL,
status VARCHAR(16) NOT NULL,
line_count INT NOT NULL,
remark VARCHAR(500) NOT NULL DEFAULT '',
version INT NOT NULL DEFAULT 0,
submitted_by VARCHAR(64) NULL,
submitted_time DATETIME NULL,
reviewed_by VARCHAR(64) NULL,
reviewed_time DATETIME NULL,
voided_by VARCHAR(64) NULL,
voided_time DATETIME NULL,
review_remark VARCHAR(500) NOT NULL DEFAULT '',
void_reason VARCHAR(500) NOT NULL DEFAULT '',
del_flag CHAR(1) NOT NULL DEFAULT '0',
create_by VARCHAR(64) NOT NULL,create_time DATETIME NOT NULL,update_by VARCHAR(64) NULL,update_time DATETIME NULL,
PRIMARY KEY (receipt_id),
UNIQUE KEY uk_pr_no(receipt_no),
KEY idx_pr_supplier(supplier_id,del_flag,status),
KEY idx_pr_warehouse(warehouse_id,del_flag,status),
KEY idx_pr_date(del_flag,receipt_date,receipt_id),
CONSTRAINT chk_pr_status CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','VOIDED')),
CONSTRAINT chk_pr_version CHECK(version>=0),
CONSTRAINT chk_pr_count CHECK(line_count BETWEEN 1 AND 200)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS purchase_receipt_line (
line_id BIGINT NOT NULL AUTO_INCREMENT,
receipt_id BIGINT NOT NULL,
line_no VARCHAR(64) NOT NULL,
arrival_id BIGINT NOT NULL,
arrival_line_id BIGINT NOT NULL,
arrival_no VARCHAR(64) NOT NULL,
arrival_line_no VARCHAR(64) NOT NULL,
order_id BIGINT NOT NULL,
order_line_id BIGINT NOT NULL,
order_no VARCHAR(64) NOT NULL,
order_line_no VARCHAR(64) NOT NULL,
item_id BIGINT NOT NULL,
item_code VARCHAR(64) NOT NULL,
item_name VARCHAR(200) NOT NULL,
specification VARCHAR(200) NOT NULL DEFAULT '',
unit_id BIGINT NOT NULL,
unit_name VARCHAR(100) NOT NULL,
location_id BIGINT NULL,
location_code VARCHAR(64) NOT NULL DEFAULT '',
location_name VARCHAR(100) NOT NULL DEFAULT '',
quantity DECIMAL(15,6) NOT NULL,
quantity_decimal_places TINYINT NOT NULL,
remark VARCHAR(500) NOT NULL DEFAULT '',
del_flag CHAR(1) NOT NULL DEFAULT '0',
create_by VARCHAR(64) NOT NULL,create_time DATETIME NOT NULL,update_by VARCHAR(64) NULL,update_time DATETIME NULL,
PRIMARY KEY (line_id),
KEY idx_prl_header(receipt_id,del_flag,line_no),
KEY idx_prl_source(arrival_line_id,del_flag,receipt_id),
KEY idx_prl_arrival(arrival_id,del_flag,receipt_id),
KEY idx_prl_item(item_id,del_flag),
KEY idx_prl_location(location_id,del_flag),
KEY idx_prl_unit(unit_id,del_flag),
CONSTRAINT chk_prl_qty CHECK(quantity>0),
CONSTRAINT chk_prl_precision CHECK(quantity_decimal_places BETWEEN 0 AND 6),
CONSTRAINT chk_prl_line CHECK(line_no>0 AND arrival_line_no>0 AND order_line_no>0),
CONSTRAINT chk_prl_location CHECK(location_id IS NULL OR location_id>0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS inventory_balance (
balance_id BIGINT NOT NULL AUTO_INCREMENT,
warehouse_id BIGINT NOT NULL,
location_id BIGINT NULL,
item_id BIGINT NOT NULL,
unit_id BIGINT NOT NULL,
quantity DECIMAL(20,6) NOT NULL DEFAULT 0,
version BIGINT NOT NULL DEFAULT 0,
create_by VARCHAR(64) NOT NULL,create_time DATETIME NOT NULL,update_by VARCHAR(64) NULL,update_time DATETIME NULL,
PRIMARY KEY (balance_id),
location_key BIGINT GENERATED ALWAYS AS (COALESCE(location_id,0)) STORED,
UNIQUE KEY uk_ib_dimension(warehouse_id,location_key,item_id),
KEY idx_ib_item(item_id,warehouse_id),
CONSTRAINT chk_ib_quantity CHECK(quantity>=0),
CONSTRAINT chk_ib_version CHECK(version>=0),
CONSTRAINT chk_ib_location CHECK(location_id IS NULL OR location_id>0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS inventory_transaction (
transaction_id BIGINT NOT NULL AUTO_INCREMENT,
balance_id BIGINT NOT NULL,
business_type VARCHAR(100) NOT NULL,
receipt_id BIGINT NOT NULL,
receipt_no VARCHAR(64) NOT NULL,
receipt_line_id BIGINT NOT NULL,
warehouse_id BIGINT NOT NULL,
location_id BIGINT NULL,
item_id BIGINT NOT NULL,
unit_id BIGINT NOT NULL,
warehouse_code VARCHAR(64) NOT NULL,
warehouse_name VARCHAR(100) NOT NULL,
location_code VARCHAR(64) NOT NULL DEFAULT '',
location_name VARCHAR(100) NOT NULL DEFAULT '',
item_code VARCHAR(64) NOT NULL,
item_name VARCHAR(200) NOT NULL,
specification VARCHAR(200) NOT NULL DEFAULT '',
unit_name VARCHAR(100) NOT NULL,
quantity_change DECIMAL(20,6) NOT NULL,
quantity_before DECIMAL(20,6) NOT NULL,
quantity_after DECIMAL(20,6) NOT NULL,
business_date DATE NOT NULL,
posted_time DATETIME NOT NULL,
reversal_of BIGINT NULL,
remark VARCHAR(500) NOT NULL DEFAULT '',
create_by VARCHAR(64) NOT NULL,create_time DATETIME NOT NULL,update_by VARCHAR(64) NULL,update_time DATETIME NULL,
PRIMARY KEY (transaction_id),
UNIQUE KEY uk_it_source(business_type,receipt_line_id),
UNIQUE KEY uk_it_reversal(reversal_of),
KEY idx_it_dimension(warehouse_id,location_id,item_id,posted_time,transaction_id),
KEY idx_it_receipt(receipt_id,transaction_id),
KEY idx_it_time(posted_time,transaction_id),
CONSTRAINT chk_it_type CHECK(business_type IN ('PURCHASE_RECEIPT','PURCHASE_RECEIPT_VOID')),
CONSTRAINT chk_it_qty CHECK(quantity_change<>0 AND quantity_before>=0 AND quantity_after>=0 AND quantity_before+quantity_change=quantity_after),
CONSTRAINT chk_it_direction CHECK((business_type='PURCHASE_RECEIPT' AND quantity_change>0 AND reversal_of IS NULL) OR (business_type='PURCHASE_RECEIPT_VOID' AND quantity_change<0 AND reversal_of IS NOT NULL))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- RECEIPT EXECUTION COLUMNS
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='purchase_arrival_line' AND COLUMN_NAME='pending_receipt_quantity'),'SELECT 1','ALTER TABLE purchase_arrival_line ADD COLUMN pending_receipt_quantity DECIMAL(15,6) NOT NULL DEFAULT 0');
PREPARE receipt_migration FROM @ddl; EXECUTE receipt_migration; DEALLOCATE PREPARE receipt_migration;
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='purchase_arrival_line' AND COLUMN_NAME='received_quantity'),'SELECT 1','ALTER TABLE purchase_arrival_line ADD COLUMN received_quantity DECIMAL(15,6) NOT NULL DEFAULT 0');
PREPARE receipt_migration FROM @ddl; EXECUTE receipt_migration; DEALLOCATE PREPARE receipt_migration;
SET @ddl=IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='purchase_arrival_line' AND CONSTRAINT_NAME='chk_pa_line_receipt'),'SELECT 1','ALTER TABLE purchase_arrival_line ADD CONSTRAINT chk_pa_line_receipt CHECK(pending_receipt_quantity>=0 AND received_quantity>=0 AND pending_receipt_quantity+received_quantity<=quantity)');
PREPARE receipt_migration FROM @ddl; EXECUTE receipt_migration; DEALLOCATE PREPARE receipt_migration;
START TRANSACTION;
SET @purchase_id=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='purchase' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '采购入库单',@purchase_id,3,'receipt','purchase/receipt/index','PurchaseReceipt','C','0','0','purchase:receipt:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='purchase:receipt:list');
SET @receipt_menu_id=(SELECT menu_id FROM sys_menu WHERE perms='purchase:receipt:list' ORDER BY menu_id LIMIT 1);
SET @source_arrival_menu=(SELECT menu_id FROM sys_menu WHERE perms='purchase:arrival:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT d.name,@receipt_menu_id,d.n,'#','F',d.perms,'admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR FROM (
SELECT '入库查询' AS name,1 AS n,'purchase:receipt:query' AS perms
UNION ALL SELECT '入库新增',2,'purchase:receipt:add'
UNION ALL SELECT '入库修改',3,'purchase:receipt:edit'
UNION ALL SELECT '入库删除',4,'purchase:receipt:remove'
UNION ALL SELECT '入库提交',5,'purchase:receipt:submit'
UNION ALL SELECT '入库撤回',6,'purchase:receipt:withdraw'
UNION ALL SELECT '入库审核',7,'purchase:receipt:approve'
UNION ALL SELECT '入库驳回',8,'purchase:receipt:reject'
UNION ALL SELECT '入库作废',9,'purchase:receipt:void'
) d WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms=d.perms);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT '到货行参照',@source_arrival_menu,10,'#','F','purchase:arrival:reference','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='purchase:arrival:reference');
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT 1,menu_id FROM sys_menu WHERE (menu_id=@receipt_menu_id OR parent_id=@receipt_menu_id OR perms='purchase:arrival:reference')
AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=sys_menu.menu_id);


INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,menu_type,visible,status,icon,create_by,create_time) SELECT '库存管理',0,7,'inventory','Layout','M','0','0','warehouse','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='inventory');
SET @inventory_menu=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='inventory' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time) SELECT '库存余额',@inventory_menu,1,'balance','inventory/balance/index','InventoryBalance','C','0','0','inventory:balance:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='inventory:balance:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time) SELECT '库存流水',@inventory_menu,2,'transaction','inventory/transaction/index','InventoryTransaction','C','0','0','inventory:transaction:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='inventory:transaction:list');
INSERT INTO sys_role_menu(role_id,menu_id) SELECT 1,menu_id FROM sys_menu WHERE (menu_id=@inventory_menu OR parent_id=@inventory_menu) AND NOT EXISTS(SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=sys_menu.menu_id);
COMMIT;
