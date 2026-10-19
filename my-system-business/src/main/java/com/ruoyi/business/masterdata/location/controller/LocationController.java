package com.ruoyi.business.masterdata.location.controller;

import com.ruoyi.business.masterdata.location.dto.LocationQuery;
import com.ruoyi.business.masterdata.location.dto.SaveLocationRequest;
import com.ruoyi.business.masterdata.location.service.LocationService;
import com.ruoyi.business.masterdata.location.vo.LocationView;
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

/** 货位接口，统一参数校验、方法权限与 R 响应。 */
@Validated
@RestController
@RequestMapping("/masterdata/location")
public class LocationController {
  private final LocationService service;

  /** 注入货位业务服务。 */
  public LocationController(LocationService service) {
    this.service = service;
  }

  /**
   * 查询货位分页结果，排序和筛选作用于全部符合条件的数据。
   *
   * @param query 货位筛选、分页和排序条件，必须指定所属仓库
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:location:list')")
  public R<PageResult<LocationView>> list(@Valid LocationQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 货位维护和参照用户读取所属仓库的必要上下文。
   *
   * @param warehouseId 所属仓库 ID
   * @return 所属仓库的 ID、编码、名称、启停状态及货位管理开关
   */
  @GetMapping("/warehouse-context")
  @PreAuthorize(
      "@ss.hasAnyPermi('masterdata:location:list,masterdata:location:query,masterdata:location:add,masterdata:location:edit,masterdata:location:reference')")
  public R<com.ruoyi.business.masterdata.reference.vo.WarehouseReference> context(
      @RequestParam @Positive Long warehouseId) {
    return R.ok(service.context(warehouseId));
  }

  /**
   * 查询货位详情。
   *
   * @param id 货位 ID
   * @return 最新的货位详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:location:query')")
  public R<LocationView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 新增货位并返回生成的详情。
   *
   * @param request 待新增的货位字段
   * @return 新增后的货位详情，包含生成的 ID 和审计信息
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:location:add')")
  @Log(title = "货位", businessType = BusinessType.INSERT)
  public R<LocationView> create(@Valid @RequestBody SaveLocationRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 按当前版本修改货位及启停状态。
   *
   * @param request 待修改的货位字段，包含主键和当前版本
   * @return 操作成功响应，业务数据为空
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:location:edit')")
  @Log(title = "货位", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveLocationRequest request) {
    service.update(request);
    return R.ok();
  }

  /**
   * 按当前版本逻辑删除货位。
   *
   * @param id 货位 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:location:remove')")
  @Log(title = "货位", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }
}
