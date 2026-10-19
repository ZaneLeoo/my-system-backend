package com.ruoyi.query.engine;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.definition.*;
import com.ruoyi.query.mapper.*;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.*;
import org.springframework.stereotype.Service;

/** 按发布版本隔离不可变快照，每个请求仍读取当前版本并检查业务权限。 */
@Service
public class QueryRuntimeService {
  private static final Logger LOG = LoggerFactory.getLogger(QueryRuntimeService.class);
  private final DefinitionService definitions;
  private final QueryReleaseMapper releases;
  private final MetadataColumnMapper columns;
  private final QueryRegistry registry;
  private final QueryCompiler compiler;
  private final Map<String, QuerySnapshot> cache = new ConcurrentHashMap<>();

  /** 注入发布读取和纯编译器。 */
  public QueryRuntimeService(
      DefinitionService definitions,
      QueryReleaseMapper releases,
      MetadataColumnMapper columns,
      QueryRegistry registry,
      QueryCompiler compiler) {
    this.definitions = definitions;
    this.releases = releases;
    this.columns = columns;
    this.registry = registry;
    this.compiler = compiler;
  }

  /** 返回当前版本，同时检测已同步的字段失效和类型变化。 */
  public QuerySnapshot published(String key) {
    registry.authorized(key);
    var d = definitions.byKey(key);
    if (d.getPublishedRevision() == 0) throw new ServiceException("查询配置尚未发布", 409);
    String version = key + ":" + d.getPublishedRevision();
    var snapshot = cache.get(version);
    if (snapshot == null) {
      var release =
          releases.selectOne(
              new QueryWrapper<QueryRelease>()
                  .eq("definition_id", d.getDefinitionId())
                  .eq("revision", d.getPublishedRevision()));
      if (release == null) throw new ServiceException("查询发布版本不存在", 409);
      snapshot = JSON.parseObject(release.getConfigSnapshot(), QuerySnapshot.class);
      if (cache.size() > 128) cache.clear();
      cache.put(version, snapshot);
    }
    if (!Boolean.TRUE.equals(snapshot.getEnabled())) throw new ServiceException("查询对象已停用", 409);
    Map<Long, com.ruoyi.query.metadata.MetadataColumn> actual = new HashMap<>();
    columns.selectList(null).forEach(c -> actual.put(c.getColumnId(), c));
    for (var f : snapshot.getFields()) {
      var c = actual.get(f.getColumnId());
      if (c == null
          || !Boolean.TRUE.equals(c.getAvailable())
          || !Objects.equals(f.getColumnName(), c.getColumnName())
          || !Objects.equals(f.getColumnType(), c.getColumnType())
          || !Objects.equals(f.getNullable(), c.getNullable()))
        throw new ServiceException("查询配置字段失效，请联系管理员重新发布", 409);
    }
    return snapshot;
  }

  /** 对外只返回 UI 所需字段，不暴露数据库列、内部来源或审计。 */
  public Map<String, Object> metadata(String key) {
    return publicMetadata(published(key));
  }

