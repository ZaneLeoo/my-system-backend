package com.ruoyi.query.engine;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.*;
import lombok.Getter;

/** 仅由引擎创建的 SQL 结构和值，绑定交给 MyBatis-Plus。 */
public final class CompiledQuery {
  /** 编译器生成的受控 SQL 条件模板，参数值单独绑定。 */
  @Getter private final String sql;

  /** 按占位符顺序保存的绑定值，不得写入普通查询日志。 */
  @Getter private final List<Object> parameters;

  /** 已验证的主表排序列，包含受控表别名。 */
  private final String sortColumn;

  /** 是否按主排序字段升序排列。 */
  private final boolean ascending;

  /** 主表主键列，用于追加稳定排序。 */
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
}
