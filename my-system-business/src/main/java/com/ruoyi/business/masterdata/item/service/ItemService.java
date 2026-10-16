package com.ruoyi.business.masterdata.item.service;

import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.core.page.PageResult;
import java.util.List;

/** 料品档案业务入口。 */
public interface ItemService {
  /** 查询分页列表并补齐分类路径。 */
  PageResult<ItemView> list(ItemQuery query);

  /** 获取最新详情。 */
  ItemView detail(Long id);

  /** 新增料品。 */
  ItemView create(SaveItemRequest request);

  /** 按当前版本修改料品。 */
  void update(SaveItemRequest request);

  /** 按当前版本逻辑删除。 */
  void delete(Long id, Integer version);

  /** 返回完整单位选项，包含停用旧值。 */
  List<UnitOption> unitOptions();
}
