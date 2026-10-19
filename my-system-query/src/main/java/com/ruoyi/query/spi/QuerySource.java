package com.ruoyi.query.spi;

/** 开发者登记的查询来源；连接和强制过滤不接受页面输入。 */
public final class QuerySource {
  private final String table;
  private final String alias;
  private final String label;
  private final String correlation;

  /** 创建经过代码审核的数据来源。 */
  public QuerySource(String table, String alias, String label, String correlation) {
    this.table = table;
    this.alias = alias;
    this.label = label;
    this.correlation = correlation;
  }

  /** 返回物理表。 */
  public String getTable() {
    return table;
  }

  /** 返回受控别名。 */
  public String getAlias() {
    return alias;
  }

  /** 返回中文名称。 */
  public String getLabel() {
    return label;
  }

  /** 返回关联条件，主表为空。 */
  public String getCorrelation() {
    return correlation;
  }
}
