package com.ruoyi.business.masterdata.reference.controller;

import com.ruoyi.business.masterdata.location.dto.LocationQuery;
import com.ruoyi.business.masterdata.reference.service.StorageReferenceService;
import com.ruoyi.business.masterdata.reference.vo.*;
import com.ruoyi.business.masterdata.warehouse.dto.WarehouseQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 仓库及货位参照使用独立权限，不依赖维护列表权限。 */
@Validated
@RestController
@RequestMapping("/masterdata/reference")
public class StorageReferenceController {
  private final StorageReferenceService service;

  /** 注入只读仓储参照服务。 */
  public StorageReferenceController(StorageReferenceService service) {
    this.service = service;
  }

  /** 分页搜索启用仓库。 */
  @GetMapping("/warehouses")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:reference')")
  public R<PageResult<WarehouseReference>> warehouses(@Valid WarehouseQuery query) {
    return R.ok(service.warehouses(query));
  }

  /** 回显仓库旧值。 */
  @GetMapping("/warehouses/resolve")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:reference')")
  public R<List<WarehouseReference>> warehouses(
      @RequestParam @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids) {
    return R.ok(service.warehouses(ids));
  }

  /** 分页搜索指定仓库内可新选货位。 */
  @GetMapping("/locations")
  @PreAuthorize("@ss.hasPermi('masterdata:location:reference')")
  public R<PageResult<LocationReference>> locations(@Valid LocationQuery query) {
    return R.ok(service.locations(query));
  }

  /** 回显货位时限定所属仓库。 */
  @GetMapping("/locations/resolve")
  @PreAuthorize("@ss.hasPermi('masterdata:location:reference')")
  public R<List<LocationReference>> locations(
      @RequestParam @Positive Long warehouseId,
      @RequestParam @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids) {
    return R.ok(service.locations(warehouseId, ids));
  }
}
