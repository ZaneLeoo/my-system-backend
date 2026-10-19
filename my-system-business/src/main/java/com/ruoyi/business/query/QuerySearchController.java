package com.ruoyi.business.query;

import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.purchase.order.vo.OrderView;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.query.engine.QueryRuntimeService;
import com.ruoyi.query.protocol.SearchRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 保留原列表接口，业务列表分别接入新版查询协议。 */
@RestController
public class QuerySearchController {
  private final QueryRuntimeService service;

  /** 注入公共运行时入口。 */
  public QuerySearchController(QueryRuntimeService service) {
    this.service = service;
  }

  /** 料品元数据驱动的分页查询。 */
  @PostMapping("/masterdata/item/search")
  @PreAuthorize("@ss.hasPermi('masterdata:item:list')")
  public R<PageResult<ItemView>> items(@RequestBody SearchRequest request) {
    return R.ok(service.search("masterdata.item", request, ItemView.class));
  }

  /** 采购订单主明细条件分页查询。 */
  @PostMapping("/purchase/order/search")
  @PreAuthorize("@ss.hasPermi('purchase:order:list')")
  public R<PageResult<OrderView>> orders(@RequestBody SearchRequest request) {
    return R.ok(service.search("purchase.order", request, OrderView.class));
  }
}
