package com.ruoyi.business.masterdata.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.business.masterdata.category.mapper.ItemCategoryMapper;
import com.ruoyi.business.masterdata.item.domain.Item;
import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.mapper.ItemMapper;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.business.masterdata.unit.mapper.UnitMapper;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.constant.LogicDeleteConstants;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 料品事务与关联有效性规则，暂不进行跨单位换算。 */
@Service
public class ItemServiceImpl implements ItemService {
  private final ItemMapper mapper;
  private final ItemCategoryMapper categories;
  private final UnitMapper units;

  /** 注入数据访问组件，不形成双向 Service 依赖。 */
  public ItemServiceImpl(ItemMapper mapper, ItemCategoryMapper categories, UnitMapper units) {
    this.mapper = mapper;
    this.categories = categories;
    this.units = units;
  }

  /** 先确定分类后代，再分页执行单次关联查询，转换前保留总数。 */
  @Override
  public PageResult<ItemView> list(ItemQuery query) {
    if (query.getPageNum() == null
        || query.getPageNum() < 1
        || query.getPageSize() == null
        || query.getPageSize() < 1
        || query.getPageSize() > 100) throw new ServiceException("分页参数无效");
    Map<Long, ItemCategory> tree = categoryIndex();
    QueryWrapper<Item> wrapper = visibleQuery();
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
  @Override
  public ItemView detail(Long id) {
    List<ItemView> found = mapper.selectViews(visibleQuery().eq("i.item_id", id));
    if (found.isEmpty()) throw new ServiceException("料品不存在或已删除");
    ItemView view = found.get(0);
    view.setCategoryPath(categoryPath(view.getCategoryId(), categoryIndex()));
    return view;
  }

  /** 取得分类锁后验证所有引用，新增后返回实际落库值。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ItemView create(SaveItemRequest request) {
    if (request.getItemId() != null || request.getVersion() != null)
      throw new ServiceException("新增料品不能指定 ID 或版本号");
    lockTree();
    validateReferences(request, null);
    checkCode(request.getItemCode(), null);
    Item item = new Item();
    copy(request, item);
    item.setVersion(0);
    try {
      mapper.insert(item);
    } catch (DuplicateKeyException exception) {
      throw occupied();
    }
    return detail(item.getItemId());
  }

  /** 锁定分类、单位和料品后按旧版本执行原子更新。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveItemRequest request) {
    lockTree();
    if (request.getItemId() == null || request.getVersion() == null)
      throw new ServiceException("修改料品必须指定 ID 和版本号");
    // 分类锁串行化料品写入，因此这里读取的旧引用不会被其他料品写操作修改。
    Item previous = mapper.selectById(request.getItemId());
    if (previous == null) throw new ServiceException("料品不存在或已删除");
    validateReferences(request, previous);
    Item item = lockItem(request.getItemId(), request.getVersion());
    checkCode(request.getItemCode(), item.getItemId());
    if (item.getVersion() == Integer.MAX_VALUE) throw new ServiceException("料品版本已达上限");
    copy(request, item);
    item.setVersion(item.getVersion() + 1);
    try {
      if (mapper.update(
              item,
              Wrappers.<Item>lambdaUpdate()
                  .eq(Item::getItemId, item.getItemId())
                  .eq(Item::getVersion, request.getVersion()))
          != 1) throw stale();
    } catch (DuplicateKeyException exception) {
      throw occupied();
    }
  }

  /** 持锁检查版本，以实体方式删除并触发公共审计。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    lockTree();
    Item item = lockItem(id, version);
    if (mapper.deleteById(item) != 1) throw stale();
  }

  /** 单位选项不分页，停用项保留供旧值展示与查询。 */
  @Override
  public List<UnitOption> unitOptions() {
    return units
        .selectList(Wrappers.<Unit>lambdaQuery().orderByAsc(Unit::getUnitCode, Unit::getUnitId))
        .stream()
        .map(
            unit -> {
              UnitOption option = new UnitOption();
              option.setUnitId(unit.getUnitId());
              option.setUnitCode(unit.getUnitCode());
              option.setUnitName(unit.getUnitName());
              option.setStatus(unit.getStatus());
              return option;
            })
        .toList();
  }

  /** 引用替换要求启用；停用料品可原样保留停用引用，重新启用时全量校验。 */
  private void validateReferences(SaveItemRequest request, Item previous) {
    ItemCategory category = categories.selectById(request.getCategoryId());
    if (category == null) throw new ServiceException("分类不存在或已删除");
    boolean enabled = request.getStatus() == EnableStatus.ENABLED;
    if (category.getStatus() != CategoryStatus.ENABLED
        && (previous == null
            || enabled
            || !Objects.equals(previous.getCategoryId(), request.getCategoryId())))
      throw new ServiceException("请选择启用的分类");
    Set<Long> newIds = new TreeSet<>();
    if (request.getBaseUnitId() != null) newIds.add(request.getBaseUnitId());
    if (request.getPurchaseUnitId() != null) newIds.add(request.getPurchaseUnitId());
    if (request.getSalesUnitId() != null) newIds.add(request.getSalesUnitId());
    Map<Long, Unit> locked = new HashMap<>();
    for (Long id : newIds) {
      Unit unit =
          units.selectOne(Wrappers.<Unit>lambdaQuery().eq(Unit::getUnitId, id).last("FOR UPDATE"));
      if (unit == null) throw new ServiceException("单位不存在或已删除");
      locked.put(id, unit);
    }
    Long[] current = {
      request.getBaseUnitId(), request.getPurchaseUnitId(), request.getSalesUnitId()
    };
    Long[] old =
        previous == null
            ? new Long[3]
            : new Long[] {
              previous.getBaseUnitId(), previous.getPurchaseUnitId(), previous.getSalesUnitId()
            };
    for (int i = 0; i < current.length; i++)
      if (current[i] != null
          && locked.get(current[i]).getStatus() != EnableStatus.ENABLED
          && (previous == null || enabled || !Objects.equals(current[i], old[i])))
        throw new ServiceException("请选择启用的单位");
  }

  /** 分类树锁也保护料品分类引用与分类停用、删除之间的校验。 */
  private void lockTree() {
    if (categories.lockTree() == null) throw new ServiceException("分类初始化不完整，缺少分类树锁记录");
  }

  /** 对料品当前读并拒绝旧版本。 */
  private Item lockItem(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0)
      throw new ServiceException("必须指定有效料品 ID 和版本号");
    Item item =
        mapper.selectOne(Wrappers.<Item>lambdaQuery().eq(Item::getItemId, id).last("FOR UPDATE"));
    if (item == null) throw new ServiceException("料品不存在或已删除");
    if (!version.equals(item.getVersion())) throw stale();
    return item;
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

  /** 唯一校验包含已删除编码。 */
  private void checkCode(String code, Long id) {
    if (mapper.countCode(code, id) > 0) throw occupied();
  }

  /** 返回不泄露数据库信息的编码占用提示。 */
  private ServiceException occupied() {
    return new ServiceException("料品编码已占用（含已删除料品）");
  }

  /** 返回统一版本冲突提示。 */
  private ServiceException stale() {
    return new ServiceException("料品已变更，请刷新后重试");
  }

  /** 转义包含查询的通配符。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 只复制允许维护的字段，绝不复制审计和内部标记。 */
  private void copy(SaveItemRequest source, Item target) {
    target.setItemCode(source.getItemCode());
    target.setItemName(source.getItemName());
    target.setSpecification(source.getSpecification());
    target.setCategoryId(source.getCategoryId());
    target.setBaseUnitId(source.getBaseUnitId());
    target.setPurchaseUnitId(source.getPurchaseUnitId());
    target.setSalesUnitId(source.getSalesUnitId());
    target.setPurchaseEnabled(source.getPurchaseEnabled());
    target.setDefaultTaxRate(source.getDefaultTaxRate());
    target.setStatus(source.getStatus());
    target.setRemark(source.getRemark());
  }
}
