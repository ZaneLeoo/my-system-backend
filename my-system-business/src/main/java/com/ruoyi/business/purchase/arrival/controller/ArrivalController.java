package com.ruoyi.business.purchase.arrival.controller;

import com.ruoyi.business.purchase.arrival.domain.ArrivalAction;
import com.ruoyi.business.purchase.arrival.dto.*;
import com.ruoyi.business.purchase.arrival.service.ArrivalService;
import com.ruoyi.business.purchase.arrival.vo.*;
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

/** 采购到货单接口，操作权限与状态约束分别在接口及业务层执行。 */
@Validated
@RestController
@RequestMapping("/purchase/arrival")
public class ArrivalController {
  private final ArrivalService service;

  /** 注入整单业务服务。 */
  public ArrivalController(ArrivalService service) {
    this.service = service;
  }

  /** 服务端分页筛选及排序。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:list')")
  public R<PageResult<ArrivalView>> list(@Valid ArrivalQuery query) {
    return R.ok(service.list(query));
  }

  /** 查看到货单保存的业务快照。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:query')")
  public R<ArrivalView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 提供当前收货经办人及部门。 */
  @GetMapping("/defaults")
  @PreAuthorize(
      "@ss.hasAnyPermi('purchase:arrival:add,purchase:arrival:edit,purchase:arrival:query')")
  public R<ArrivalDefaults> defaults() {
    return R.ok(service.defaults());
  }

  /** 新增整单草稿，返回正式单号和实际明细。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('purchase:arrival:add')")
  @Log(title = "采购到货单", businessType = BusinessType.INSERT)
  public R<ArrivalView> create(@Valid @RequestBody SaveArrivalRequest request) {
    return R.ok(service.create(request));
  }

  /** 按旧版本修改整单草稿。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('purchase:arrival:edit')")
  @Log(title = "采购到货单", businessType = BusinessType.UPDATE)
  public R<ArrivalView> update(@Valid @RequestBody SaveArrivalRequest request) {
    return R.ok(service.update(request));
  }

  /** 删除当前版本草稿。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:remove')")
  @Log(title = "采购到货单", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /** 路由限定允许的状态命令，每个命令使用独立权限。 */
  @PostMapping("/{id}/{action:submit|withdraw|approve|reject|void}")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:' + #action)")
  @Log(title = "采购到货单状态操作", businessType = BusinessType.UPDATE)
  public R<ArrivalView> transition(
      @PathVariable @Positive Long id,
      @PathVariable String action,
      @Valid @RequestBody ArrivalActionRequest request) {
    return R.ok(service.transition(id, ArrivalAction.fromPath(action), request));
  }
}
