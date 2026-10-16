package com.ruoyi.business.masterdata.item.service.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.business.masterdata.category.mapper.ItemCategoryMapper;
import com.ruoyi.business.masterdata.item.domain.Item;
import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.mapper.ItemMapper;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.common.constant.LogicDeleteConstants;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** 档案与参照共用的只读查询、分页及分类路径组装。 */
@Component
public class ItemReadSupport {
  private final ItemMapper mapper;
  private final ItemCategoryMapper categories;

  /** 注入相同的关联 Mapper，避免参照另建一套查询 SQL。 */
  public ItemReadSupport(ItemMapper mapper, ItemCategoryMapper categories) {
    this.mapper = mapper;
    this.categories = categories;
  }

  /** 先确定分类后代，再分页执行单次关联查询，转换前保留总数。 */
  public PageResult<ItemView> list(ItemQuery query) {
    if (query.getPageNum() == null
        || query.getPageNum() < 1
        || query.getPageSize() == null
        || query.getPageSize() < 1
        || query.getPageSize() > 100) throw new ServiceException("分页参数无效");
    Map<Long, ItemCategory> tree = categoryIndex();
    QueryWrapper<Item> wrapper = visibleQuery();
    if (query.getKeyword() != null) {
      String keyword = escape(query.getKeyword());
      wrapper.and(
          w ->
              w.like("i.item_code", keyword)
                  .or()
                  .like("i.item_name", keyword)
                  .or()
                  .like("i.specification", keyword));
    }
    if (query.getItemCode() != null) wrapper.like("i.item_code", escape(query.getItemCode()));
    if (query.getItemName() != null) wrapper.like("i.item_name", escape(query.getItemName()));
    if (query.getSpecification() != null)
      wrapper.like("i.specification", escape(query.getSpecification()));
    if (query.getCategoryId() != null) {
      Set<Long> ids = new HashSet<>();
      ids.add(query.getCategoryId());
      boolean changed;
      do {
        changed = false;
        for (ItemCategory c : tree.values())
          if (ids.contains(c.getParentId())) changed |= ids.add(c.getCategoryId());
      } while (changed);
      wrapper.in("i.category_id", ids);
    }
    if (query.getBaseUnitId() != null) wrapper.eq("i.base_unit_id", query.getBaseUnitId());
    if (query.getPurchaseUnitId() != null)
      wrapper.eq("i.purchase_unit_id", query.getPurchaseUnitId());
    if (query.getSalesUnitId() != null) wrapper.eq("i.sales_unit_id", query.getSalesUnitId());
    if (query.getStatus() != null)
      wrapper.eq("i.status", EnableStatus.fromCode(query.getStatus()).getCode());
    if (query.getPurchaseEnabled() != null)
      wrapper.eq("i.purchase_enabled", query.getPurchaseEnabled());
    String column =
        switch (query.getSortField() == null ? "itemCode" : query.getSortField()) {
          case "itemCode" -> "i.item_code";
          case "itemName" -> "i.item_name";
          case "specification" -> "i.specification";
          case "updateTime" -> "i.update_time";
          default -> throw new ServiceException("不支持的排序字段");
        };
    boolean asc =
        switch (query.getSortDirection() == null ? "" : query.getSortDirection()) {
          case "asc" -> true;
          case "desc" -> false;
          default -> throw new ServiceException("排序方向无效");
        };
    wrapper.orderBy(true, asc, column).orderByAsc("i.item_id");
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<ItemView> rows = mapper.selectViews(wrapper);
      long total = new PageInfo<>(rows).getTotal();
      rows.forEach(row -> row.setCategoryPath(categoryPath(row.getCategoryId(), tree)));
      return new PageResult<>(rows, total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 查询未删除详情，并补齐分类路径。 */
  public ItemView detail(Long id) {
    List<ItemView> found = mapper.selectViews(visibleQuery().eq("i.item_id", id));
    if (found.isEmpty()) throw new ServiceException("料品不存在或已删除");
    ItemView view = found.get(0);
    view.setCategoryPath(categoryPath(view.getCategoryId(), categoryIndex()));
    return view;
  }

  /** 一次批量查询回显数据，保留停用记录及调用方的去重后 ID 顺序。 */
  public List<ItemView> resolve(List<Long> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > 100
        || ids.stream().anyMatch(id -> id == null || id <= 0))
      throw new ServiceException("每次须指定 1～100 个有效料品 ID");
    LinkedHashSet<Long> unique = new LinkedHashSet<>(ids);
    List<ItemView> rows = mapper.selectViews(visibleQuery().in("i.item_id", unique));
    Map<Long, ItemCategory> tree = categoryIndex();
    rows.forEach(row -> row.setCategoryPath(categoryPath(row.getCategoryId(), tree)));
    Map<Long, ItemView> index =
        rows.stream().collect(Collectors.toMap(ItemView::getItemId, Function.identity()));
    return unique.stream().map(index::get).filter(Objects::nonNull).toList();
  }

  /** 显式过滤手写关联查询的逻辑删除记录。 */
  private QueryWrapper<Item> visibleQuery() {
    return new QueryWrapper<Item>().eq("i.del_flag", LogicDeleteConstants.NOT_DELETED);
  }

  /** 一次加载分类索引，避免每条料品查询祖先。 */
  private Map<Long, ItemCategory> categoryIndex() {
    return categories.selectList(null).stream()
        .collect(Collectors.toMap(ItemCategory::getCategoryId, Function.identity()));
  }

  /** 根据父子关系计算完整名称路径，防御异常循环。 */
  private String categoryPath(Long id, Map<Long, ItemCategory> index) {
    LinkedList<String> names = new LinkedList<>();
    Set<Long> visited = new HashSet<>();
    while (id != null && index.containsKey(id) && visited.add(id)) {
      ItemCategory c = index.get(id);
      names.addFirst(c.getCategoryName());
      id = c.getParentId();
    }
    return String.join(" / ", names);
  }

  /** 转义包含查询的通配符。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
