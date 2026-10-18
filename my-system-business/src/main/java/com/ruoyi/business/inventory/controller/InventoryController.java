package com.ruoyi.business.inventory.controller;

import com.ruoyi.business.inventory.dto.InventoryQuery;
import com.ruoyi.business.inventory.service.InventoryQueryService;
import com.ruoyi.business.inventory.vo.*;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 数量库存只读接口，不开放余额或流水写入。 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
  private final InventoryQueryService service;

  /** 注入查询服务。 */
  public InventoryController(InventoryQueryService service) {
    this.service = service;
  }

  /** 按维度分页查询当前余额。 */
  @GetMapping("/balance/list")
  @PreAuthorize("@ss.hasPermi('inventory:balance:list')")
  public R<PageResult<BalanceView>> balances(@Valid InventoryQuery query) {
    return R.ok(service.balances(query));
  }

  /** 查询包含冲销记录的实际库存流水。 */
  @GetMapping("/transaction/list")
  @PreAuthorize("@ss.hasPermi('inventory:transaction:list')")
  public R<PageResult<TransactionView>> transactions(@Valid InventoryQuery query) {
    return R.ok(service.transactions(query));
  }
}
