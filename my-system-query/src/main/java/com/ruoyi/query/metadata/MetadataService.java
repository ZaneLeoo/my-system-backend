package com.ruoyi.query.metadata;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.mapper.*;
import com.ruoyi.query.spi.QueryRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 当前数据库业务表目录，所有结构读取使用绑定参数且不执行 DDL。 */
@Service
public class MetadataService {
  private final JdbcTemplate jdbc;
  private final MetadataTableMapper tables;
  private final MetadataColumnMapper columns;
  private final QueryRegistry registry;

  /** 注入目录和只读结构访问。 */
  public MetadataService(
      JdbcTemplate jdbc,
      MetadataTableMapper tables,
      MetadataColumnMapper columns,
      QueryRegistry registry) {
    this.jdbc = jdbc;
    this.tables = tables;
    this.columns = columns;
    this.registry = registry;
  }

  /** 列出代码允许导入的实际业务表。 */
  public List<Map<String, Object>> candidates() {
    List<Map<String, Object>> result = new ArrayList<>();
    for (String name : registry.tables())
      result.addAll(
          jdbc.queryForList(
              "SELECT TABLE_NAME AS tableName,TABLE_COMMENT AS tableComment FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' AND TABLE_NAME=?",
              name));
    return result;
  }

  /** 返回已导入目录。 */
  public List<MetadataTable> list() {
    return tables.selectList(new QueryWrapper<MetadataTable>().orderByAsc("table_name"));
  }

  /** 查找有效目录。 */
  public MetadataTable require(Long id) {
    var t = tables.selectById(id);
    if (t == null) throw new ServiceException("数据表目录不存在");
    return t;
  }

  /** 返回字段，包括失效字段，便于检查引用。 */
  public List<MetadataColumn> columns(Long id) {
    require(id);
    return columns.selectList(
        new QueryWrapper<MetadataColumn>().eq("table_id", id).orderByAsc("ordinal_position"));
  }

  /** 在同一事务导入指定表，重复导入不覆盖人工配置。 */
  @Transactional(rollbackFor = Exception.class)
  public MetadataTable importTable(String name) {
    if (!registry.tables().contains(name)) throw new ServiceException("该表未登记为可导入业务表", 400);
    String schema = jdbc.queryForObject("SELECT DATABASE()", String.class);
    var found =
        tables.selectOne(
            new QueryWrapper<MetadataTable>().eq("schema_name", schema).eq("table_name", name));
    if (found != null) return found;
    var available =
        candidates().stream()
            .filter(t -> name.equals(t.get("tableName")))
            .findFirst()
            .orElseThrow(() -> new ServiceException("业务表不存在"));
    var t = new MetadataTable();
    t.setSchemaName(schema);
    t.setTableName(name);
    t.setTableComment(Objects.toString(available.get("tableComment"), ""));
    t.setDisplayName(
        t.getTableComment().isBlank()
            ? name
            : t.getTableComment().substring(0, Math.min(100, t.getTableComment().length())));
    t.setAvailable(true);
    t.setVersion(0);
    t.setSynchronizedTime(
        LocalDateTime.now(com.ruoyi.common.constant.DateTimeConstants.BUSINESS_ZONE));
    var physical = physical(name);
    t.setStructureSignature(signature(physical));
    tables.insert(t);
    for (var c : physical) {
      c.setTableId(t.getTableId());
      columns.insert(c);
    }
    return t;
  }

  /** 生成完整结构差异及确认签名，不改变目录。 */
  public Map<String, Object> preview(Long id) {
    var t = require(id);
    var actual = physical(t.getTableName());
    var old = columns(id);
    Map<String, MetadataColumn> indexed = new HashMap<>();
    old.forEach(c -> indexed.put(c.getColumnName(), c));
    List<MetadataColumn> added = new ArrayList<>(), changed = new ArrayList<>();
    Set<String> names = new HashSet<>();
    for (var c : actual) {
      names.add(c.getColumnName());
      var previous = indexed.get(c.getColumnName());
      if (previous == null) added.add(c);
      else if (!same(previous, c) || !Boolean.TRUE.equals(previous.getAvailable())) changed.add(c);
    }
    var removed =
        old.stream()
            .filter(
                c -> Boolean.TRUE.equals(c.getAvailable()) && !names.contains(c.getColumnName()))
            .toList();
    List<Map<String, Object>> references =
        jdbc.queryForList(
            "SELECT DISTINCT d.query_key AS queryKey,d.display_name AS displayName FROM sys_query_definition d JOIN sys_query_field f ON f.definition_id=d.definition_id JOIN sys_metadata_column c ON c.column_id=f.column_id WHERE c.table_id=?",
            id);
    return Map.of(
        "version",
        t.getVersion(),
        "signature",
        signature(actual),
        "added",
        added,
        "changed",
        changed,
        "removed",
        removed,
        "references",
        references);
  }

