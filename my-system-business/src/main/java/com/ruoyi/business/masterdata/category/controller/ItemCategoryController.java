package com.ruoyi.business.masterdata.category.controller;

import com.ruoyi.business.masterdata.category.dto.CategoryQuery;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.category.vo.CategoryView;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 存货分类 API；每个入口分别执行权限与请求校验。 */
@Validated
@RestController
@RequestMapping("/masterdata/item-category")
public class ItemCategoryController {
  private final ItemCategoryService service;

  /** 注入分类业务服务。 */
  public ItemCategoryController(ItemCategoryService service) {
    this.service = service;
  }

  /**
   * 查询平铺分类树表，筛选结果包含层级祖先。
   *
   * @param query 分类编码、名称、状态及白名单排序条件
   * @return 符合条件的料品分类列表
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:category:list')")
  public R<List<CategoryView>> list(@Valid CategoryQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 查询可选上级树，排除编辑节点及后代。
   *
   * @param excludedId 编辑中的分类 ID，排除该节点及其后代；新增时可为空
   * @return 排除当前节点及后代后的分类选择树，保留虚拟根节点
   */
  @GetMapping("/tree")
  @PreAuthorize("@ss.hasPermi('masterdata:category:query')")
  public R<List<CategoryTreeNode>> tree(@RequestParam(required = false) @Positive Long excludedId) {
    return R.ok(service.tree(excludedId));
  }

  /**
   * 查询未删除分类详情。
   *
   * @param id 料品分类 ID
   * @return 最新的料品分类详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:category:query')")
  public R<CategoryView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 新增分类并返回生成的分类详情。
   *
   * @param request 待新增的料品分类字段
   * @return 新增后的料品分类详情，包含生成的 ID 和审计信息
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:category:add')")
  @Log(title = "存货分类", businessType = BusinessType.INSERT)
  public R<CategoryView> create(@Valid @RequestBody SaveCategoryRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 修改分类业务信息，保留创建审计。
   *
   * @param request 待修改的料品分类字段，包含主键和当前版本
   * @return 操作成功响应，业务数据为空
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:category:edit')")
  @Log(title = "存货分类", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveCategoryRequest request) {
    service.update(request);
    return R.ok();
  }

  /**
   * 按当前版本逻辑删除分类，拒绝删除仍有子节点的分类。
   *
   * @param id 料品分类 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:category:remove')")
  @Log(title = "存货分类", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }
}
