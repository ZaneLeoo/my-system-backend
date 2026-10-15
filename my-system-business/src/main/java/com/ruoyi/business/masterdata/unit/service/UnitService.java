package com.ruoyi.business.masterdata.unit.service;

import com.ruoyi.business.masterdata.unit.dto.SaveUnitRequest;
import com.ruoyi.business.masterdata.unit.dto.UnitQuery;
import com.ruoyi.business.masterdata.unit.vo.UnitView;
import com.ruoyi.common.core.page.PageResult;

/** 单位档案业务入口，统一查询、版本控制及删除约束。 */
public interface UnitService {
  /** 按受控条件查询单位分页结果。 */
  PageResult<UnitView> list(UnitQuery query);

  /** 查询未删除单位详情。 */
  UnitView detail(Long id);

  /** 新增单位并返回生成的详情。 */
  UnitView create(SaveUnitRequest request);

  /** 按当前版本修改单位资料和状态。 */
  void update(SaveUnitRequest request);

  /** 按当前版本逻辑删除单位，填充删除审计。 */
  void delete(Long id, Integer version);
}
