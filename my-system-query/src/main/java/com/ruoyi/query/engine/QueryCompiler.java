package com.ruoyi.query.engine;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Component;

/** 递归编译受限条件树；SQL 标识符来自已发布配置，所有输入值均绑定。 */
@Component
public class QueryCompiler {
  public static final int MAX_CONDITIONS = 30;
  public static final int MAX_DEPTH = 3;
  public static final int MAX_RELATIONS = 3;
  public static final int MAX_SET_SIZE = 100;
  private static final Set<String> DENIED =
      Set.of("del_flag", "version", "password", "salt", "token", "lock_id");
  private static final Set<String> UNARY = Set.of("isNull", "isNotNull", "isEmpty", "isNotEmpty");

  /** 检查字段类型支持的运算符，发布和执行共用。 */
  public static Set<String> operators(FieldSpec f) {
    Set<String> ops = new LinkedHashSet<>(List.of("eq", "ne", "in", "notIn"));
    if ("STRING".equals(f.getDataType())
        && (f.getOptionProvider() == null || f.getOptionProvider().isBlank())) {
      ops.addAll(List.of("like", "startsWith", "endsWith", "isEmpty", "isNotEmpty"));
    }
    if (Set.of("INTEGER", "DECIMAL", "DATE", "DATETIME").contains(f.getDataType())
        && (f.getOptionProvider() == null || f.getOptionProvider().isBlank())) {
      ops.addAll(List.of("gt", "ge", "lt", "le", "between"));
    }
    if (Boolean.TRUE.equals(f.getNullable())) {
      ops.addAll(List.of("isNull", "isNotNull"));
    }
    if ("categories".equals(f.getOptionProvider())) {
      ops.add("descendantOf");
    }
    return ops;
  }

  /** 禁止技术字段和任何 SQL 片段进入字段目录。 */
  public static String identifier(String value) {
    if (value == null
        || !value.matches("[a-zA-Z_][a-zA-Z0-9_]{0,63}")
        || DENIED.contains(value.toLowerCase(Locale.ROOT))) {
      throw error("字段来源不允许查询");
    }
    return value;
  }

  /** 在服务器分页与强制条件外编译用户表达式。 */
  public CompiledQuery compile(
      QueryAdapter adapter, QuerySnapshot snapshot, SearchRequest request) {
    if (request == null
        || request.getPageNum() == null
        || request.getPageNum() < 1
        || request.getPageNum() > 100000
        || request.getPageSize() == null
        || request.getPageSize() < 1
        || request.getPageSize() > 100) {
      throw error("分页参数无效");
    }
    if (!Set.of("asc", "desc")
        .contains(request.getSortDirection() == null ? "" : request.getSortDirection())) {
      throw error("排序方向无效");
    }
    LinkedHashMap<String, FieldSpec> fields = new LinkedHashMap<String, FieldSpec>();
    for (FieldSpec f : snapshot.getFields()) {
      identifier(f.getColumnName());
      if (!adapter.sources().containsKey(f.getSourceKey())
          || fields.putIfAbsent(f.getFieldKey(), f) != null) {
        throw error("查询配置来源无效");
      }
    }
    State state = new State(adapter, fields);
    String expression =
        request.getFilter() == null ? "1=1" : node(request.getFilter(), "main", 1, state);
    String sortKey =
        request.getSortField() == null ? adapter.defaultSort() : request.getSortField();
    FieldSpec sort = fields.get(sortKey);
    if (sort == null
        || !Boolean.TRUE.equals(sort.getSortable())
        || !"main".equals(sort.getSourceKey())) {
      throw error("该字段未开放排序");
    }
    String direction =
        request.getSortField() == null ? adapter.defaultDirection() : request.getSortDirection();
    String root = adapter.sources().get("main").getAlias();
    return new CompiledQuery(
        root + ".del_flag='0' AND (" + expression + ")",
        state.values,
        column(adapter, sort),
        "asc".equals(direction),
        root + ".`" + identifier(adapter.primaryKey()) + "`");
  }

