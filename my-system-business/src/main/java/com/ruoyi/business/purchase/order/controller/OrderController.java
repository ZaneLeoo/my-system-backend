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

  /**
   * 服务端分页筛选及排序。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('purchase:order:list')")
  public R<PageResult<OrderView>> list(@Valid OrderQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 查看订单保存的业务快照。
   *
   * @param id 采购订单 ID
   * @return 最新的采购订单详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:order:query')")
  public R<OrderView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 提供当前采购归属及默认精度。
   *
   * @return 当前登录用户、部门及创建单据所需的默认设置
   */
  @GetMapping("/defaults")
  @PreAuthorize("@ss.hasAnyPermi('purchase:order:add,purchase:order:edit,purchase:order:query')")
  public R<OrderDefaults> defaults() {
    return R.ok(service.defaults());
  }

  /**
   * 按指定计价口径和适用精度试算各行金额及整单汇总。
   *
   * <p>试算不持久化单据或占用来源；数量、价格使用十进制字符串，正式保存仍由服务端重新计算金额。
   *
   * @param request 计价方式、精度及待试算明细；不会修改单据
   * @return 后端按计价规则计算的行金额和整单汇总
   * @throws com.ruoyi.common.exception.ServiceException 数量、单价、税率或精度规则不合法，或金额超出存储范围时
   */
  @PostMapping("/calculate")
  @PreAuthorize("@ss.hasAnyPermi('purchase:order:add,purchase:order:edit')")
  public R<OrderCalculation> calculate(@Valid @RequestBody CalculationRequest request) {
    return R.ok(service.calculate(request));
  }

  /**
   * 新增整单草稿，返回正式单号和实际明细。
   *
   * @param request 待新增的采购订单字段
   * @return 实际落库的整单、稳定明细 ID 及最新版本
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('purchase:order:add')")
  @Log(title = "采购订单", businessType = BusinessType.INSERT)
  public R<OrderView> create(@Valid @RequestBody SaveOrderRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 按旧版本修改整单草稿。
   *
   * @param request 待修改的采购订单字段，包含主键和当前版本
   * @return 实际落库的整单、稳定明细 ID 及最新版本
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('purchase:order:edit')")
  @Log(title = "采购订单", businessType = BusinessType.UPDATE)
  public R<OrderView> update(@Valid @RequestBody SaveOrderRequest request) {
    return R.ok(service.update(request));
  }

  /**
   * 删除当前版本草稿。
   *
   * @param id 采购订单 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:order:remove')")
  @Log(title = "采购订单", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /**
   * 按整单版本执行提交、撤回、审核、驳回或关闭命令。
   *
   * <p>每个命令检查独立权限及当前状态；驳回、关闭必须填写原因，成功后递增版本并返回最新整单。
   *
   * @param id 采购订单 ID
   * @param action 状态命令：submit、withdraw、approve、reject、close
   * @param request 当前整单版本及操作原因；驳回、关闭时原因必填
   * @return 状态变更后的完整单据快照和最新版本
   * @throws com.ruoyi.common.exception.ServiceException 版本过期、状态不允许、必要原因缺失或档案引用不满足约束时
   */
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
