package com.ruoyi.business.masterdata.item.service;

import com.ruoyi.business.masterdata.item.dto.ItemQuery;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.masterdata.unit.vo.UnitOption;
import com.ruoyi.common.core.page.PageResult;
import java.util.List;

/** 料品档案业务入口。 */
public interface ItemService {
  /**
   * 查询分页列表并补齐分类路径。
   *
   * @param query 业务筛选、分页及白名单排序条件
   * @return 当前页记录及全部匹配记录的总数
   */
  PageResult<ItemView> list(ItemQuery query);

  /**
   * 获取最新详情。
   *
   * @param id 料品 ID
   * @return 最新的料品详情及当前版本
   */
  ItemView detail(Long id);

  /**
   * 新增料品。
   *
   * @param request 待新增的料品字段
   * @return 新增后的料品详情，包含生成的 ID 和审计信息
   */
  ItemView create(SaveItemRequest request);

  /**
   * 按当前版本修改料品。
   *
   * @param request 待修改的料品字段，包含主键和当前版本
   */
  void update(SaveItemRequest request);

  /**
   * 按当前版本逻辑删除。
   *
   * @param id 料品 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   */
  void delete(Long id, Integer version);

  /**
   * 返回完整单位选项，包含停用旧值。
   *
   * @return 完整单位选项，包含未删除的停用旧值
   */
  List<UnitOption> unitOptions();
}