  /** 保持树结构，避免转换为展开式导致条件数量爆炸。 */
  private String node(QueryNode n, String scope, int depth, State s) {
    if (n == null || ("group".equals(n.getType()) && depth > MAX_DEPTH) || ++s.nodes > 70) {
      throw error("查询条件层级或数量超限");
    }
    if ("group".equals(n.getType())) {
      if (!Set.of("AND", "OR").contains(n.getLogic() == null ? "" : n.getLogic())
          || n.getChildren() == null
          || n.getChildren().size() > MAX_CONDITIONS) {
        throw error("条件分组无效");
      }
      if (n.getChildren().isEmpty()) {
        if (depth == 1 && "main".equals(scope) && "AND".equals(n.getLogic())) {
          return "1=1";
        }
        throw error("条件分组不能为空");
      }
      List<String> parts = new ArrayList<>();
      for (QueryNode child : n.getChildren()) {
        parts.add("(" + node(child, scope, depth + 1, s) + ")");
      }
      return String.join(" " + n.getLogic() + " ", parts);
    }
    if ("relation".equals(n.getType())) {
      if (!"main".equals(scope)
          || ++s.relations > MAX_RELATIONS
          || !Set.of("EXISTS", "NOT_EXISTS").contains(n.getMode() == null ? "" : n.getMode())
          || n.getRelation() == null
          || "main".equals(n.getRelation())) {
        throw error("关联条件无效或超限");
      }
      QuerySource source = s.adapter.sources().get(n.getRelation());
      if (source == null
          || source.getCorrelation() == null
          || n.getChildren() == null
          || n.getChildren().isEmpty()) {
        throw error("关联关系未登记或条件为空");
      }
      QueryNode group = new QueryNode();
      group.setType("group");
      group.setLogic(n.getLogic() == null ? "AND" : n.getLogic());
      group.setChildren(n.getChildren());
      String condition = node(group, n.getRelation(), depth, s);
      return ("NOT_EXISTS".equals(n.getMode()) ? "NOT " : "")
          + "EXISTS (SELECT 1 FROM "
          + source.getTable()
          + " "
          + source.getAlias()
          + " WHERE "
          + source.getCorrelation()
          + " AND "
          + source.getAlias()
          + ".del_flag='0' AND ("
          + condition
          + "))";
    }
    if (!"condition".equals(n.getType()) || ++s.conditions > MAX_CONDITIONS) {
      throw error("查询条件类型或数量无效");
    }
    FieldSpec f = s.fields.get(n.getField());
    if (f == null
        || !scope.equals(f.getSourceKey())
        || n.getOp() == null
        || !f.getOperators().contains(n.getOp())
        || !operators(f).contains(n.getOp())) {
      throw error("字段或运算符未开放");
    }
    String c = column(s.adapter, f), op = n.getOp();
    if (UNARY.contains(op)) {
      if (n.getValue() != null || n.getValue2() != null) {
        throw error("空值操作不接受比较值");
      }
      return c
          + switch (op) {
            case "isNull" -> " IS NULL";
            case "isNotNull" -> " IS NOT NULL";
            case "isEmpty" -> " = ''";
            default -> " <> ''";
          };
    }
    if ("in".equals(op) || "notIn".equals(op)) {
      if (!(n.getValue() instanceof List<?> list) || list.isEmpty() || list.size() > MAX_SET_SIZE) {
        throw error("集合须包含 1～100 个值");
      }
      List<String> params = new ArrayList<>();
      for (Object v : list) {
        params.add(s.bind(value(f, v, s.adapter)));
      }
      return c + ("notIn".equals(op) ? " NOT IN (" : " IN (") + String.join(",", params) + ")";
    }
    Object first = value(f, n.getValue(), s.adapter);
    if ("descendantOf".equals(op)) {
      List<?> ids = s.adapter.descendants(first);
      if (ids.isEmpty()) {
        return "1=0";
      }
      if (ids.size() > 10000) {
        throw error("分类范围过大，请缩小查询范围");
      }
      return c + " IN (" + String.join(",", ids.stream().map(s::bind).toList()) + ")";
    }
    if ("between".equals(op)) {
      Object second = value(f, n.getValue2(), s.adapter);
      if (compare(first, second) > 0) {
        throw error("区间起始值不能大于结束值");
      }
      return c + " BETWEEN " + s.bind(first) + " AND " + s.bind(second);
    }
    if (n.getValue2() != null) {
      throw error("该操作不接受第二个值");
    }
    if (Set.of("like", "startsWith", "endsWith").contains(op)) {
      String text = first.toString().replace("!", "!!").replace("%", "!%").replace("_", "!_");
      return c
          + " LIKE "
          + s.bind(("startsWith".equals(op) ? "" : "%") + text + ("endsWith".equals(op) ? "" : "%"))
          + " ESCAPE '!'";
    }
    String symbol =
        switch (op) {
          case "eq" -> "=";
          case "ne" -> "<>";
          case "gt" -> ">";
          case "ge" -> ">=";
          case "lt" -> "<";
          case "le" -> "<=";
          default -> throw error("不支持的运算符");
        };
    return c + " " + symbol + " " + s.bind(first);
  }

