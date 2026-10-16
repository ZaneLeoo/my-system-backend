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
          "20261014_01_item_category.sql", "20261015_01_unit.sql", "20261016_01_item.sql"
        }) {
      Path path = Path.of("../sql/migrations", file);
      if (!Files.exists(path)) path = Path.of("sql/migrations", file);
      String schema =
          Files.readString(path)
              .split("START TRANSACTION;")[0]
              .replaceAll("(?m)^SET NAMES.*;", "")
              .replaceAll("ENGINE=InnoDB[^;]*", "");
      try (var statement = connection.createStatement()) {
        for (String sql : schema.split(";")) if (!sql.isBlank()) statement.execute(sql);
      }
    }
  }
}
