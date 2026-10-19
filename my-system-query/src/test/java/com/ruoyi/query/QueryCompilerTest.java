package com.ruoyi.query;

import static org.junit.jupiter.api.Assertions.*;

import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.engine.*;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;

/** 用真实 SQL 执行验证逻辑分组、关联语义和参数边界。 */
class QueryCompilerTest {
  private final QueryCompiler compiler = new QueryCompiler();
  private QuerySnapshot snapshot;
  private QueryAdapter adapter;
  private Connection connection;

  /** 建立含重复明细、删除单据和空值的隔离夹具。 */
  @BeforeEach
  void setup() throws Exception {
    connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL");
    try (var s = connection.createStatement()) {
      s.execute(
          "CREATE TABLE headers(id BIGINT,name VARCHAR(100),amount DECIMAL(18,2),flag BOOLEAN,business_date DATE,del_flag CHAR(1))");
      s.execute(
          "CREATE TABLE lines(id BIGINT,parent_id BIGINT,name VARCHAR(100),quantity DECIMAL(15,6),del_flag CHAR(1))");
      s.execute(
          "INSERT INTO headers VALUES(1,'台灯',0,false,'2026-10-19','0'),(2,'灯_100%',5,true,'2026-10-18','0'),(3,NULL,6,true,'2026-10-17','0'),(4,'台灯',5,true,'2026-10-16','2')");
      s.execute(
          "INSERT INTO lines VALUES(1,1,'台灯',10,'0'),(2,1,'适配器',200,'0'),(3,2,'台灯',150,'0'),(4,2,'台灯',180,'0'),(5,3,'台灯',500,'2')");
    }
    adapter =
        new QueryAdapter() {
          public String key() {
            return "test";
          }

          public String permission() {
            return "test:list";
          }

          public Map<String, QuerySource> sources() {
            return Map.of(
                "main",
                new QuerySource("headers", "h", "主表", null),
                "lines",
                new QuerySource("lines", "l", "明细", "l.parent_id=h.id"));
          }

          public String primaryKey() {
            return "id";
          }

          public String defaultSort() {
            return "name";
          }

          public String defaultDirection() {
            return "asc";
          }

          public PageResult<?> search(CompiledQuery c, SearchRequest r) {
            throw new UnsupportedOperationException();
          }
        };
    snapshot = new QuerySnapshot();
    snapshot.setFields(
        List.of(
            field("name", "main", "name", "STRING", true),
            field("amount", "main", "amount", "DECIMAL", false),
            field("flag", "main", "flag", "BOOLEAN", false),
            field("day", "main", "business_date", "DATE", false),
            field("lineName", "lines", "name", "STRING", false),
            field("quantity", "lines", "quantity", "DECIMAL", false)));
  }

  /** 关闭隔离库。 */
  @AfterEach
  void close() throws Exception {
    connection.close();
  }

  /** 构造受控字段描述。 */
  private FieldSpec field(String key, String source, String column, String type, boolean sort) {
    var f = new FieldSpec();
    f.setFieldKey(key);
    f.setSourceKey(source);
    f.setColumnName(column);
    f.setDataType(type);
    f.setNumericScale(2);
    f.setNullable(true);
    f.setSortable(sort);
    f.setOperators(new ArrayList<>(QueryCompiler.operators(f)));
    return f;
  }

  /** 构造普通条件。 */
  private QueryNode condition(String field, String op, Object value) {
    var n = new QueryNode();
    n.setType("condition");
    n.setField(field);
    n.setOp(op);
    n.setValue(value);
    return n;
  }

  /** 构造显式逻辑组。 */
  private QueryNode group(String logic, QueryNode... children) {
    var n = new QueryNode();
    n.setType("group");
    n.setLogic(logic);
    n.setChildren(List.of(children));
    return n;
  }

  /** 构造同一行明细条件。 */
  private QueryNode relation(String mode, QueryNode... children) {
    var n = group("AND", children);
    n.setType("relation");
    n.setRelation("lines");
    n.setMode(mode);
    return n;
  }

  /** 编译并用绑定参数执行，不把测试值拼入 SQL。 */
  private List<Long> execute(QueryNode filter) throws Exception {
    var request = new SearchRequest();
    request.setFilter(filter);
    var compiled = compiler.compile(adapter, snapshot, request);
    String sql = compiled.getSql().replaceAll("\\{[0-9]+}", "?");
    try (var stmt =
        connection.prepareStatement("SELECT h.id FROM headers h WHERE " + sql + " ORDER BY h.id")) {
      int index = 0;
      for (var value : compiled.getParameters()) stmt.setObject(++index, value);
      List<Long> ids = new ArrayList<>();
      try (var rows = stmt.executeQuery()) {
        while (rows.next()) ids.add(rows.getLong(1));
      }
      return ids;
    }
  }

  /** 同一行要求不会把不同行的条件拼成误命中。 */
  @Test
  void sameLineAndNoDuplicate() throws Exception {
    assertEquals(
        List.of(2L),
        execute(
            relation(
                "EXISTS",
                condition("lineName", "like", "台灯"),
                condition("quantity", "gt", "100"))));
  }

