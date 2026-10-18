package com.ruoyi.business.masterdata.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

/** 从生产脚本创建关联测试基础表，不执行菜单和演示数据。 */
public final class MasterDataTestSchema {
  /** 常量工具类禁止实例化。 */
  private MasterDataTestSchema() {}

  /** 使用测试保留的连接建表，兼容 H2 的 CHECK 表达式生命周期。 */
  public static void create(Connection connection) throws Exception {
    for (String file :
        new String[] {
          "20261014_01_item_category.sql",
          "20261015_01_unit.sql",
          "20261016_01_item.sql",
          "20261017_01_supplier.sql",
          "20261017_02_purchase_order.sql",
          "20261017_03_warehouse_location.sql",
          "20261018_01_purchase_arrival.sql",
          "20261018_02_purchase_receipt_inventory.sql"
        }) {
      Path path = Path.of("../sql/migrations", file);
      if (!Files.exists(path)) path = Path.of("sql/migrations", file);
      String schema =
          Files.readString(path)
              .split("START TRANSACTION;")[0]
              .split("-- ORDER EXECUTION COLUMNS")[0]
              .split("-- RECEIPT EXECUTION COLUMNS")[0]
              .replace(
                  "GENERATED ALWAYS AS (COALESCE(location_id,0)) STORED",
                  "GENERATED ALWAYS AS (COALESCE(location_id,0))")
              .replaceAll("(?m)^SET NAMES.*;", "")
              .replaceAll("ENGINE=InnoDB[^;]*", "");
      try (var statement = connection.createStatement()) {
        for (String sql : schema.split(";")) if (!sql.isBlank()) statement.execute(sql);
      }
    }
    try (var statement = connection.createStatement()) {
      statement.execute("ALTER TABLE purchase_arrival ALTER COLUMN warehouse_id DROP NOT NULL");
      statement.execute(
          "ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_id BIGINT NOT NULL");
      statement.execute(
          "ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_code VARCHAR(64) NOT NULL");
      statement.execute(
          "ALTER TABLE purchase_receipt_line ADD COLUMN warehouse_name VARCHAR(100) NOT NULL");
      statement.execute(
          "ALTER TABLE purchase_receipt_line ADD COLUMN location_enabled BOOLEAN NOT NULL");
      statement.execute("DROP INDEX idx_pr_warehouse");
      statement.execute("ALTER TABLE purchase_receipt DROP COLUMN warehouse_id");
      statement.execute("ALTER TABLE purchase_receipt DROP COLUMN warehouse_code");
      statement.execute("ALTER TABLE purchase_receipt DROP COLUMN warehouse_name");
      statement.execute("ALTER TABLE purchase_receipt DROP COLUMN location_enabled");
      statement.execute(
          "ALTER TABLE purchase_order_line ADD COLUMN IF NOT EXISTS pending_arrival_quantity DECIMAL(15,6) NOT NULL DEFAULT 0");
      statement.execute(
          "ALTER TABLE purchase_order_line ADD COLUMN IF NOT EXISTS arrived_quantity DECIMAL(15,6) NOT NULL DEFAULT 0");
      statement.execute(
          "ALTER TABLE purchase_arrival_line ADD COLUMN IF NOT EXISTS pending_receipt_quantity DECIMAL(15,6) NOT NULL DEFAULT 0");
      statement.execute(
          "ALTER TABLE purchase_arrival_line ADD COLUMN IF NOT EXISTS received_quantity DECIMAL(15,6) NOT NULL DEFAULT 0");
      statement.execute(
          "ALTER TABLE purchase_arrival_line ADD CONSTRAINT IF NOT EXISTS chk_pa_line_receipt CHECK(pending_receipt_quantity>=0 AND received_quantity>=0 AND pending_receipt_quantity+received_quantity<=quantity)");
      statement.execute(
          "ALTER TABLE purchase_order_line ADD CONSTRAINT IF NOT EXISTS chk_po_line_arrival CHECK (pending_arrival_quantity>=0 AND arrived_quantity>=0 AND pending_arrival_quantity+arrived_quantity<=quantity)");
    }
  }
}
