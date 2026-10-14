package com.ruoyi.business.masterdata.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import org.apache.ibatis.annotations.Select;

/** 分类数据访问；MP CRUD 自动过滤逻辑删除。 */
public interface ItemCategoryMapper extends BaseMapper<ItemCategory> {

  /** 在当前事务内取得分类树写锁，覆盖空树和跨实例并发变更。 */
  @Select("SELECT lock_id FROM md_item_category_lock WHERE lock_id = 1 FOR UPDATE")
  Integer lockTree();

  /** 检查包括已删除记录在内的编码占用，遵循数据库排序规则。 */
  @Select(
      "SELECT COUNT(*) FROM md_item_category WHERE category_code = #{code} AND (#{excludedId} IS NULL OR category_id != #{excludedId})")
  long countCodeIncludingDeleted(String code, Long excludedId);
}
