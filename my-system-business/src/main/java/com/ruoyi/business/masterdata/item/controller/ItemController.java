package com.ruoyi.business.masterdata.item.controller;

import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 料品档案接口，统一参数校验、方法权限与 R 响应。 */
@Validated
@RestController
@RequestMapping("/masterdata/item")
public class ItemController {
  private final ItemService service;
  private final ItemCategoryService categories;

  /** 注入料品业务服务。 */
  public ItemController(ItemService service, ItemCategoryService categories) {
    this.service = service;
    this.categories = categories;
  }

  /** 查询料品分页结果，排序和筛选作用于全部符合条件的数据。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:item:list')")
  public R<PageResult<ItemView>> list(@Valid ItemQuery query) {
    return R.ok(service.list(query));
  }

  /** 查询料品详情。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:item:query')")
  public R<ItemView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 新增料品并返回生成的详情。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:item:add')")
  @Log(title = "料品档案", businessType = BusinessType.INSERT)
  public R<ItemView> create(@Valid @RequestBody SaveItemRequest request) {
    return R.ok(service.create(request));
  }

  /** 按当前版本修改料品及启停状态。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:item:edit')")
  @Log(title = "料品档案", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveItemRequest request) {
    service.update(request);
    return R.ok();
  }

  /** 按当前版本逻辑删除料品。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:item:remove')")
  @Log(title = "料品档案", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /** 料品查询和维护用户可读取分类选项，无需分类维护权限。 */
  @GetMapping("/category-options")
  @PreAuthorize(
      "@ss.hasAnyPermi('masterdata:item:list,masterdata:item:query,masterdata:item:add,masterdata:item:edit')")
  public R<List<CategoryTreeNode>> categoryOptions() {
    return R.ok(categories.tree(null));
  }

  /** 返回完整单位选项，包括停用旧值。 */
  @GetMapping("/unit-options")
  @PreAuthorize(
      "@ss.hasAnyPermi('masterdata:item:list,masterdata:item:query,masterdata:item:add,masterdata:item:edit')")
  public R<List<UnitOption>> unitOptions() {
    return R.ok(service.unitOptions());
  }
}
