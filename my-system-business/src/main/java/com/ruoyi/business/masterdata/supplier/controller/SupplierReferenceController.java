package com.ruoyi.business.masterdata.supplier.controller;

import com.ruoyi.business.masterdata.supplier.service.SupplierService;
import com.ruoyi.business.masterdata.supplier.vo.SupplierOption;
import com.ruoyi.common.core.domain.R;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 供应商只读参照入口，独立于档案列表和维护权限。 */
@RestController
public class SupplierReferenceController {
  private final SupplierService service;

  /** 注入供应商查询服务。 */
  public SupplierReferenceController(SupplierService service) {
    this.service = service;
  }

  /**
   * 返回完整选项，停用旧值允许回显，新选由组件和后续单据服务校验。
   *
   * @return 完整供应商参照选项，包含用于历史回显的停用记录
   */
  @GetMapping("/masterdata/reference/suppliers")
  @PreAuthorize("@ss.hasPermi('masterdata:supplier:reference')")
  public R<List<SupplierOption>> options() {
    return R.ok(service.options());
  }
}
