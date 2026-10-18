-- 实际仓库下放入库明细；先复制历史表头快照，再移除旧字段。
-- 到货单 warehouse_* 保留，含义为预计入库仓库，不决定库存维度。
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS migrate_receipt_line_warehouse;
DELIMITER $$
CREATE PROCEDURE migrate_receipt_line_warehouse()
BEGIN
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt_line' AND column_name='warehouse_id') THEN
    ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_id BIGINT NULL;
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt_line' AND column_name='warehouse_code') THEN
    ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_code VARCHAR(64) NULL;
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt_line' AND column_name='warehouse_name') THEN
    ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_name VARCHAR(100) NULL;
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt_line' AND column_name='location_enabled') THEN
    ALTER TABLE purchase_receipt_line ADD COLUMN location_enabled BOOLEAN NULL;
  END IF;
  IF EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt' AND column_name='warehouse_id') THEN
    -- 包括逻辑删除行，保留原始快照；重复执行不覆盖已经下放的仓库。
    UPDATE purchase_receipt_line l JOIN purchase_receipt h ON h.receipt_id=l.receipt_id
      SET l.warehouse_id=h.warehouse_id,l.warehouse_code=h.warehouse_code,
          l.warehouse_name=h.warehouse_name,l.location_enabled=h.location_enabled
      WHERE l.warehouse_id IS NULL;
  END IF;
  IF EXISTS(SELECT 1 FROM purchase_receipt_line WHERE warehouse_id IS NULL OR warehouse_code IS NULL OR warehouse_name IS NULL OR location_enabled IS NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Receipt line warehouse backfill incomplete; header snapshots retained';
  END IF;
  ALTER TABLE purchase_receipt_line
    MODIFY COLUMN warehouse_id BIGINT NOT NULL,
    MODIFY COLUMN warehouse_code VARCHAR(64) NOT NULL,
    MODIFY COLUMN warehouse_name VARCHAR(100) NOT NULL,
    MODIFY COLUMN location_enabled BOOLEAN NOT NULL;
  IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='purchase_receipt_line' AND index_name='idx_prl_warehouse') THEN
    ALTER TABLE purchase_receipt_line ADD KEY idx_prl_warehouse(warehouse_id,del_flag,receipt_id);
  END IF;
  IF EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='purchase_receipt' AND column_name='warehouse_id') THEN
    IF EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='purchase_receipt' AND index_name='idx_pr_warehouse') THEN
      ALTER TABLE purchase_receipt DROP INDEX idx_pr_warehouse;
    END IF;
    -- 四个旧字段一次删除，失败时保留原表头供重试。
    ALTER TABLE purchase_receipt DROP COLUMN warehouse_id, DROP COLUMN warehouse_code,
      DROP COLUMN warehouse_name, DROP COLUMN location_enabled;
  END IF;
END$$
DELIMITER ;
CALL migrate_receipt_line_warehouse();
DROP PROCEDURE migrate_receipt_line_warehouse;
