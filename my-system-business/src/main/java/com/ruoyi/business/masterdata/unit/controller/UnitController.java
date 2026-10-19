package com.ruoyi.business.masterdata.unit.controller;

import com.ruoyi.business.masterdata.unit.dto.SaveUnitRequest;
import com.ruoyi.business.masterdata.unit.dto.UnitQuery;
import com.ruoyi.business.masterdata.unit.service.UnitService;
import com.ruoyi.business.masterdata.unit.vo.UnitView;
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

/** 计量单位接口，统一参数校验、方法权限与 R 响应。 */
@Validated
@RestController
@RequestMapping("/masterdata/unit")
public class UnitController {
  private final UnitService service;

  /** 注入单位业务服务。 */
  public UnitController(UnitService service) {
    this.service = service;
  }

  /**
   * 查询单位分页结果，排序和筛选作用于全部符合条件的数据。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('masterdata:unit:list')")
  public R<PageResult<UnitView>> list(@Valid UnitQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 查询单位详情。
   *
   * @param id 计量单位 ID
   * @return 最新的计量单位详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:unit:query')")
  public R<UnitView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 新增单位并返回生成的详情。
   *
   * @param request 待新增的计量单位字段
   * @return 新增后的计量单位详情，包含生成的 ID 和审计信息
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('masterdata:unit:add')")
  @Log(title = "计量单位", businessType = BusinessType.INSERT)
  public R<UnitView> create(@Valid @RequestBody SaveUnitRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 按当前版本修改单位及启停状态。
   *
   * @param request 待修改的计量单位字段，包含主键和当前版本
   * @return 操作成功响应，业务数据为空
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('masterdata:unit:edit')")
  @Log(title = "计量单位", businessType = BusinessType.UPDATE)
  public R<Void> update(@Valid @RequestBody SaveUnitRequest request) {
    service.update(request);
    return R.ok();
  }

  /**
   * 按当前版本逻辑删除单位。
   *
   * @param id 计量单位 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('masterdata:unit:remove')")
  @Log(title = "计量单位", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }
}
