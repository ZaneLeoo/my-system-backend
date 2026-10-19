package com.ruoyi.query.definition;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.engine.QueryCompiler;
import com.ruoyi.query.mapper.*;
import com.ruoyi.query.metadata.*;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.util.*;
import java.util.Map.Entry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 草稿和不可变发布版本，所有字段修改受整份草稿版本保护。 */
@Service
public class DefinitionService {
  private final QueryDefinitionMapper definitions;
  private final QueryFieldMapper fields;
  private final QueryReleaseMapper releases;
  private final MetadataColumnMapper columns;
  private final MetadataService metadata;
  private final QueryRegistry registry;

  /** 注入配置持久化与业务能力注册。 */
  public DefinitionService(
      QueryDefinitionMapper definitions,
      QueryFieldMapper fields,
      QueryReleaseMapper releases,
      MetadataColumnMapper columns,
      MetadataService metadata,
      QueryRegistry registry) {
    this.definitions = definitions;
    this.fields = fields;
    this.releases = releases;
    this.columns = columns;
    this.metadata = metadata;
    this.registry = registry;
  }

  /** 返回已接入对象，新增对象由开发者迁移登记。 */
  public List<QueryDefinition> list() {
    return definitions.selectList(new QueryWrapper<QueryDefinition>().orderByAsc("definition_id"));
  }

  /** 返回数据目录与已接入查询对象的关联，包含预设明细来源。 */
  public List<Map<String, Object>> tableLinks() {
    return tableLinks(null);
  }

