-- 采购订单一期增量，不重建数据库，不预置演示订单。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS purchase_order (
  order_id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  order_date DATE NOT NULL,
  supplier_id BIGINT NOT NULL,
  supplier_code VARCHAR(64) NOT NULL,
  supplier_name VARCHAR(200) NOT NULL,
  contact_name VARCHAR(64) NOT NULL DEFAULT '',
  contact_phone VARCHAR(64) NOT NULL DEFAULT '',
  address VARCHAR(500) NOT NULL DEFAULT '',
  buyer_id BIGINT NOT NULL,
  buyer_name VARCHAR(100) NOT NULL,
  dept_id BIGINT NOT NULL,
  dept_name VARCHAR(100) NOT NULL,
  quantity_decimal_places TINYINT NOT NULL,
  price_decimal_places TINYINT NOT NULL,
  amount_decimal_places TINYINT NOT NULL DEFAULT 2,
  rounding_mode VARCHAR(16) NOT NULL DEFAULT 'HALF_UP',
  currency CHAR(3) NOT NULL DEFAULT 'CNY',
  price_tax_mode VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  line_count INT NOT NULL,
  net_amount DECIMAL(20,2) NOT NULL,
  tax DECIMAL(20,2) NOT NULL,
  gross_amount DECIMAL(20,2) NOT NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  version INT NOT NULL DEFAULT 0,
  submitted_by VARCHAR(64) NULL,
  submitted_time DATETIME NULL,
  reviewed_by VARCHAR(64) NULL,
  reviewed_time DATETIME NULL,
  review_remark VARCHAR(500) NOT NULL DEFAULT '',
  closed_by VARCHAR(64) NULL,
  closed_time DATETIME NULL,
  close_reason VARCHAR(500) NOT NULL DEFAULT '',
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_purchase_order_no (order_no),
  KEY idx_po_supplier (supplier_id, del_flag, status),
  KEY idx_po_date (del_flag, order_date, order_id),
  CONSTRAINT chk_po_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','CLOSED')),
  CONSTRAINT chk_po_basis CHECK (price_tax_mode IN ('TAX_INCLUDED','TAX_EXCLUDED')),
  CONSTRAINT chk_po_currency CHECK (currency='CNY'),
  CONSTRAINT chk_po_version CHECK (version>=0),
  CONSTRAINT chk_po_count CHECK (line_count BETWEEN 1 AND 200),
  CONSTRAINT chk_po_amount CHECK (net_amount>=0 AND tax>=0 AND gross_amount=net_amount+tax)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单';

CREATE TABLE IF NOT EXISTS purchase_order_line (
  line_id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_id BIGINT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  specification VARCHAR(200) NOT NULL DEFAULT '',
  unit_id BIGINT NOT NULL,
  unit_name VARCHAR(100) NOT NULL,
  quantity DECIMAL(15,6) NOT NULL,
  net_price DECIMAL(18,6) NOT NULL,
  gross_price DECIMAL(18,6) NOT NULL,
  tax_rate DECIMAL(5,2) NOT NULL,
  net_amount DECIMAL(20,2) NOT NULL,
  tax DECIMAL(20,2) NOT NULL,
  gross_amount DECIMAL(20,2) NOT NULL,
  expected_date DATE NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  PRIMARY KEY (line_id),
  KEY idx_po_line_order (order_id, del_flag, line_no),
  KEY idx_po_line_item (item_id, del_flag, order_id),
  KEY idx_po_line_unit (unit_id, del_flag, order_id),
  CONSTRAINT chk_po_line_quantity CHECK (quantity>0),
  CONSTRAINT chk_po_line_tax CHECK (tax_rate BETWEEN 0 AND 100),
  CONSTRAINT chk_po_line_price CHECK (net_price>=0 AND gross_price>=0),
  CONSTRAINT chk_po_line_amount CHECK (net_amount>=0 AND tax>=0 AND gross_amount=net_amount+tax),
  CONSTRAINT chk_po_line_no CHECK (line_no>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单明细';

START TRANSACTION;
INSERT INTO sys_config (config_name,config_key,config_value,config_type,create_by,create_time)
SELECT definitions.name,definitions.config_key,'6','N','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
FROM (SELECT '采购订单数量小数位' AS name,'purchase.order.quantityDecimalPlaces' AS config_key
UNION ALL SELECT '采购订单单价小数位','purchase.order.priceDecimalPlaces') definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_config existing WHERE existing.config_key=definitions.config_key);

INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,menu_type,visible,status,icon,create_by,create_time)
SELECT '采购管理',0,6,'purchase',NULL,'M','0','0','shopping','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='purchase');
SET @purchase_id=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='purchase' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '采购订单',@purchase_id,1,'order','purchase/order/index','PurchaseOrder','C','0','0','purchase:order:list','list','admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='purchase:order:list');
SET @order_menu_id=(SELECT menu_id FROM sys_menu WHERE perms='purchase:order:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT definitions.menu_name,@order_menu_id,definitions.n,'#','F',definitions.perms,'admin',UTC_TIMESTAMP()+INTERVAL 8 HOUR
FROM (
  SELECT '订单查询' AS menu_name,1 AS n,'purchase:order:query' AS perms
  UNION ALL SELECT '订单新增',2,'purchase:order:add'
  UNION ALL SELECT '订单修改',3,'purchase:order:edit'
  UNION ALL SELECT '订单删除',4,'purchase:order:remove'
  UNION ALL SELECT '订单提交',5,'purchase:order:submit'
  UNION ALL SELECT '订单撤回',6,'purchase:order:withdraw'
  UNION ALL SELECT '订单审核',7,'purchase:order:approve'
  UNION ALL SELECT '订单驳回',8,'purchase:order:reject'
  UNION ALL SELECT '订单关闭',9,'purchase:order:close'
 ) definitions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms=definitions.perms);
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT 1,menu_id FROM sys_menu WHERE (menu_id=@purchase_id OR menu_id=@order_menu_id OR parent_id=@order_menu_id)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id=1 AND assigned.menu_id=sys_menu.menu_id);
COMMIT;
