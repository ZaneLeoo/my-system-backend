package com.ruoyi.business.masterdata.warehouse.controller;

import com.ruoyi.business.masterdata.warehouse.dto.SaveWarehouseRequest;
import com.ruoyi.business.masterdata.warehouse.dto.WarehouseQuery;
import com.ruoyi.business.masterdata.warehouse.service.WarehouseService;
import com.ruoyi.business.masterdata.warehouse.vo.WarehouseView;
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

/** 仓库接口，统一参数校验、方法权限与 R 响应。 */
@Validated
@RestController
@RequestMapping("/masterdata/warehouse")
public class WarehouseController {
  private final WarehouseService service;

  /** 注入仓库业务服务。 */
  public WarehouseController(WarehouseService service) {
    this.service = service;
  }

  /** 查询仓库分页结果，排序和筛选作用于全部符合条件的数据。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:list')")
  public R<PageResult<WarehouseView>> list(@Valid WarehouseQuery query) {
    return R.ok(service.list(query));
  }

  /** 查询仓库详情。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:query')")
  public R<WarehouseView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 新增仓库并返回生成的详情。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:add')")
  @Log(title = "仓库", businessType = BusinessType.INSERT)
  public R<WarehouseView> create(@Valid @RequestBody SaveWarehouseRequest request) {
    return R.ok(service.create(request));
  }

  /** 按当前版本修改仓库及启停状态。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:edit')")
  @Log(title = "仓库", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveWarehouseRequest request) {
    service.update(request);
    return R.ok();
  }

  /** 按当前版本逻辑删除仓库。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:remove')")
  @Log(title = "仓库", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }
}
