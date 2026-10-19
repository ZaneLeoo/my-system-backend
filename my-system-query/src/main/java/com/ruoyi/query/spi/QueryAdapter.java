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

  /**
   * 声明可用于配置查询的主表及预设关联来源。
   *
   * <p>表名、别名和关联表达式仅由开发者登记，实施配置和查询请求不能改写。主表使用固定来源标识 main。
   *
   * @return 受控来源映射，必须包含 main，关联来源需声明与主表的关联条件
   */
  Map<String, QuerySource> sources();

  /** 主表稳定排序键。 */
  String primaryKey();

  /** 默认排序字段标识。 */
  String defaultSort();

  /** 默认排序方向。 */
  String defaultDirection();

  /**
   * 执行框架已校验的条件，分页读取并组装当前业务的列表 VO。
   *
   * <p>实现应保留完整匹配总数，绑定全部条件值，并在辅助查询前及退出时清理分页上下文。返回行类型必须与业务查询入口声明的 VO 类型一致。
   *
   * @param query 框架已校验并绑定参数的查询条件和稳定排序
   * @param request 已通过运行时校验的分页、发布版本、排序和条件请求
   * @return 本页业务 VO 及全部匹配记录的总数
   */
  PageResult<?> search(CompiledQuery query, SearchRequest request);

  /**
   * 对完成通用类型转换的值执行业务校验，例如状态枚举和档案 ID 的合法范围。
   *
   * @param field 已发布字段定义，包含数据类型、选项来源和允许的运算符
   * @param value 已完成通用类型转换的单个条件值；集合及范围会逐个校验
   * @throws com.ruoyi.common.exception.ServiceException 适配器发现条件值不满足业务约束时
   */
  default void validateValue(FieldSpec field, Object value) {}

  /**
   * 提供已注册选项来源，禁止任意 URL。
   *
   * @return 当前适配器允许使用的选项提供器标识集合
   */
  default Set<String> optionProviders() {
    return Set.of();
  }

  /**
   * 使用已注册的选项提供器搜索可选值，或批量回显已保存的查询值。
   *
   * @param provider 当前适配器已登记的选项提供器标识
   * @param keyword 编码或名称搜索关键字，可为空
   * @param ids 批量回显的 ID 集合，非空时已限定为 1～100 个不同正整数；为 null 时按关键字及页码搜索
   * @param page 从 1 开始的选项页码
   * @return 包含 rows 和 total 的选项结果，rows 中每项提供 value、label 及必要的层级信息
   * @throws IllegalArgumentException 当前适配器未实现该选项来源时
   */
  default Object options(String provider, String keyword, List<Long> ids, int page) {
    throw new IllegalArgumentException("不支持的选项来源");
  }

  /**
   * 将分类后代条件展开为可绑定的节点 ID，范围包含选中节点本身。
   *
   * @param id 已校验的分类节点 ID
   * @return 包含选中节点的后代 ID 集合；无匹配记录时返回空集合
   * @throws IllegalArgumentException 当前适配器不支持分类后代查询时
   */
  default List<?> descendants(Object id) {
    throw new IllegalArgumentException("不支持分类后代查询");
  }

  /**
   * 按受控来源和物理字段绑定引用或枚举语义，发布时用来校验配置的选项来源。
   *
   * @param sourceKey 适配器登记的来源标识，例如 main 或 lines
   * @param columnName 来源表中的物理字段名
   * @return 引用或枚举提供器标识，普通字段返回空字符串
   */
  default String fieldOptionProvider(String sourceKey, String columnName) {
    return "";
  }
}