  /** 按当前目录页限制关联结果，旧调用未传 ID 时保留完整目录行为。 */
  public List<Map<String, Object>> tableLinks(List<Long> ids) {
    if (ids != null && (ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0))) {
      throw new ServiceException("目录 ID 每次最多 100 个且必须为正整数", 400);
    }
    Map<String, Long> tableIds = new HashMap<>();
    List<MetadataTable> tables = ids == null ? metadata.list() : metadata.findByIds(ids);
    tables.forEach(table -> tableIds.put(table.getTableName(), table.getTableId()));
    List<Map<String, Object>> links = new ArrayList<>();
    for (QueryDefinition definition : list()) {
      QueryAdapter adapter = registry.require(definition.getAdapterKey());
      for (Entry<String, QuerySource> source : adapter.sources().entrySet()) {
        Long tableId = tableIds.get(source.getValue().getTable());
        if (tableId == null) {
          continue;
        }
        links.add(
            Map.of(
                "tableId", tableId,
                "definitionId", definition.getDefinitionId(),
                "displayName", definition.getDisplayName(),
                "sourceLabel", source.getValue().getLabel()));
      }
    }
    return links;
  }

  /** 按 ID 返回查询对象。 */
  public QueryDefinition require(Long id) {
    QueryDefinition d = definitions.selectById(id);
    if (d == null) {
      throw new ServiceException("查询配置不存在");
    }
    return d;
  }

  /** 按稳定编码返回查询对象。 */
  public QueryDefinition byKey(String key) {
    QueryDefinition d =
        definitions.selectOne(new QueryWrapper<QueryDefinition>().eq("query_key", key));
    if (d == null) {
      throw new ServiceException("查询配置未初始化");
    }
    return d;
  }

  /** 返回字段草稿，保留停用字段标识防止重复使用。 */
  public List<QueryField> fields(Long id) {
    require(id);
    return fields.selectList(
        new QueryWrapper<QueryField>()
            .eq("definition_id", id)
            .orderByAsc("display_order", "field_id"));
  }

  /** 提供当前对象的来源分组和可选字段，禁止任意选表。 */
  public List<Map<String, Object>> sources(Long id) {
    QueryDefinition d = require(id);
    QueryAdapter adapter = registry.require(d.getAdapterKey());
    List<Map<String, Object>> result = new ArrayList<>();
    for (Entry<String, QuerySource> source : adapter.sources().entrySet()) {
      Optional<MetadataTable> table =
          metadata.list().stream()
              .filter(t -> t.getTableName().equals(source.getValue().getTable()))
              .findFirst();
      if (table.isEmpty()) {
        continue;
      }
      List<MetadataColumn> available =
          metadata.columns(table.get().getTableId()).stream()
              .filter(
                  c ->
                      Boolean.TRUE.equals(c.getAvailable())
                          && !"UNSUPPORTED".equals(c.getDataType()))
              .filter(
                  c -> {
                    try {
                      QueryCompiler.identifier(c.getColumnName());
                      return true;
                    } catch (ServiceException e) {
                      return false;
                    }
                  })
              .toList();
      result.add(
          Map.of(
              "key",
              source.getKey(),
              "label",
              source.getValue().getLabel(),
              "columns",
              available,
              "optionProviders",
              available.stream()
                  .collect(
                      java.util.stream.Collectors.toMap(
                          MetadataColumn::getColumnName,
                          c -> provider(adapter, source.getKey(), c)))));
    }
    return result;
  }

  /** 返回允许选择的选项提供器。 */
  public Set<String> providers(Long id) {
    return registry.require(require(id).getAdapterKey()).optionProviders();
  }

  /**
   * 按当前编辑版本保存整份查询草稿，并保留已有字段标识及来源绑定。
   *
   * @param id 查询定义 ID
   * @param request 查询名称、启停状态、完整字段集合及编辑时的草稿版本
   * @return 草稿版本递增后的查询定义；当前发布快照不受影响
   * @throws ServiceException 编辑版本过期、字段标识不合法或试图换绑已有来源时
   */
  @Transactional(rollbackFor = Exception.class)
  public QueryDefinition save(Long id, SaveDefinitionRequest request) {
    QueryDefinition d = lock(id, request.getVersion());
    if (request.getDisplayName() == null
        || request.getDisplayName().isBlank()
        || request.getDisplayName().length() > 100
        || request.getFields() == null
        || request.getFields().size() > 100) {
      throw new ServiceException("查询名称或字段数量无效", 400);
    }
    Map<String, QueryField> existing = new HashMap<>();
    fields(id).forEach(f -> existing.put(f.getFieldKey(), f));
    Set<String> keys = new HashSet<>();
    for (SaveFieldRequest input : request.getFields()) {
      if (input.getFieldKey() == null
          || !input.getFieldKey().matches("[a-zA-Z][a-zA-Z0-9]{0,63}")
          || !keys.add(input.getFieldKey())) {
        throw new ServiceException("字段标识无效或重复", 400);
      }
      QueryField previous = existing.remove(input.getFieldKey());
      if (input.getFieldId() != null
          && (previous == null || !input.getFieldId().equals(previous.getFieldId()))) {
        throw new ServiceException("字段标识不可更改", 400);
      }
      if (previous != null
          && (!Objects.equals(previous.getSourceKey(), input.getSourceKey())
              || !Objects.equals(previous.getColumnId(), input.getColumnId()))) {
        throw new ServiceException("已有字段不可更换数据来源，请停用后新增字段标识", 400);
      }
      QueryField f = new QueryField();
      f.setDefinitionId(id);
      f.setFieldKey(input.getFieldKey());
      f.setSourceKey(input.getSourceKey());
      f.setColumnId(input.getColumnId());
      f.setLabel(input.getLabel());
      f.setComponent(input.getComponent());
      f.setOperatorsJson(input.getOperatorsJson());
      f.setDefaultOperator(input.getDefaultOperator());
      f.setOptionProvider(input.getOptionProvider() == null ? "" : input.getOptionProvider());
      f.setCommonVisible(Boolean.TRUE.equals(input.getCommonVisible()));
      f.setDisplayOrder(input.getDisplayOrder() == null ? 0 : input.getDisplayOrder());
      f.setSortable(Boolean.TRUE.equals(input.getSortable()));
      f.setEnabled(Boolean.TRUE.equals(input.getEnabled()));
      if (previous == null) {
        fields.insert(f);
      } else {
        f.setFieldId(previous.getFieldId());
        fields.updateById(f);
      }
    }
    for (QueryField old : existing.values()) {
      old.setEnabled(false);
      fields.updateById(old);
    }
    d.setDisplayName(request.getDisplayName().strip());
    d.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
    d.setDraftVersion(Math.incrementExact(d.getDraftVersion()));
    definitions.updateById(d);
    return d;
  }

  /**
   * 解析并验证当前已保存草稿，为预览及发布生成受控配置快照。
   *
   * <p>验证字段真实来源、类型、控件、运算符、选项语义和默认排序；只纳入启用字段，不生成发布记录。
   *
   * @param id 查询定义 ID
   * @return 已完成配置校验的当前草稿快照
   * @throws ServiceException 字段目录失效、来源不匹配或配置超出适配器能力时
   */
  public QuerySnapshot draft(Long id) {
    QueryDefinition d = require(id);
    QueryAdapter adapter = registry.require(d.getAdapterKey());
    QuerySnapshot snapshot = new QuerySnapshot();
    snapshot.setEnabled(d.getEnabled());
    snapshot.setQueryKey(d.getQueryKey());
    snapshot.setDisplayName(d.getDisplayName());
    snapshot.setConfigVersion(d.getPublishedRevision());
    adapter
        .sources()
        .forEach(
            (key, s) -> {
              if (!"main".equals(key)) {
                snapshot.getRelations().put(key, s.getLabel());
              }
            });
    Map<Long, MetadataColumn> allColumns = new HashMap<>();
    columns.selectList(null).forEach(c -> allColumns.put(c.getColumnId(), c));
    Map<Long, MetadataTable> allTables = new HashMap<>();
    metadata.list().forEach(t -> allTables.put(t.getTableId(), t));
    for (QueryField f : fields(id)) {
      if (Boolean.TRUE.equals(f.getEnabled())) {
        QuerySource source = adapter.sources().get(f.getSourceKey());
        MetadataColumn col = allColumns.get(f.getColumnId());
        MetadataTable table = col == null ? null : allTables.get(col.getTableId());
        if (source == null
            || col == null
            || table == null
            || !Boolean.TRUE.equals(col.getAvailable())
            || !Boolean.TRUE.equals(table.getAvailable())
            || !source.getTable().equals(table.getTableName())) {
          throw new ServiceException("字段来源失效：" + f.getFieldKey(), 400);
        }
        QueryCompiler.identifier(col.getColumnName());
        if (f.getLabel() == null || f.getLabel().isBlank() || f.getLabel().length() > 100) {
          throw new ServiceException("字段显示名称无效", 400);
        }
        FieldSpec spec = new FieldSpec();
        spec.setFieldKey(f.getFieldKey());
        spec.setSourceKey(f.getSourceKey());
        spec.setColumnId(col.getColumnId());
        spec.setColumnName(col.getColumnName());
        spec.setColumnType(col.getColumnType());
        spec.setDataType(col.getDataType());
        spec.setMaxLength(col.getMaxLength());
        spec.setNumericScale(col.getNumericScale());
        spec.setNullable(col.getNullable());
        spec.setLabel(f.getLabel());
        spec.setComponent(f.getComponent());
        spec.setOptionProvider(f.getOptionProvider());
        spec.setCommonVisible(f.getCommonVisible());
        spec.setDisplayOrder(f.getDisplayOrder());
        spec.setSortable(f.getSortable());
        spec.setDefaultOperator(f.getDefaultOperator());
        try {
          spec.setOperators(JSON.parseArray(f.getOperatorsJson(), String.class));
        } catch (Exception e) {
          throw new ServiceException("运算符配置格式无效", 400);
        }
        if (spec.getOperators() == null
            || spec.getOperators().isEmpty()
            || !QueryCompiler.operators(spec).containsAll(spec.getOperators())
            || !spec.getOperators().contains(spec.getDefaultOperator())) {
          throw new ServiceException("字段运算符与类型不匹配：" + f.getLabel(), 400);
        }
        String expectedProvider = provider(adapter, f.getSourceKey(), col);
        if (!expectedProvider.equals(Objects.toString(spec.getOptionProvider(), ""))) {
          throw new ServiceException("字段选项来源与业务语义不匹配：" + f.getLabel(), 400);
        }
        boolean ref = spec.getOptionProvider() != null && !spec.getOptionProvider().isBlank();
        if (ref && !adapter.optionProviders().contains(spec.getOptionProvider())) {
          throw new ServiceException("选项来源未登记", 400);
        }
        Set<String> controls =
            switch (spec.getDataType()) {
              case "STRING" -> Set.of("TEXT", "SELECT");
              case "INTEGER" -> Set.of("NUMBER", "REFERENCE", "SELECT");
              case "DECIMAL" -> Set.of("NUMBER");
              case "BOOLEAN" -> Set.of("SELECT");
              case "DATE" -> Set.of("DATE");
              case "DATETIME" -> Set.of("DATETIME");
              default -> Set.of();
            };
        if (!controls.contains(spec.getComponent())
            || (Set.of("SELECT", "REFERENCE").contains(spec.getComponent()) && !ref)
            || (ref && !Set.of("SELECT", "REFERENCE").contains(spec.getComponent()))) {
          throw new ServiceException("控件与字段类型不匹配：" + f.getLabel(), 400);
        }
        if (Boolean.TRUE.equals(spec.getSortable()) && !"main".equals(spec.getSourceKey())) {
          throw new ServiceException("明细字段不支持排序", 400);
        }
        if (Boolean.TRUE.equals(spec.getCommonVisible()) && !"main".equals(spec.getSourceKey())) {
          throw new ServiceException("明细条件请在高级查询关联组中配置", 400);
        }
        snapshot.getFields().add(spec);
      }
    }
    if (snapshot.getFields().stream()
        .noneMatch(
            f ->
                f.getFieldKey().equals(adapter.defaultSort())
                    && Boolean.TRUE.equals(f.getSortable()))) {
      throw new ServiceException("默认排序字段必须启用并允许排序", 400);
    }
    return snapshot;
  }

  /**
   * 持锁检查草稿版本及真实数据库结构，在同一事务发布不可变快照。
   *
   * @param id 查询定义 ID
   * @param version 当前草稿编辑版本，不是已发布配置版本
   * @return 新建的发布记录，当前发布指针及草稿版本同步递增
   * @throws ServiceException 编辑版本过期、草稿配置无效或物理结构尚未同步时
   */
  @Transactional(rollbackFor = Exception.class)
  public QueryRelease publish(Long id, Integer version) {
    QueryDefinition d = lock(id, version);
    QuerySnapshot snapshot = draft(id);
    for (QuerySource source : registry.require(d.getAdapterKey()).sources().values()) {
      MetadataTable table =
          metadata.list().stream()
              .filter(t -> source.getTable().equals(t.getTableName()))
              .findFirst()
              .orElseThrow();
      Map<String, Object> diff = metadata.preview(table.getTableId());
      if (!((List<?>) diff.get("added")).isEmpty()
          || !((List<?>) diff.get("changed")).isEmpty()
          || !((List<?>) diff.get("removed")).isEmpty()) {
        throw new ServiceException("数据库结构已变化，请先同步数据目录", 409);
      }
    }
    int next = Math.incrementExact(d.getPublishedRevision());
    snapshot.setConfigVersion(next);
    QueryRelease release = new QueryRelease();
    release.setDefinitionId(id);
    release.setRevision(next);
    release.setConfigSnapshot(JSON.toJSONString(snapshot));
    release.setStructureSignature(
        snapshot.getFields().stream()
            .map(f -> f.getColumnId() + ":" + f.getColumnType())
            .sorted()
            .reduce("", (a, b) -> a + ";" + b));
    releases.insert(release);
    d.setPublishedRevision(next);
    d.setDraftVersion(Math.incrementExact(d.getDraftVersion()));
    definitions.updateById(d);
    return release;
  }

  /** 发布记录不允许修改或删除。 */
  public List<QueryRelease> releases(Long id) {
    require(id);
    return releases.selectList(
        new QueryWrapper<QueryRelease>().eq("definition_id", id).orderByDesc("revision"));
  }

  /** 在事务内锁定整份配置并比较编辑版本。 */
  private QueryDefinition lock(Long id, Integer version) {
    QueryDefinition d =
        definitions.selectOne(
            new QueryWrapper<QueryDefinition>().eq("definition_id", id).last("FOR UPDATE"));
    if (d == null || !Objects.equals(d.getDraftVersion(), version)) {
      throw new ServiceException("配置已被修改，请刷新后重试", 409);
    }
    return d;
  }

  /** 普通布尔字段可直接使用内置选项，其余业务引用由适配器绑定。 */
  private String provider(QueryAdapter adapter, String source, MetadataColumn column) {
    String explicit =
        Objects.toString(adapter.fieldOptionProvider(source, column.getColumnName()), "");
    return explicit.isBlank() && "BOOLEAN".equals(column.getDataType()) ? "boolean" : explicit;
  }
}
