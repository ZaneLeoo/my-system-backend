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

  /**
   * 分页搜索启用仓库。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/warehouses")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:reference')")
  public R<PageResult<WarehouseReference>> warehouses(@Valid WarehouseQuery query) {
    return R.ok(service.warehouses(query));
  }

  /**
   * 回显仓库旧值。
   *
   * @param ids 批量回显的不同记录 ID，每次最多 100 个
   * @return 匹配的未删除记录，包含允许回显的停用旧值
   */
  @GetMapping("/warehouses/resolve")
  @PreAuthorize("@ss.hasPermi('masterdata:warehouse:reference')")
  public R<List<WarehouseReference>> warehouses(
      @RequestParam @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids) {
    return R.ok(service.warehouses(ids));
  }

  /**
   * 分页搜索指定仓库内可新选货位。
   *
   * @param query 货位筛选、分页和排序条件，必须指定所属仓库
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/locations")
  @PreAuthorize("@ss.hasPermi('masterdata:location:reference')")
  public R<PageResult<LocationReference>> locations(@Valid LocationQuery query) {
    return R.ok(service.locations(query));
  }

  /**
   * 回显货位时限定所属仓库。
   *
   * @param warehouseId 所属仓库 ID
   * @param ids 批量回显的不同记录 ID，每次最多 100 个
   * @return 匹配的未删除记录，包含允许回显的停用旧值
   */
  @GetMapping("/locations/resolve")
  @PreAuthorize("@ss.hasPermi('masterdata:location:reference')")
  public R<List<LocationReference>> locations(
      @RequestParam @Positive Long warehouseId,
      @RequestParam @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids) {
    return R.ok(service.locations(warehouseId, ids));
  }
}