  /** 独立关系组允许分别命中两行。 */
  @Test
  void differentLines() throws Exception {
    assertEquals(
        List.of(1L),
        execute(
            group(
                "AND",
                relation("EXISTS", condition("lineName", "eq", "台灯")),
                relation("EXISTS", condition("lineName", "eq", "适配器")))));
  }

  /** 不存在不考虑逻辑删除明细。 */
  @Test
  void notExists() throws Exception {
    assertEquals(List.of(3L), execute(relation("NOT_EXISTS", condition("lineName", "like", "台灯"))));
  }

  /** 强制删除条件不能被 OR 绕过。 */
  @Test
  void orCannotExposeDeleted() throws Exception {
    assertEquals(
        List.of(1L, 2L),
        execute(group("OR", condition("name", "eq", "台灯"), condition("amount", "eq", "5"))));
  }

  /** LIKE 输入中的百分号和下划线按字面匹配。 */
  @Test
  void literalWildcards() throws Exception {
    assertEquals(List.of(2L), execute(condition("name", "like", "_100%")));
  }

  /** 注入内容仅出现在参数值。 */
  @Test
  void injectionIsBound() throws Exception {
    String value = "x' OR 1=1 --";
    var request = new SearchRequest();
    request.setFilter(condition("name", "eq", value));
    var result = compiler.compile(adapter, snapshot, request);
    assertFalse(result.getSql().contains(value));
    assertEquals(List.of(), execute(request.getFilter()));
  }

  /** 零和 false 不被当作空条件。 */
  @Test
  void zeroAndFalse() throws Exception {
    assertEquals(
        List.of(1L),
        execute(group("AND", condition("amount", "eq", "0"), condition("flag", "eq", false))));
  }

  /** SQL 不等于遵守 NULL 三值逻辑，空值必须显式查询。 */
  @Test
  void explicitNull() throws Exception {
    assertEquals(List.of(3L), execute(condition("name", "isNull", null)));
    assertEquals(List.of(2L), execute(condition("name", "ne", "台灯")));
  }

  /** 区间类型和日期严格解析。 */
  @Test
  void ranges() throws Exception {
    var n = condition("day", "between", "2026-10-18");
    n.setValue2("2026-10-19");
    assertEquals(List.of(1L, 2L), execute(n));
    n.setValue2("2026-02-30");
    assertThrows(ServiceException.class, () -> execute(n));
  }

  /** 非法字段、运算符和来源组都拒绝执行。 */
  @Test
  void denyUnknownFieldsAndScope() {
    assertThrows(ServiceException.class, () -> execute(condition("name OR 1=1", "eq", "x")));
    assertThrows(ServiceException.class, () -> execute(condition("name", "raw", "x")));
    assertThrows(ServiceException.class, () -> execute(condition("lineName", "eq", "x")));
  }

  /** 小数不接受浮点数、超精度、科学计数法和缺失值。 */
  @Test
  void decimalValidation() {
    for (Object value : List.of(0.1, "1.001", "1e3", Map.of("sql", "x")))
      assertThrows(ServiceException.class, () -> execute(condition("amount", "eq", value)));
    assertThrows(ServiceException.class, () -> execute(condition("amount", "eq", null)));
  }

  /** 禁止空 OR 和超大集合意外扩大条件。 */
  @Test
  void malformedGroups() {
    assertThrows(ServiceException.class, () -> execute(group("OR")));
    assertThrows(
        ServiceException.class,
        () -> execute(condition("name", "in", Collections.nCopies(101, "x"))));
  }

  /** 复杂度边界作用于整棵树。 */
  @Test
  void complexityLimits() {
    assertThrows(
        ServiceException.class,
        () ->
            execute(
                group(
                    "AND",
                    Collections.nCopies(31, condition("name", "eq", "x"))
                        .toArray(QueryNode[]::new))));
    assertThrows(
        ServiceException.class,
        () ->
            execute(
                group(
                    "AND",
                    group("AND", group("AND", group("AND", condition("name", "eq", "x")))))));
    assertThrows(
        ServiceException.class,
        () -> execute(relation("EXISTS", relation("EXISTS", condition("lineName", "eq", "x")))));
  }

  /** 拒绝明细排序与 SQL 排序片段。 */
  @Test
  void sortingWhitelist() {
    var r = new SearchRequest();
    r.setSortField("quantity");
    assertThrows(ServiceException.class, () -> compiler.compile(adapter, snapshot, r));
    r.setSortField("name");
    r.setSortDirection("asc; DROP TABLE headers");
    assertThrows(ServiceException.class, () -> compiler.compile(adapter, snapshot, r));
  }

  /** 分页有界，技术字段和非法标识不可开放。 */
  @Test
  void boundaries() {
    var r = new SearchRequest();
    r.setPageSize(101);
    assertThrows(ServiceException.class, () -> compiler.compile(adapter, snapshot, r));
    assertThrows(ServiceException.class, () -> QueryCompiler.identifier("del_flag"));
    assertThrows(ServiceException.class, () -> QueryCompiler.identifier("name) OR 1=1"));
  }
}
