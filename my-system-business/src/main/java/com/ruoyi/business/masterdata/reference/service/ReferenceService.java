package com.ruoyi.business.masterdata.reference.service;

import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.service.support.ItemReadSupport;
import com.ruoyi.business.masterdata.reference.dto.ItemReferenceQuery;
import com.ruoyi.business.masterdata.reference.vo.ItemReference;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import java.util.List;
import org.springframework.stereotype.Service;

/** 独立权限下的只读参照服务，维护接口授权保持原状。 */
@Service
public class ReferenceService {
  private final ItemReadSupport read;
  private final ItemService items;
  private final ItemCategoryService categories;

  /** 复用查询与选项能力，不依赖维护 Controller。 */
  public ReferenceService(ItemReadSupport read, ItemService items, ItemCategoryService categories) {
    this.read = read;
    this.items = items;
    this.categories = categories;
  }

  /** 服务端固定新选料品为启用状态，关键词内部按 OR、其他条件按 AND。 */
  public PageResult<ItemReference> items(ItemReferenceQuery request) {
    ItemQuery query = new ItemQuery();
    query.setPageNum(request.getPageNum());
    query.setPageSize(request.getPageSize());
    query.setKeyword(request.getKeyword());
    query.setCategoryId(request.getCategoryId());
    query.setSortField(request.getSortField());
    query.setSortDirection(request.getSortDirection());
    query.setStatus(EnableStatus.ENABLED.getCode());
    if (Boolean.TRUE.equals(request.getPurchaseOnly())) query.setPurchaseEnabled(true);
    PageResult<com.ruoyi.business.masterdata.item.vo.ItemView> result = read.list(query);
    return new PageResult<>(
        result.getRows().stream().map(ItemReference::from).toList(), result.getTotal());
  }

  /** 批量回显未删除记录，允许停用旧值且不产生逐条查询。 */
  public List<ItemReference> resolve(List<Long> ids) {
    return read.resolve(ids).stream().map(ItemReference::from).toList();
  }

  /** 返回完整单位选项，保留停用项供旧值回显。 */
  public List<UnitOption> units() {
    return items.unitOptions();
  }

  /** 返回所有未删除分类及其原有可选状态。 */
  public List<CategoryTreeNode> categories() {
    return categories.tree(null);
  }
}
