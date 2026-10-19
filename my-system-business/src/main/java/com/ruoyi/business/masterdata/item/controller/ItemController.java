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

  /**
   * 查询料品分页结果，排序和筛选作用于全部符合条件的数据。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:item:list')")
  public R<PageResult<ItemView>> list(@Valid ItemQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 查询料品详情。
   *
   * @param id 料品 ID
   * @return 最新的料品详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:item:query')")
  public R<ItemView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 新增料品并返回生成的详情。
   *
   * @param request 待新增的料品字段
   * @return 新增后的料品详情，包含生成的 ID 和审计信息
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:item:add')")
  @Log(title = "料品档案", businessType = BusinessType.INSERT)
  public R<ItemView> create(@Valid @RequestBody SaveItemRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 按当前版本修改料品及启停状态。
   *
   * @param request 待修改的料品字段，包含主键和当前版本
   * @return 操作成功响应，业务数据为空
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:item:edit')")
  @Log(title = "料品档案", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveItemRequest request) {
    service.update(request);
    return R.ok();
  }

  /**
   * 按当前版本逻辑删除料品。
   *
   * @param id 料品 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:item:remove')")
  @Log(title = "料品档案", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /**
   * 料品查询和维护用户可读取分类选项，无需分类维护权限。
   *
   * @return 分类树选项，保留展示所需的层级关系
   */
  @GetMapping("/category-options")
  @PreAuthorize(
      "@ss.hasAnyPermi('masterdata:item:list,masterdata:item:query,masterdata:item:add,masterdata:item:edit')")
  public R<List<CategoryTreeNode>> categoryOptions() {
    return R.ok(categories.tree(null));
  }

  /**
   * 返回完整单位选项，包括停用旧值。
   *
   * @return 完整单位选项，包含未删除的停用旧值
   */
  @GetMapping("/unit-options")
  @PreAuthorize(
      "@ss.hasAnyPermi('masterdata:item:list,masterdata:item:query,masterdata:item:add,masterdata:item:edit')")
  public R<List<UnitOption>> unitOptions() {
    return R.ok(service.unitOptions());
  }
}
