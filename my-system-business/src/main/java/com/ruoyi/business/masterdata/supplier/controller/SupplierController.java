package com.ruoyi.business.masterdata.supplier.controller;

import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.dto.SupplierQuery;
import com.ruoyi.business.masterdata.supplier.service.SupplierService;
import com.ruoyi.business.masterdata.supplier.vo.SupplierView;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 供应商档案接口，统一参数校验、方法权限与 R 响应。 */
@Validated
@RestController
@RequestMapping("/masterdata/supplier")
public class SupplierController {
  private final SupplierService service;

  /** 注入供应商业务服务。 */
  public SupplierController(SupplierService service) {
    this.service = service;
  }

  /** 查询供应商分页结果，排序和筛选作用于全部符合条件的数据。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:list')")
  public R<PageResult<SupplierView>> list(@Valid SupplierQuery query) {
    return R.ok(service.list(query));
  }

  /** 查询供应商详情。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:query')")
  public R<SupplierView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 新增供应商并返回生成的详情。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:add')")
  @Log(title = "供应商档案", businessType = BusinessType.INSERT)
  public R<SupplierView> create(@Valid @RequestBody SaveSupplierRequest request) {
    return R.ok(service.create(request));
  }

  /** 按当前版本修改供应商及启停状态。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:edit')")
  @Log(title = "供应商档案", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveSupplierRequest request) {
    service.update(request);
    return R.ok();
  }

  /** 按当前版本逻辑删除供应商。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:remove')")
  @Log(title = "供应商档案", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }
}
