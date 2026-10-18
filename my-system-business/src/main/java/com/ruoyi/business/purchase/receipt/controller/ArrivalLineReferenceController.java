package com.ruoyi.business.purchase.receipt.controller;

import com.ruoyi.business.purchase.receipt.domain.ReceiptRules;
import com.ruoyi.business.purchase.receipt.dto.ArrivalLineReferenceQuery;
import com.ruoyi.business.purchase.receipt.service.ReceiptService;
import com.ruoyi.business.purchase.receipt.vo.ArrivalLineReference;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 来源行独立授权，不依赖到货单列表或详情权限。 */
@Validated
@RestController
@RequestMapping("/purchase/arrival/reference-lines")
@PreAuthorize("@ss.hasPermi('purchase:arrival:reference')")
public class ArrivalLineReferenceController {
  private final ReceiptService service;

  /** 注入到货来源查询服务。 */
  public ArrivalLineReferenceController(ReceiptService service) {
    this.service = service;
  }

  /** 查询同一供应商下可新增到货的已审核到货单行。 */
  @GetMapping
  public R<PageResult<ArrivalLineReference>> list(@Valid ArrivalLineReferenceQuery query) {
    return R.ok(service.references(query));
  }

  /** 确认前重新读取来源行额度，不执行占用。 */
  @GetMapping("/resolve")
  public R<List<ArrivalLineReference>> resolve(
      @RequestParam @NotEmpty @Size(max = ReceiptRules.MAX_REFERENCE_IDS)
          List<@NotNull @Positive Long> ids) {
    return R.ok(service.resolve(ids));
  }
}