  /** 将可信字段描述转换成安全界面配置。 */
  public Map<String, Object> publicMetadata(QuerySnapshot snapshot) {
    List<Map<String, Object>> fields = new ArrayList<>();
    for (var f : snapshot.getFields()) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("fieldKey", f.getFieldKey());
      item.put("sourceKey", f.getSourceKey());
      item.put("label", f.getLabel());
      item.put("dataType", f.getDataType());
      item.put("component", f.getComponent());
      item.put("operators", f.getOperators());
      item.put("defaultOperator", f.getDefaultOperator());
      item.put("optionProvider", f.getOptionProvider());
      item.put("commonVisible", f.getCommonVisible());
      item.put("displayOrder", f.getDisplayOrder());
      item.put("sortable", f.getSortable());
      item.put("nullable", f.getNullable());
      item.put("maxLength", f.getMaxLength());
      item.put("numericScale", f.getNumericScale());
      fields.add(item);
    }
    return Map.of(
        "queryKey",
        snapshot.getQueryKey(),
        "displayName",
        snapshot.getDisplayName(),
        "configVersion",
        snapshot.getConfigVersion(),
        "fields",
        fields,
        "relations",
        snapshot.getRelations());
  }

  /** 业务入口再次验证发布版本，不允许旧配置静默扩大结果。 */
  public <T> PageResult<T> search(String key, SearchRequest request, Class<T> type) {
    if (request == null) throw new ServiceException("查询参数不能为空", 400);
    var snapshot = published(key);
    if (!Objects.equals(snapshot.getConfigVersion(), request.getConfigVersion()))
      throw new ServiceException("查询配置已更新，请刷新配置后重新查询", 409);
    var result = execute(registry.authorized(key), snapshot, request);
    return new PageResult<>(result.getRows().stream().map(type::cast).toList(), result.getTotal());
  }

  /** 管理预览使用已保存草稿，仍然要求原业务列表权限。 */
  public Map<String, Object> preview(Long id, SearchRequest request) {
    var d = definitions.require(id);
    var adapter = registry.authorized(d.getQueryKey());
    var snapshot = definitions.draft(id);
    return Map.of(
        "metadata", publicMetadata(snapshot), "result", execute(adapter, snapshot, request));
  }

  /** 执行单次查询，不记录原始值或生成 SQL。 */
  private PageResult<?> execute(
      QueryAdapter adapter, QuerySnapshot snapshot, SearchRequest request) {
    long start = System.nanoTime();
    String shape = "invalid";
    try {
      var compiled = compiler.compile(adapter, snapshot, request);
      shape = shape(request.getFilter());
      return adapter.search(compiled, request);
    } catch (org.springframework.dao.QueryTimeoutException e) {
      throw new ServiceException("查询超时，请缩小日期范围或减少复杂条件", 408);
    } catch (org.springframework.dao.DataAccessException e) {
      LOG.error(
          "Query execution failed key={} version={} type={}",
          adapter.key(),
          snapshot.getConfigVersion(),
          e.getClass().getSimpleName());
      throw new ServiceException("查询执行失败，请检查配置是否与数据库结构一致", 400);
    } finally {
      long elapsed = (System.nanoTime() - start) / 1_000_000;
      if (elapsed > 1000)
        LOG.warn(
            "Slow query key={} version={} elapsedMs={} shape={}",
            adapter.key(),
            snapshot.getConfigVersion(),
            elapsed,
            shape);
    }
  }

  /** 选项请求也检查字段已开放和业务列表权限，不依赖维护或参照权限。 */
  public Object options(String key, String field, String keyword, List<Long> ids, Integer page) {
    return optionsSnapshot(registry.authorized(key), published(key), field, keyword, ids, page);
  }

  /** 预览选项使用当前草稿，仍要求业务列表权限。 */
  public Object previewOptions(
      Long id, String field, String keyword, List<Long> ids, Integer page) {
    var d = definitions.require(id);
    return optionsSnapshot(
        registry.authorized(d.getQueryKey()), definitions.draft(id), field, keyword, ids, page);
  }

  /** 对选项参数和已批准提供器统一校验。 */
  private Object optionsSnapshot(
      QueryAdapter adapter,
      QuerySnapshot snapshot,
      String field,
      String keyword,
      List<Long> ids,
      Integer page) {
    var spec =
        snapshot.getFields().stream()
            .filter(f -> f.getFieldKey().equals(field))
            .findFirst()
            .orElseThrow(() -> new ServiceException("查询字段未开放", 400));
    if (spec.getOptionProvider() == null
        || !adapter.optionProviders().contains(spec.getOptionProvider()))
      throw new ServiceException("字段没有选项来源", 400);
    if (keyword != null && keyword.length() > 200) throw new ServiceException("选项关键字过长", 400);
    if (ids != null
        && (ids.isEmpty()
            || ids.size() > 100
            || ids.stream().anyMatch(v -> v == null || v <= 0)
            || new HashSet<>(ids).size() != ids.size()))
      throw new ServiceException("每次须指定 1～100 个不同的有效 ID", 400);
    if (page == null || page < 1 || page > 100000) throw new ServiceException("选项页码无效", 400);
    return adapter.options(spec.getOptionProvider(), keyword, ids, page);
  }

  /** 编译通过后仅记录稳定字段和运算符结构，不记录输入值。 */
  private String shape(com.ruoyi.query.protocol.QueryNode node) {
    if (node == null) return "empty";
    if ("condition".equals(node.getType())) return node.getField() + ":" + node.getOp();
    String children =
        node.getChildren().stream()
            .map(this::shape)
            .collect(java.util.stream.Collectors.joining(","));
    return "relation".equals(node.getType())
        ? node.getMode() + ":" + node.getRelation() + "(" + children + ")"
        : node.getLogic() + "(" + children + ")";
  }
}
