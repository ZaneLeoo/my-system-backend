package com.ruoyi.query.spi;

import java.util.Map;

/** 业务模块声明允许收录的物理表，不授予查询能力或业务数据权限。 */
public interface MetadataTableProvider {
  /**
   * 返回物理表名与默认中文目录名称。
   *
   * @return 物理表名到默认中文名称的精确映射，不能包含模式或 SQL 表达式
   */
  Map<String, String> tables();
}
