package com.ruoyi.business.purchase.order.controller;

import com.ruoyi.business.purchase.order.domain.OrderAction;
import com.ruoyi.business.purchase.order.dto.*;
import com.ruoyi.business.purchase.order.service.OrderService;
import com.ruoyi.business.purchase.order.vo.*;
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

/** 采购订单接口，操作权限与状态约束分别在接口及业务层执行。 */
@Validated
@RestController
@RequestMapping("/purchase/order")
public class OrderController {
  private final OrderService service;

  /** 注入整单业务服务。 */
  public OrderController(OrderService service) {
    this.service = service;
  }

  /** 服务端分页筛选及排序。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('purchase:order:list')")
  public R<PageResult<OrderView>> list(@Valid OrderQuery query) {
    return R.ok(service.list(query));
  }

  /** 查看订单保存的业务快照。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:order:query')")
  public R<OrderView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 提供当前采购归属及默认精度。 */
  @GetMapping("/defaults")
  @PreAuthorize("@ss.hasAnyPermi('purchase:order:add,purchase:order:edit,purchase:order:query')")
  public R<OrderDefaults> defaults() {
    return R.ok(service.defaults());
  }

  /** 只读试算，不保存订单或更改精度。 */
  @PostMapping("/calculate")
  @PreAuthorize("@ss.hasAnyPermi('purchase:order:add,purchase:order:edit')")
  public R<OrderCalculation> calculate(@Valid @RequestBody CalculationRequest request) {
    return R.ok(service.calculate(request));
  }

  /** 新增整单草稿，返回正式单号和实际明细。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('purchase:order:add')")
  @Log(title = "采购订单", businessType = BusinessType.INSERT)
  public R<OrderView> create(@Valid @RequestBody SaveOrderRequest request) {
    return R.ok(service.create(request));
  }

  /** 按旧版本修改整单草稿。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('purchase:order:edit')")
  @Log(title = "采购订单", businessType = BusinessType.UPDATE)
  public R<OrderView> update(@Valid @RequestBody SaveOrderRequest request) {
    return R.ok(service.update(request));
  }

  /** 删除当前版本草稿。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:order:remove')")
  @Log(title = "采购订单", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /** 路由限定允许的状态命令，每个命令使用独立权限。 */
  @PostMapping("/{id}/{action:submit|withdraw|approve|reject|close}")
  @PreAuthorize("@ss.hasPermi('purchase:order:' + #action)")
  @Log(title = "采购订单状态操作", businessType = BusinessType.UPDATE)
  public R<OrderView> transition(
      @PathVariable @Positive Long id,
      @PathVariable String action,
      @Valid @RequestBody OrderActionRequest request) {
    return R.ok(service.transition(id, OrderAction.fromPath(action), request));
  }
}
