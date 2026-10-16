package com.ruoyi.business.masterdata.category.service.impl;

import static com.ruoyi.business.masterdata.category.domain.CategoryConstants.ROOT_ID;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.business.masterdata.category.domain.CategoryConstants;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.business.masterdata.category.dto.CategoryQuery;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.mapper.ItemCategoryMapper;
import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.category.vo.CategoryView;
import com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper;
import com.ruoyi.common.exception.ServiceException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 分类树业务规则；所有写操作先取得同一数据库行锁，再读取并验证最新状态。 */
@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {
  private final ItemCategoryMapper mapper;
  private final MasterDataReferenceMapper references;

  /** 注入分类数据访问接口。 */
  public ItemCategoryServiceImpl(ItemCategoryMapper mapper, MasterDataReferenceMapper references) {
    this.mapper = mapper;
    this.references = references;
  }

  /** 查询分类，过滤时补回祖先并标识真正匹配的节点。 */
  @Override
  public List<CategoryView> list(CategoryQuery query) {
    List<ItemCategory> all = allCategories();
    Map<Long, ItemCategory> indexed = index(all);
    Set<Long> matched =
        all.stream()
            .filter(c -> matches(c, query))
            .map(ItemCategory::getCategoryId)
            .collect(Collectors.toSet());
    Set<Long> included = new HashSet<>(matched);
    for (Long id : matched) {
      ItemCategory node = indexed.get(id);
      Set<Long> visited = new HashSet<>();
      while (node != null && visited.add(node.getCategoryId())) {
        included.add(node.getCategoryId());
        node = indexed.get(node.getParentId());
      }
    }
    return all.stream()
        .filter(c -> included.contains(c.getCategoryId()))
        .map(c -> CategoryView.from(c, matched.contains(c.getCategoryId())))
        .toList();
  }

  /** 构建选择树；剔除自身和后代，停用节点不可选。 */
  @Override
  public List<CategoryTreeNode> tree(Long excludedId) {
    List<ItemCategory> all = allCategories();
    Set<Long> excluded = new HashSet<>();
    if (excludedId != null) {
      requireCategory(index(all), excludedId);
      excluded.add(excludedId);
      boolean changed;
      do {
        changed = false;
        for (ItemCategory node : all) {
          if (excluded.contains(node.getParentId())) {
            changed |= excluded.add(node.getCategoryId());
          }
        }
      } while (changed);
    }
    Map<Long, List<ItemCategory>> children =
        all.stream()
            .filter(c -> !excluded.contains(c.getCategoryId()))
            .collect(Collectors.groupingBy(ItemCategory::getParentId));
    return buildTree(ROOT_ID, children);
  }

  /** 查询有效分类详情。 */
  @Override
  public CategoryView detail(Long id) {
    ItemCategory category = mapper.selectById(id);
    if (category == null) {
      throw new ServiceException("分类不存在或已删除");
    }
    return CategoryView.from(category, true);
  }

  /** 创建分类，唯一索引作为并发及数据库排序规则的最终约束。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public CategoryView create(SaveCategoryRequest request) {
    if (request.getCategoryId() != null || request.getVersion() != null) {
      throw new ServiceException("新增分类不能指定 ID 或版本号");
    }
    lockTree();
    validateParent(request, index(allCategories()));
    checkCode(request.getCategoryCode(), null);
    ItemCategory category = new ItemCategory();
    copyFields(request, category);
    category.setVersion(0);
    try {
      mapper.insert(category);
    } catch (DuplicateKeyException exception) {
      throw new ServiceException("分类编码已占用（含已删除分类）");
    }
    return CategoryView.from(category, true);
  }

  /** 持锁校验分类版本、父子关系和启停规则，再原子更新。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveCategoryRequest request) {
    lockTree();
    Map<Long, ItemCategory> indexed = index(allCategories());
    ItemCategory category = requireCategory(indexed, request.getCategoryId());
    checkVersion(category, request.getVersion());
    validateParent(request, indexed);
    if (request.getStatus() == CategoryStatus.DISABLED
        && indexed.values().stream()
            .anyMatch(
                c ->
                    c.getParentId().equals(category.getCategoryId())
                        && c.getStatus() == CategoryStatus.ENABLED)) {
      throw new ServiceException("存在启用的子分类，请先停用子分类");
    }
    if (request.getStatus() == CategoryStatus.DISABLED
        && references.categoryItems(category.getCategoryId(), true) > 0) {
      throw new ServiceException("存在启用料品引用，请先调整或停用料品");
    }
    checkCode(request.getCategoryCode(), category.getCategoryId());
    copyFields(request, category);
    category.setVersion(Math.addExact(category.getVersion(), 1));
    try {
      int rows =
          mapper.update(
              category,
              Wrappers.<ItemCategory>lambdaUpdate()
                  .eq(ItemCategory::getCategoryId, category.getCategoryId())
                  .eq(ItemCategory::getVersion, request.getVersion()));
      if (rows != 1) {
        throw new ServiceException("分类已变更，请刷新后重试");
      }
    } catch (DuplicateKeyException exception) {
      throw new ServiceException("分类编码已占用（含已删除分类）");
    }
  }

  /** 禁止删除有子分类的节点；实体删除保证 MP 填充本次删除审计。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    lockTree();
    Map<Long, ItemCategory> indexed = index(allCategories());
    ItemCategory category = requireCategory(indexed, id);
    checkVersion(category, version);
    if (indexed.values().stream().anyMatch(c -> c.getParentId().equals(id))) {
      throw new ServiceException("存在子分类，不能删除");
    }
    if (references.categoryItems(id, false) > 0) {
      throw new ServiceException("存在料品引用，不能删除分类");
    }
    if (mapper.deleteById(category) != 1) {
      throw new ServiceException("分类已变更，请刷新后重试");
    }
  }

  /** 读取有效分类并保证同级排序稳定。 */
  private List<ItemCategory> allCategories() {
    return mapper.selectList(
        Wrappers.<ItemCategory>lambdaQuery()
            .orderByAsc(ItemCategory::getSortOrder, ItemCategory::getCategoryId));
  }

  /** 取得专用锁行；缺失锁行时拒绝无保护的业务写入。 */
  private void lockTree() {
    if (mapper.lockTree() == null) {
      throw new ServiceException("分类初始化不完整，缺少分类树锁记录");
    }
  }

  /** 校验上级存在、无循环，启用分类的所有祖先均处于启用状态。 */
  private void validateParent(SaveCategoryRequest request, Map<Long, ItemCategory> indexed) {
    Long parentId = request.getParentId();
    Set<Long> visited = new HashSet<>();
    while (!Objects.equals(parentId, ROOT_ID)) {
      if (Objects.equals(parentId, request.getCategoryId()) || !visited.add(parentId)) {
        throw new ServiceException("上级不能为自身或后代分类");
      }
      ItemCategory parent = requireCategory(indexed, parentId);
      if (request.getStatus() == CategoryStatus.ENABLED
          && parent.getStatus() != CategoryStatus.ENABLED) {
        throw new ServiceException("启用分类的上级及祖先必须启用");
      }
      parentId = parent.getParentId();
    }
    validateDepth(request.getCategoryId(), visited.size() + 1, indexed);
  }

  /** 检查移动后整棵子树的深度，避免只检查当前节点而使后代超限。 */
  private void validateDepth(Long id, int depth, Map<Long, ItemCategory> indexed) {
    Map<Long, List<ItemCategory>> children =
        indexed.values().stream().collect(Collectors.groupingBy(ItemCategory::getParentId));
    List<Long> level = id == null ? List.of() : List.of(id);
    do {
      if (depth > CategoryConstants.MAX_DEPTH) {
        throw new ServiceException("分类最多支持 " + CategoryConstants.MAX_DEPTH + " 层");
      }
      level =
          level.stream()
              .flatMap(parent -> children.getOrDefault(parent, List.of()).stream())
              .map(ItemCategory::getCategoryId)
              .toList();
      depth++;
    } while (!level.isEmpty());
  }

  /** 校验包含已删除记录的编码唯一性。 */
  private void checkCode(String code, Long excludedId) {
    if (mapper.countCodeIncludingDeleted(code, excludedId) > 0) {
      throw new ServiceException("分类编码已占用（含已删除分类）");
    }
  }

  /** 检查编辑版本，避免旧页面覆盖其他用户已保存的修改。 */
  private void checkVersion(ItemCategory category, Integer version) {
    if (version == null || !version.equals(category.getVersion())) {
      throw new ServiceException("分类已变更，请刷新后重试");
    }
  }

  /** 从有效分类索引查找节点，统一处理失效 ID。 */
  private ItemCategory requireCategory(Map<Long, ItemCategory> indexed, Long id) {
    ItemCategory category = indexed.get(id);
    if (category == null) {
      throw new ServiceException("分类不存在或已删除");
    }
    return category;
  }

  /** 建立节点索引，供祖先校验和筛选使用。 */
  private Map<Long, ItemCategory> index(List<ItemCategory> categories) {
    return categories.stream()
        .collect(Collectors.toMap(ItemCategory::getCategoryId, Function.identity()));
  }

  /** 仅复制允许客户端维护的业务字段。 */
  private void copyFields(SaveCategoryRequest request, ItemCategory category) {
    category.setParentId(request.getParentId());
    category.setCategoryCode(request.getCategoryCode());
    category.setCategoryName(request.getCategoryName());
    category.setSortOrder(request.getSortOrder());
    category.setStatus(request.getStatus());
    category.setRemark(request.getRemark());
  }

  /** 按已过滤的父子索引组装选择树，保持同级顺序。 */
  private List<CategoryTreeNode> buildTree(Long parentId, Map<Long, List<ItemCategory>> children) {
    return children.getOrDefault(parentId, List.of()).stream()
        .map(
            c ->
                new CategoryTreeNode(
                    c.getCategoryId(),
                    c.getCategoryCode() + " · " + c.getCategoryName(),
                    c.getStatus() != CategoryStatus.ENABLED,
                    buildTree(c.getCategoryId(), children)))
        .toList();
  }

  /** 判断行是否满足全部筛选条件。 */
  private boolean matches(ItemCategory category, CategoryQuery query) {
    return contains(category.getCategoryCode(), query.getCategoryCode())
        && contains(category.getCategoryName(), query.getCategoryName())
        && (query.getStatus() == null || query.getStatus().equals(category.getStatus().getCode()));
  }

  /** 对编码和名称执行忽略大小写的字面包含匹配，不把通配符当作 SQL。 */
  private boolean contains(String value, String filter) {
    return filter == null
        || value.toLowerCase(Locale.ROOT).contains(filter.strip().toLowerCase(Locale.ROOT));
  }
}
