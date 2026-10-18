-- 到货主表的预计入库仓库为可选默认值；实际仓库仍由入库明细必填。
-- 重复执行不修改档案引用、单据快照、审计、执行数量或库存。
SET NAMES utf8mb4;
ALTER TABLE purchase_arrival
  MODIFY COLUMN warehouse_id BIGINT NULL,
  MODIFY COLUMN warehouse_code VARCHAR(64) NOT NULL DEFAULT '',
  MODIFY COLUMN warehouse_name VARCHAR(100) NOT NULL DEFAULT '';
