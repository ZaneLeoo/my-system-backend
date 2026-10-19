package com.ruoyi.business.purchase.arrival.controller;

import com.ruoyi.business.purchase.arrival.domain.ArrivalRules;
import com.ruoyi.business.purchase.arrival.dto.OrderLineReferenceQuery;
import com.ruoyi.business.purchase.arrival.service.ArrivalService;
import com.ruoyi.business.purchase.arrival.vo.OrderLineReference;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 来源行独立授权，不依赖订单列表或详情权限。 */
@Validated
@RestController
@RequestMapping("/purchase/order/reference-lines")
@PreAuthorize("@ss.hasPermi('purchase:order:reference')")
public class OrderLineReferenceController {
  private final ArrivalService service;

  /** 注入到货来源查询服务。 */
  public OrderLineReferenceController(ArrivalService service) {
    this.service = service;
  }

  /**
   * 查询同一供应商下可新增到货的已审核订单行。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping
  public R<PageResult<OrderLineReference>> list(@Valid OrderLineReferenceQuery query) {
    return R.ok(service.references(query));
  }

  /**
   * 确认前重新读取来源行额度，不执行占用。
   *
   * @param ids 批量回显的不同记录 ID，每次最多 100 个
   * @return 匹配的未删除记录，包含允许回显的停用旧值
   */
  @GetMapping("/resolve")
  public R<List<OrderLineReference>> resolve(
      @RequestParam @NotEmpty @Size(max = ArrivalRules.MAX_REFERENCE_IDS)
          List<@NotNull @Positive Long> ids) {
    return R.ok(service.resolve(ids));
  }
}
