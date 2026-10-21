package com.ruoyi.business.masterdata.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import org.apache.ibatis.annotations.Select;

/** 分类数据访问；MP CRUD 自动过滤逻辑删除。 */
public interface ItemCategoryMapper extends BaseMapper<ItemCategory> {

  /**
   * 在当前事务内取得分类树写锁，覆盖空树和跨实例并发变更。
   *
   * @return 锁定的分类树互斥记录标识；锁保持至当前事务结束
   */
  @Select("SELECT lock_id FROM md_item_category_lock WHERE lock_id = 1 FOR UPDATE")
  Integer lockTree();

  /**
   * 检查包括已删除记录在内的编码占用，遵循数据库排序规则。
   *
   * @param code 待检查的档案编码，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_item_category WHERE category_code = #{code} AND (#{excludedId} IS NULL OR category_id != #{excludedId})")
  long countCodeIncludingDeleted(String code, Long excludedId);
}
