package com.ruoyi.query.spi;

import lombok.Getter;

/** 开发者登记的查询来源；连接和强制过滤不接受页面输入。 */
@Getter
public final class QuerySource {
  /** 开发者登记的物理表名，运行时请求不可改写。 */
  private final String table;

  /** 开发者登记的 SQL 表别名。 */
  private final String alias;

  /** 来源分组的中文显示名称。 */
  private final String label;

  /** 与主表关联的受控表达式，主表来源为 null。 */
  private final String correlation;

  /** 创建经过代码审核的数据来源。 */
  public QuerySource(String table, String alias, String label, String correlation) {
    this.table = table;
    this.alias = alias;
    this.label = label;
    this.correlation = correlation;
  }
}
