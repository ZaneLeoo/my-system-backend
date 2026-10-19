package com.ruoyi.query.engine;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.*;

/** 仅由引擎创建的 SQL 结构和值，绑定交给 MyBatis-Plus。 */
public final class CompiledQuery {
  private final String sql;
  private final List<Object> parameters;
  private final String sortColumn;
  private final boolean ascending;
  private final String primaryKey;

  /** 保存编译结果，不包含客户端 SQL。 */
  CompiledQuery(
      String sql,
      List<Object> parameters,
      String sortColumn,
      boolean ascending,
      String primaryKey) {
    this.sql = sql;
    this.parameters = List.copyOf(parameters);
    this.sortColumn = sortColumn;
    this.ascending = ascending;
    this.primaryKey = primaryKey;
  }

  /** 创建带参数绑定和稳定排序的 Wrapper。 */
  public <T> QueryWrapper<T> wrapper() {
    return new QueryWrapper<T>()
        .apply(sql, parameters.toArray())
        .orderBy(true, ascending, sortColumn)
        .orderByAsc(primaryKey);
  }

  /** 返回 SQL 模板供安全验证。 */
  public String getSql() {
    return sql;
  }

  /** 返回绑定值供测试和诊断，日志不得记录此列表。 */
  public List<Object> getParameters() {
    return parameters;
  }
}
