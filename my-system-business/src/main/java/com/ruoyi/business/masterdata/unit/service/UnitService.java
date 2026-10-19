package com.ruoyi.business.masterdata.unit.service;

import com.ruoyi.business.masterdata.unit.dto.SaveUnitRequest;
import com.ruoyi.business.masterdata.unit.dto.UnitQuery;
import com.ruoyi.business.masterdata.unit.vo.UnitView;
import com.ruoyi.common.core.page.PageResult;

/** 单位档案业务入口，统一查询、版本控制及删除约束。 */
public interface UnitService {
  /**
   * 按受控条件查询单位分页结果。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  PageResult<UnitView> list(UnitQuery query);

  /**
   * 查询未删除单位详情。
   *
   * @param id 计量单位 ID
   * @return 最新的计量单位详情及当前版本
   */
  UnitView detail(Long id);

  /**
   * 新增单位并返回生成的详情。
   *
   * @param request 待新增的计量单位字段
   * @return 新增后的计量单位详情，包含生成的 ID 和审计信息
   */
  UnitView create(SaveUnitRequest request);

  /**
   * 按当前版本修改单位资料和状态。
   *
   * @param request 待修改的计量单位字段，包含主键和当前版本
   */
  void update(SaveUnitRequest request);

  /**
   * 按当前版本逻辑删除单位，填充删除审计。
   *
   * @param id 计量单位 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   */
  void delete(Long id, Integer version);
}
