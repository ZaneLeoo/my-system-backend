-- 现有业务表收录到元数据目录；不新增查询对象，不修改业务表和已有目录。
SET NAMES utf8mb4;
SET SESSION group_concat_max_len = 1048576;
DROP TEMPORARY TABLE IF EXISTS query_metadata_new_tables;
CREATE TEMPORARY TABLE query_metadata_new_tables (
  table_name VARCHAR(64) NOT NULL PRIMARY KEY,
  display_name VARCHAR(100) NOT NULL
);

START TRANSACTION;
SET @metadata_now = UTC_TIMESTAMP() + INTERVAL 8 HOUR;
INSERT INTO query_metadata_new_tables (table_name, display_name)
SELECT allowed.table_name, allowed.display_name
FROM (
  SELECT 'md_item_category' AS table_name, '料品分类' AS display_name
  UNION ALL SELECT 'md_unit', '计量单位'
  UNION ALL SELECT 'md_item', '料品档案'
  UNION ALL SELECT 'md_supplier', '供应商档案'
  UNION ALL SELECT 'md_warehouse', '仓库档案'
  UNION ALL SELECT 'md_location', '货位档案'
  UNION ALL SELECT 'purchase_order', '采购订单'
  UNION ALL SELECT 'purchase_order_line', '采购订单明细'
  UNION ALL SELECT 'purchase_arrival', '采购到货单'
  UNION ALL SELECT 'purchase_arrival_line', '采购到货明细'
  UNION ALL SELECT 'purchase_receipt', '采购入库单'
  UNION ALL SELECT 'purchase_receipt_line', '采购入库明细'
  UNION ALL SELECT 'inventory_balance', '库存余额'
  UNION ALL SELECT 'inventory_transaction', '库存流水'
) allowed
JOIN information_schema.TABLES physical_table
  ON physical_table.TABLE_SCHEMA = DATABASE()
  AND physical_table.TABLE_NAME = allowed.table_name
  AND physical_table.TABLE_TYPE = 'BASE TABLE'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_metadata_table existing
  WHERE existing.schema_name = DATABASE() AND existing.table_name = allowed.table_name
);

INSERT INTO sys_metadata_table (
  schema_name, table_name, table_comment, display_name, structure_signature,
  available, synchronized_time, version, create_by, create_time, update_by, update_time
)
SELECT DATABASE(), physical_table.TABLE_NAME, physical_table.TABLE_COMMENT, pending.display_name,
  (SELECT SHA2(GROUP_CONCAT(CONCAT(
    c.COLUMN_NAME, '|', c.COLUMN_TYPE, '|', IF(c.IS_NULLABLE = 'YES', 'true', 'false'),
    '|', c.COLUMN_COMMENT, '|', c.ORDINAL_POSITION, ';'
  ) ORDER BY c.ORDINAL_POSITION SEPARATOR ''), 256)
  FROM information_schema.COLUMNS c
  WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = physical_table.TABLE_NAME),
  TRUE, @metadata_now, 0, 'admin', @metadata_now, 'admin', @metadata_now
FROM query_metadata_new_tables pending
JOIN information_schema.TABLES physical_table
  ON physical_table.TABLE_SCHEMA = DATABASE() AND physical_table.TABLE_NAME = pending.table_name;

INSERT INTO sys_metadata_column (
  table_id, column_name, column_comment, display_name, data_type, column_type,
  max_length, numeric_scale, nullable, primary_key, ordinal_position, available,
  create_by, create_time, update_by, update_time
)
SELECT directory.table_id, c.COLUMN_NAME, c.COLUMN_COMMENT,
  LEFT(COALESCE(NULLIF(c.COLUMN_COMMENT, ''), c.COLUMN_NAME), 100),
  CASE
    WHEN c.DATA_TYPE IN ('varchar', 'char', 'text') THEN 'STRING'
    WHEN c.DATA_TYPE = 'tinyint' AND c.COLUMN_TYPE = 'tinyint(1)' THEN 'BOOLEAN'
    WHEN c.DATA_TYPE IN ('tinyint', 'int', 'bigint', 'smallint', 'mediumint') THEN 'INTEGER'
    WHEN c.DATA_TYPE = 'decimal' THEN 'DECIMAL'
    WHEN c.DATA_TYPE = 'date' THEN 'DATE'
    WHEN c.DATA_TYPE IN ('datetime', 'timestamp') THEN 'DATETIME'
    ELSE 'UNSUPPORTED'
  END,
  c.COLUMN_TYPE, c.CHARACTER_MAXIMUM_LENGTH, c.NUMERIC_SCALE,
  c.IS_NULLABLE = 'YES', c.COLUMN_KEY = 'PRI', c.ORDINAL_POSITION, TRUE,
  'admin', @metadata_now, 'admin', @metadata_now
FROM query_metadata_new_tables pending
JOIN sys_metadata_table directory
  ON directory.schema_name = DATABASE() AND directory.table_name = pending.table_name
JOIN information_schema.COLUMNS c
  ON c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = pending.table_name;
COMMIT;
DROP TEMPORARY TABLE query_metadata_new_tables;