  /** 持锁复核预览签名，保留名称和字段 ID，删除项仅标记失效。 */
  @Transactional(rollbackFor = Exception.class)
  public MetadataTable synchronize(Long id, Integer version, String expectedSignature) {
    var t =
        tables.selectOne(new QueryWrapper<MetadataTable>().eq("table_id", id).last("FOR UPDATE"));
    if (t == null || !Objects.equals(t.getVersion(), version))
      throw new ServiceException("目录已更新，请重新预览", 409);
    var physical = physical(t.getTableName());
    String signature = signature(physical);
    if (!signature.equals(expectedSignature)) throw new ServiceException("数据库结构再次变化，请重新预览", 409);
    Map<String, MetadataColumn> indexed = new HashMap<>();
    columns(id).forEach(c -> indexed.put(c.getColumnName(), c));
    for (var c : physical) {
      var old = indexed.remove(c.getColumnName());
      c.setTableId(id);
      if (old == null) columns.insert(c);
      else {
        c.setColumnId(old.getColumnId());
        c.setDisplayName(old.getDisplayName());
        columns.updateById(c);
      }
    }
    for (var c : indexed.values()) {
      c.setAvailable(false);
      columns.updateById(c);
    }
    t.setVersion(Math.incrementExact(t.getVersion()));
    t.setStructureSignature(signature);
    t.setAvailable(!physical.isEmpty());
    t.setSynchronizedTime(
        LocalDateTime.now(com.ruoyi.common.constant.DateTimeConstants.BUSINESS_ZONE));
    tables.updateById(t);
    return t;
  }

  /** 使用受控字段更新人工名称，不修改数据库注释。 */
  @Transactional(rollbackFor = Exception.class)
  public void rename(Long id, Integer version, String label, Long columnId) {
    if (label == null || label.isBlank() || label.strip().length() > 100)
      throw new ServiceException("显示名称须为 1～100 个字符", 400);
    var t =
        tables.selectOne(new QueryWrapper<MetadataTable>().eq("table_id", id).last("FOR UPDATE"));
    if (t == null || !Objects.equals(t.getVersion(), version))
      throw new ServiceException("目录版本已变化", 409);
    if (columnId == null) t.setDisplayName(label.strip());
    else {
      var c = columns.selectById(columnId);
      if (c == null || !id.equals(c.getTableId())) throw new ServiceException("字段不属于当前数据表", 400);
      c.setDisplayName(label.strip());
      columns.updateById(c);
    }
    t.setVersion(Math.incrementExact(t.getVersion()));
    tables.updateById(t);
  }

  /** 一次读取字段结构；表名也是绑定值。 */
  private List<MetadataColumn> physical(String name) {
    if (!registry.tables().contains(name)) throw new ServiceException("数据来源未登记", 400);
    return jdbc.query(
        "SELECT COLUMN_NAME,COLUMN_COMMENT,DATA_TYPE,COLUMN_TYPE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_SCALE,IS_NULLABLE,COLUMN_KEY,ORDINAL_POSITION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? ORDER BY ORDINAL_POSITION",
        (rs, n) -> {
          var c = new MetadataColumn();
          c.setColumnName(rs.getString(1));
          c.setColumnComment(rs.getString(2));
          c.setDisplayName(
              c.getColumnComment().isBlank()
                  ? c.getColumnName()
                  : c.getColumnComment()
                      .substring(0, Math.min(100, c.getColumnComment().length())));
          c.setDataType(type(rs.getString(3), rs.getString(4)));
          c.setColumnType(rs.getString(4));
          c.setMaxLength(rs.getObject(5) == null ? null : rs.getLong(5));
          c.setNumericScale(rs.getObject(6) == null ? null : rs.getInt(6));
          c.setNullable("YES".equals(rs.getString(7)));
          c.setPrimaryKey("PRI".equals(rs.getString(8)));
          c.setOrdinalPosition(rs.getInt(9));
          c.setAvailable(true);
          return c;
        },
        name);
  }

  /** 仅登记一期支持的物理类型。 */
  public static String type(String type, String full) {
    return switch (type.toLowerCase(Locale.ROOT)) {
      case "varchar", "char", "text" -> "STRING";
      case "tinyint" -> full.equalsIgnoreCase("tinyint(1)") ? "BOOLEAN" : "INTEGER";
      case "int", "bigint", "smallint", "mediumint" -> "INTEGER";
      case "decimal" -> "DECIMAL";
      case "date" -> "DATE";
      case "datetime", "timestamp" -> "DATETIME";
      default -> "UNSUPPORTED";
    };
  }

  /** 结构变化不受人工显示名称影响。 */
  private boolean same(MetadataColumn a, MetadataColumn b) {
    return Objects.equals(a.getColumnType(), b.getColumnType())
        && Objects.equals(a.getNullable(), b.getNullable())
        && Objects.equals(a.getColumnComment(), b.getColumnComment())
        && Objects.equals(a.getOrdinalPosition(), b.getOrdinalPosition());
  }

  /** 对有序物理结构生成稳定签名。 */
  public static String signature(List<MetadataColumn> values) {
    try {
      StringBuilder text = new StringBuilder();
      for (var c : values)
        text.append(c.getColumnName())
            .append('|')
            .append(c.getColumnType())
            .append('|')
            .append(c.getNullable())
            .append('|')
            .append(c.getColumnComment())
            .append('|')
            .append(c.getOrdinalPosition())
            .append(';');
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(text.toString().getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
