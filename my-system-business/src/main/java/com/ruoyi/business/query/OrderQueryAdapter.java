package com.ruoyi.business.query;

import com.github.pagehelper.*;
import com.ruoyi.business.purchase.order.service.OrderService;
import com.ruoyi.business.query.mapper.OrderQueryMapper;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.query.engine.CompiledQuery;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** 采购订单主明细查询，仅使用已有快照字段，不关联当前档案名称替换历史。 */
@Component
public class OrderQueryAdapter implements QueryAdapter {
  private final OrderQueryMapper mapper;
  private final OrderService orders;
  private final QueryOptionsSupport options;

  /** 注入主表分页、原有进度组装与查询选项。 */
  public OrderQueryAdapter(
      OrderQueryMapper mapper, OrderService orders, QueryOptionsSupport options) {
    this.mapper = mapper;
    this.orders = orders;
    this.options = options;
  }

  /** 稳定业务编码。 */
  public String key() {
    return "purchase.order";
  }

  /** 原列表权限。 */
  public String permission() {
    return "purchase:order:list";
  }

  /** 主明细关联由开发者登记，实施人员不可改写。 */
  public Map<String, QuerySource> sources() {
    return Map.of(
        "main",
        new QuerySource("purchase_order", "o", "订单主表", null),
        "lines",
        new QuerySource("purchase_order_line", "l", "订单明细", "l.order_id=o.order_id"));
  }

  /** 稳定主键。 */
  public String primaryKey() {
    return "order_id";
  }

  /** 默认日期排序。 */
  public String defaultSort() {
    return "orderDate";
  }

  /** 默认最新日期优先。 */
  public String defaultDirection() {
    return "desc";
  }

  /** 分页仅作用于 ID 查询，补齐原有 VO 与进度前清理上下文。 */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public PageResult<?> search(CompiledQuery query, SearchRequest request) {
    try {
      PageHelper.startPage(request.getPageNum(), request.getPageSize()).setReasonable(false);
      List<Long> ids = mapper.searchIds(query.wrapper());
      long total = new PageInfo<>(ids).getTotal();
      PageHelper.clearPage();
      return new PageResult<>(orders.queryViews(ids), total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 已注册选项。 */
  public Set<String> optionProviders() {
    return Set.of("boolean", "suppliers", "items", "units", "orderStatus");
  }

  /** 校验业务枚举。 */
  public void validateValue(FieldSpec f, Object value) {
    options.validate(f, value);
  }

  /** 使用查询权限读取选项。 */
  public Object options(String provider, String keyword, List<Long> ids, int page) {
    return options.options(provider, keyword, ids, page);
  }

  /** 根据业务字段提供固定的引用或枚举语义。 */
  public String fieldOptionProvider(String sourceKey, String columnName) {
    return options.provider(key(), sourceKey, columnName);
  }
}
