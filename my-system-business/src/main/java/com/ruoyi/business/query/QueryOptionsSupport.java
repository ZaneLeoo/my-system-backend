package com.ruoyi.business.query;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.protocol.FieldSpec;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 查询条件专用选项，包含停用档案，权限由查询对象控制。 */
@Component
public class QueryOptionsSupport {
  private final JdbcTemplate jdbc;

  /** 注入只读选项查询。 */
  public QueryOptionsSupport(JdbcTemplate jdbc) {
    this.jdbc = new JdbcTemplate(java.util.Objects.requireNonNull(jdbc.getDataSource()));
    this.jdbc.setQueryTimeout(3);
  }

  /** 枚举值在服务端校验，不依赖客户端下拉框。 */
  public void validate(FieldSpec field, Object value) {
    String provider = field.getOptionProvider();
    if ("enableStatus".equals(provider) && !Set.of("0", "1").contains(value.toString()))
      throw new ServiceException("状态值无效", 400);
    if ("orderStatus".equals(provider)
        && !Set.of("DRAFT", "SUBMITTED", "APPROVED", "CLOSED").contains(value.toString()))
      throw new ServiceException("订单状态无效", 400);
    if (Set.of("categories", "units", "items", "suppliers")
            .contains(provider == null ? "" : provider)
        && (!(value instanceof Long) || ((Long) value) <= 0))
      throw new ServiceException("档案 ID 必须是正整数", 400);
  }

  /** 固定的提供器选择固定 SQL，禁止输入参与表名和字段名。 */
  public Object options(String provider, String keyword, List<Long> ids, int page) {
    if ("boolean".equals(provider)) return page(List.of(option(true, "是"), option(false, "否")));
    if ("enableStatus".equals(provider)) return page(List.of(option("0", "启用"), option("1", "停用")));
    if ("orderStatus".equals(provider))
      return page(
          List.of(
              option("DRAFT", "草稿"),
              option("SUBMITTED", "已提交"),
              option("APPROVED", "已审核"),
              option("CLOSED", "已关闭")));
    if ("purchaseEnabled".equals(provider))
      return page(List.of(option(true, "允许采购"), option(false, "不允许采购")));
    String table, id, code, name;
    switch (provider) {
      case "categories" -> {
        table = "md_item_category";
        id = "category_id";
        code = "category_code";
        name = "category_name";
      }
      case "units" -> {
        table = "md_unit";
        id = "unit_id";
        code = "unit_code";
        name = "unit_name";
      }
      case "suppliers" -> {
        table = "md_supplier";
        id = "supplier_id";
        code = "supplier_code";
        name = "supplier_name";
      }
      case "items" -> {
        table = "md_item";
        id = "item_id";
        code = "item_code";
        name = "item_name";
      }
      default -> throw new ServiceException("选项来源未登记", 400);
    }
    String where = " WHERE del_flag='0'";
    List<Object> args = new ArrayList<>();
    if (ids != null) {
      where +=
          " AND " + id + " IN (" + String.join(",", Collections.nCopies(ids.size(), "?")) + ")";
      args.addAll(ids);
    } else if (keyword != null && !keyword.isBlank()) {
      where += " AND (" + code + " LIKE ? ESCAPE '!' OR " + name + " LIKE ? ESCAPE '!')";
      String v =
          "%" + keyword.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
      args.add(v);
      args.add(v);
    }
    Long total =
        jdbc.queryForObject("SELECT COUNT(*) FROM " + table + where, Long.class, args.toArray());
    String sql =
        "SELECT "
            + id
            + " AS value,CONCAT("
            + code
            + ", ' / ', "
            + name
            + ",CASE WHEN status='1' THEN '（停用）' ELSE '' END) AS label"
            + (provider.equals("categories") ? ",parent_id AS parentId" : "")
            + " FROM "
            + table
            + where
            + " ORDER BY "
            + code
            + ","
            + id;
    if (ids == null && !Set.of("units", "categories").contains(provider)) {
      sql += " LIMIT 20 OFFSET ?";
      args.add((page - 1) * 20);
    }
    var rows = jdbc.queryForList(sql, args.toArray());
    return Map.of("rows", rows, "total", total == null ? 0 : total);
  }

  /** 分类后代用一次读取计算，保留所有未删除层级。 */
  public List<Long> descendants(Object selected) {
    Long id = (Long) selected;
    var rows =
        jdbc.queryForList("SELECT category_id,parent_id FROM md_item_category WHERE del_flag='0'");
    Set<Long> found = new LinkedHashSet<>();
    if (rows.stream().noneMatch(r -> ((Number) r.get("category_id")).longValue() == id))
      return List.of();
    found.add(id);
    boolean changed;
    do {
      changed = false;
      for (var row : rows)
        if (found.contains(((Number) row.get("parent_id")).longValue()))
          changed |= found.add(((Number) row.get("category_id")).longValue());
    } while (changed);
    return List.copyOf(found);
  }

  /** 构造枚举项。 */
  private Map<String, Object> option(Object value, String label) {
    return Map.of("value", value, "label", label);
  }

  /** 选项响应统一包含总数。 */
  private Map<String, Object> page(List<Map<String, Object>> rows) {
    return Map.of("rows", rows, "total", rows.size());
  }

  /** 引用和枚举语义绑定物理字段，不能通过更换 UI 控件绕过。 */
  public String provider(String queryKey, String sourceKey, String column) {
    if ("masterdata.item".equals(queryKey))
      return switch (column) {
        case "category_id" -> "categories";
        case "base_unit_id", "purchase_unit_id", "sales_unit_id" -> "units";
        case "status" -> "enableStatus";
        case "purchase_enabled" -> "purchaseEnabled";
        default -> "";
      };
    if ("main".equals(sourceKey))
      return switch (column) {
        case "supplier_id" -> "suppliers";
        case "status" -> "orderStatus";
        default -> "";
      };
    return switch (column) {
      case "item_id" -> "items";
      case "unit_id" -> "units";
      default -> "";
    };
  }
}