  /** 严格转换值，不接受对象、隐式截断、科学计数法或超精度小数。 */
  private Object value(FieldSpec f, Object input, QueryAdapter adapter) {
    if (input == null
        || input instanceof Map
        || input instanceof Collection
        || !(input instanceof String || input instanceof Number || input instanceof Boolean)) {
      throw error("查询值类型无效");
    }
    String text = input.toString().strip();
    if (text.isEmpty()
        || text.length() > Math.min(f.getMaxLength() == null ? 500 : f.getMaxLength(), 2000)) {
      throw error("查询值为空或长度超限");
    }
    try {
      Object result =
          switch (f.getDataType()) {
            case "INTEGER" -> {
              if (!text.matches("-?[0-9]{1,19}")) {
                throw error("请输入整数");
              }
              yield Long.valueOf(text);
            }
            case "DECIMAL" -> {
              if (!(input instanceof String) || !text.matches("-?[0-9]{1,20}(\\.[0-9]{1,6})?")) {
                throw error("小数须使用十进制字符串");
              }
              BigDecimal v = new BigDecimal(text);
              if (f.getNumericScale() != null && v.scale() > f.getNumericScale()) {
                throw error("小数精度超限");
              }
              if (f.getNumericPrecision() != null
                  && v.precision() - v.scale()
                      > f.getNumericPrecision()
                          - (f.getNumericScale() == null ? 0 : f.getNumericScale())) {
                throw error("查询值超出字段数值范围");
              }
              yield v;
            }
            case "BOOLEAN" -> {
              if (!(input instanceof Boolean)) {
                throw error("请输入布尔值");
              }
              yield input;
            }
            case "DATE" -> LocalDate.parse(text);
            case "DATETIME" ->
                LocalDateTime.parse(
                    text,
                    DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                        .withResolverStyle(java.time.format.ResolverStyle.STRICT));
            case "STRING" -> {
              if (!(input instanceof String)) {
                throw error("请输入文本");
              }
              yield text;
            }
            default -> throw error("字段类型不支持查询");
          };
      adapter.validateValue(f, result);
      return result;
    } catch (java.time.DateTimeException | NumberFormatException ex) {
      throw error("查询值格式不正确");
    }
  }

  /** 比较同类型区间端点。 */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private int compare(Object a, Object b) {
    return ((Comparable) a).compareTo(b);
  }

  /** 只使用登记别名和已验证字段名。 */
  private String column(QueryAdapter a, FieldSpec f) {
    return a.sources().get(f.getSourceKey()).getAlias()
        + ".`"
        + identifier(f.getColumnName())
        + "`";
  }

  /** 返回统一参数错误。 */
  private static ServiceException error(String message) {
    return new ServiceException(message, 400);
  }

  /** 每次编译隔离计数和绑定值，禁止在线程间复用。 */
  private static final class State {
    private final QueryAdapter adapter;
    private final Map<String, FieldSpec> fields;
    private final List<Object> values = new ArrayList<>();
    private int nodes, conditions, relations;

    /** 建立本次请求的编译上下文。 */
    State(QueryAdapter adapter, Map<String, FieldSpec> fields) {
      this.adapter = adapter;
      this.fields = fields;
    }

    /** 生成 MP 参数占位符，值永不进入 SQL。 */
    String bind(Object value) {
      values.add(value);
      return "{" + (values.size() - 1) + "}";
    }
  }
}
