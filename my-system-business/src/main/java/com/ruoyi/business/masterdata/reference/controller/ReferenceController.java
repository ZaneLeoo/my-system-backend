package com.ruoyi.business.masterdata.reference.controller;

import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.reference.dto.ItemReferenceQuery;
import com.ruoyi.business.masterdata.reference.service.ReferenceService;
import com.ruoyi.business.masterdata.reference.vo.ItemReference;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 基础档案只读参照入口，权限独立于列表及维护。 */
@RestController
@Validated
@RequestMapping("/masterdata/reference")
public class ReferenceController {
  private final ReferenceService service;

  /** 注入只读参照服务。 */
  public ReferenceController(ReferenceService service) {
    this.service = service;
  }

  /**
   * 分页搜索可供新选的启用料品。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/items")
  @PreAuthorize("@ss.hasPermi('masterdata:item:reference')")
  public R<PageResult<ItemReference>> items(@Valid ItemReferenceQuery query) {
    return R.ok(service.items(query));
  }

  /**
   * 批量回显与确认复核，缺失记录由调用方标记失效。
   *
   * @param ids 批量回显的不同记录 ID，每次最多 100 个
   * @return 匹配的未删除记录，包含允许回显的停用旧值
   */
  @GetMapping("/items/resolve")
  @PreAuthorize("@ss.hasPermi('masterdata:item:reference')")
  public R<List<ItemReference>> resolve(
      @RequestParam @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids) {
    return R.ok(service.resolve(ids));
  }

  /**
   * 料品参照用户可读取窗口内的分类筛选树。
   *
   * @return 分类树选项，保留展示所需的层级关系
   */
  @GetMapping("/items/category-options")
  @PreAuthorize("@ss.hasPermi('masterdata:item:reference')")
  public R<List<CategoryTreeNode>> itemCategories() {
    return R.ok(service.categories());
  }

  /**
   * 单位参照用户可读取完整单位选项。
   *
   * @return 完整单位选项，包含未删除的停用旧值
   */
  @GetMapping("/units")
  @PreAuthorize("@ss.hasPermi('masterdata:unit:reference')")
  public R<List<UnitOption>> units() {
    return R.ok(service.units());
  }

  /**
   * 分类参照用户可读取分类树。
   *
   * @return 分类树选项，保留展示所需的层级关系
   */
  @GetMapping("/categories")
  @PreAuthorize("@ss.hasPermi('masterdata:category:reference')")
  public R<List<CategoryTreeNode>> categories() {
    return R.ok(service.categories());
  }
}
