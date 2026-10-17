package com.ruoyi.business.masterdata.supplier.service;

import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.dto.SupplierQuery;
import com.ruoyi.business.masterdata.supplier.vo.SupplierOption;
import com.ruoyi.business.masterdata.supplier.vo.SupplierView;
import com.ruoyi.common.core.page.PageResult;
import java.util.List;

/** 供应商档案业务入口，统一查询、版本控制及删除约束。 */
public interface SupplierService {
  /** 按受控条件查询供应商分页结果。 */
  PageResult<SupplierView> list(SupplierQuery query);

  /** 读取完整只读参照选项，保留停用旧值。 */
  List<SupplierOption> options();

  /** 查询未删除供应商详情。 */
  SupplierView detail(Long id);

  /** 新增供应商并返回生成的详情。 */
  SupplierView create(SaveSupplierRequest request);

  /** 按当前版本修改供应商资料和状态。 */
  void update(SaveSupplierRequest request);

  /** 按当前版本逻辑删除供应商，填充删除审计。 */
  void delete(Long id, Integer version);
}
