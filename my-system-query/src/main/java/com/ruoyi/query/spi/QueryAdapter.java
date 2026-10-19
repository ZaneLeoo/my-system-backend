package com.ruoyi.query.spi;

import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.query.engine.CompiledQuery;
import com.ruoyi.query.protocol.*;
import java.util.*;

/** 业务边界由适配器登记，公共引擎不依赖业务模块。 */
public interface QueryAdapter {
  /** 稳定业务标识。 */
  String key();

  /** 业务列表权限。 */
  String permission();

  /** 来源目录，必须有 main。 */
  Map<String, QuerySource> sources();

  /** 主表稳定排序键。 */
  String primaryKey();

  /** 默认排序字段标识。 */
  String defaultSort();

  /** 默认排序方向。 */
  String defaultDirection();

  /** 执行已校验编译结果并返回现有业务 VO。 */
  PageResult<?> search(CompiledQuery query, SearchRequest request);

  /** 校验业务枚举、类型和特殊操作。 */
  default void validateValue(FieldSpec field, Object value) {}

  /** 提供已注册选项来源，禁止任意 URL。 */
  default Set<String> optionProviders() {
    return Set.of();
  }

  /** 查询选项。 */
  default Object options(String provider, String keyword, List<Long> ids, int page) {
    throw new IllegalArgumentException("不支持的选项来源");
  }

  /** 将专用条件转换成普通 SQL 参数值，例如分类后代 ID。 */
  default List<?> descendants(Object id) {
    throw new IllegalArgumentException("不支持分类后代查询");
  }

  /** 返回字段的语义选项来源，空字符串表示普通字段。 */
  default String fieldOptionProvider(String sourceKey, String columnName) {
    return "";
  }
}
