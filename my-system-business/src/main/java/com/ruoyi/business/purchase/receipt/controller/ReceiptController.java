package com.ruoyi.business.purchase.receipt.controller;

import com.ruoyi.business.purchase.receipt.domain.ReceiptAction;
import com.ruoyi.business.purchase.receipt.dto.*;
import com.ruoyi.business.purchase.receipt.service.ReceiptService;
import com.ruoyi.business.purchase.receipt.vo.*;
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

/** 采购入库单接口，操作权限与状态约束分别在接口及业务层执行。 */
@Validated
@RestController
@RequestMapping("/purchase/receipt")
public class ReceiptController {
  private final ReceiptService service;

  /** 注入整单业务服务。 */
  public ReceiptController(ReceiptService service) {
    this.service = service;
  }

  /** 服务端分页筛选及排序。 */
  @GetMapping("/list")
  @PreAuthorize("@ss.hasPermi('purchase:receipt:list')")
  public R<PageResult<ReceiptView>> list(@Valid ReceiptQuery query) {
    return R.ok(service.list(query));
  }

  /** 查看入库单保存的业务快照。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:receipt:query')")
  public R<ReceiptView> detail(@PathVariable @Positive Long id) {
    return R.ok(service.detail(id));
  }

  /** 提供当前收货经办人及部门。 */
  @GetMapping("/defaults")
  @PreAuthorize(
      "@ss.hasAnyPermi('purchase:receipt:add,purchase:receipt:edit,purchase:receipt:query')")
  public R<ReceiptDefaults> defaults() {
    return R.ok(service.defaults());
  }

  /** 新增整单草稿，返回正式单号和实际明细。 */
  @PostMapping
  @PreAuthorize("@ss.hasPermi('purchase:receipt:add')")
  @Log(title = "采购入库单", businessType = BusinessType.INSERT)
  public R<ReceiptView> create(@Valid @RequestBody SaveReceiptRequest request) {
    return R.ok(service.create(request));
  }

  /** 按旧版本修改整单草稿。 */
  @PutMapping
  @PreAuthorize("@ss.hasPermi('purchase:receipt:edit')")
  @Log(title = "采购入库单", businessType = BusinessType.UPDATE)
  public R<ReceiptView> update(@Valid @RequestBody SaveReceiptRequest request) {
    return R.ok(service.update(request));
  }

  /** 删除当前版本草稿。 */
  @DeleteMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('purchase:receipt:remove')")
  @Log(title = "采购入库单", businessType = BusinessType.DELETE)
  public R<Void> delete(
      @PathVariable @Positive Long id, @RequestParam @PositiveOrZero Integer version) {
    service.delete(id, version);
    return R.ok();
  }

  /** 路由限定允许的状态命令，每个命令使用独立权限。 */
  @PostMapping("/{id}/{action:submit|withdraw|approve|reject|void}")
  @PreAuthorize("@ss.hasPermi('purchase:receipt:' + #action)")
  @Log(title = "采购入库单状态操作", businessType = BusinessType.UPDATE)
  public R<ReceiptView> transition(
      @PathVariable @Positive Long id,
      @PathVariable String action,
      @Valid @RequestBody ReceiptActionRequest request) {
    return R.ok(service.transition(id, ReceiptAction.fromPath(action), request));
  }
}
