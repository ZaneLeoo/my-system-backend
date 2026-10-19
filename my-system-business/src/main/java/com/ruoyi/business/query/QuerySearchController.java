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

  /**
   * 按料品当前发布配置执行分页查询。
   *
   * <p>需要料品列表权限；普通条件、分类后代条件和排序均由查询框架校验，结果继续补齐分类路径及单位名称。
   *
   * @param request 分页、发布配置版本、排序及 AND/OR 条件树
   * @return 当前页记录及全部匹配记录的总数
   * @throws com.ruoyi.common.exception.ServiceException 参数或字段不合法、配置失效或发布版本过期时
   */
  @PostMapping("/masterdata/item/search")
  @PreAuthorize("@ss.hasPermi('masterdata:item:list')")
  public R<PageResult<ItemView>> items(@RequestBody SearchRequest request) {
    return R.ok(service.search("masterdata.item", request, ItemView.class));
  }

  /**
   * 按采购订单当前发布配置执行主明细条件查询。
   *
   * <p>需要订单列表权限；明细条件通过 EXISTS/NOT EXISTS 筛选整单，分页单位为订单，结果保留原有到货和入库执行进度。
   *
   * @param request 分页、发布配置版本、排序及 AND/OR 条件树
   * @return 当前页记录及全部匹配记录的总数
   * @throws com.ruoyi.common.exception.ServiceException 条件不合法、配置失效或发布版本过期时
   */
  @PostMapping("/purchase/order/search")
  @PreAuthorize("@ss.hasPermi('purchase:order:list')")
  public R<PageResult<OrderView>> orders(@RequestBody SearchRequest request) {
    return R.ok(service.search("purchase.order", request, OrderView.class));
  }
}
