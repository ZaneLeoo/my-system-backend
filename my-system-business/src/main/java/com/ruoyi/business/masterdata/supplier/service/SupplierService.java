package com.ruoyi.business.masterdata.supplier.service;

import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.dto.SupplierQuery;
import com.ruoyi.business.masterdata.supplier.vo.SupplierOption;
import com.ruoyi.business.masterdata.supplier.vo.SupplierView;
import com.ruoyi.common.core.page.PageResult;
import java.util.List;

/** 供应商档案业务入口，统一查询、版本控制及删除约束。 */
public interface SupplierService {
  /**
   * 按受控条件查询供应商分页结果。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  PageResult<SupplierView> list(SupplierQuery query);

  /**
   * 读取完整只读参照选项，保留停用旧值。
   *
   * @return 符合条件的供应商列表
   */
  List<SupplierOption> options();

  /**
   * 查询未删除供应商详情。
   *
   * @param id 供应商 ID
   * @return 最新的供应商详情及当前版本
   */
  SupplierView detail(Long id);

  /**
   * 新增供应商并返回生成的详情。
   *
   * @param request 待新增的供应商字段
   * @return 新增后的供应商详情，包含生成的 ID 和审计信息
   */
  SupplierView create(SaveSupplierRequest request);

  /**
   * 按当前版本修改供应商资料和状态。
   *
   * @param request 待修改的供应商字段，包含主键和当前版本
   */
  void update(SaveSupplierRequest request);

  /**
   * 按当前版本逻辑删除供应商，填充删除审计。
   *
   * @param id 供应商 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   */
  void delete(Long id, Integer version);
}
