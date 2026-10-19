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

  /**
   * 服务端分页筛选及排序。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:list')")
  public R<PageResult<ArrivalView>> list(@Valid ArrivalQuery query) {
    return R.ok(service.list(query));
  }

  /**
   * 查看到货单保存的业务快照。
   *
   * @param id 采购到货单 ID
   * @return 最新的采购到货单详情及当前版本
   */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:query')")
  public R<ArrivalView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /**
   * 提供当前收货经办人及部门。
   *
   * @return 当前登录用户、部门及创建单据所需的默认设置
   */
  @GetMapping("/defaults")
  @PreAuthorize(
      "@ss.hasAnyPermi('purchase:arrival:add,purchase:arrival:edit,purchase:arrival:query')")
  public R<ArrivalDefaults> defaults() {
    return R.ok(service.defaults());
  }

  /**
   * 新增整单草稿，返回正式单号和实际明细。
   *
   * @param request 待新增的采购到货单字段
   * @return 实际落库的整单、稳定明细 ID 及最新版本
   */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('purchase:arrival:add')")
  @Log(title = "采购到货单", businessType = BusinessType.INSERT)
  public R<ArrivalView> create(@Valid @RequestBody SaveArrivalRequest request) {
    return R.ok(service.create(request));
  }

  /**
   * 按旧版本修改整单草稿。
   *
   * @param request 待修改的采购到货单字段，包含主键和当前版本
   * @return 实际落库的整单、稳定明细 ID 及最新版本
   */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('purchase:arrival:edit')")
  @Log(title = "采购到货单", businessType = BusinessType.UPDATE)
  public R<ArrivalView> update(@Valid @RequestBody SaveArrivalRequest request) {
    return R.ok(service.update(request));
  }

  /**
   * 删除当前版本草稿。
   *
   * @param id 采购到货单 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   * @return 操作成功响应，业务数据为空
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:arrival:remove')")
  @Log(title = "采购到货单", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /**
   * 按整单版本执行到货单状态命令及来源订单数量反写。
   *
   * <p>每个命令检查独立权限；驳回、作废必须填写原因，单据状态、来源执行数量和版本在同一事务中更新。
   *
   * @param id 采购到货单 ID
   * @param action 状态命令：submit、withdraw、approve、reject、void
   * @param request 当前整单版本及操作原因；驳回、作废时原因必填
   * @return 状态变更后的完整单据快照和最新版本
   * @throws com.ruoyi.common.exception.ServiceException 版本过期、状态或来源余量不满足约束，或存在阻止作废的下游引用时
   */
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
