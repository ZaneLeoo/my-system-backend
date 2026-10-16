package com.ruoyi.business.masterdata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 基础资料引用检查；不依赖料品 Service，避免循环依赖。 */
public interface MasterDataReferenceMapper {
  /** 查询分类被未删除料品引用的数量，停用时只检查启用料品。 */
  @Select(
      "SELECT COUNT(*) FROM md_item WHERE category_id=#{id} AND del_flag='0' AND (#{enabledOnly}=false OR status='0')")
  long categoryItems(@Param("id") Long id, @Param("enabledOnly") boolean enabledOnly);

  /** 三种单位角色任一被引用都纳入约束。 */
  @Select(
      "SELECT COUNT(*) FROM md_item WHERE (base_unit_id=#{id} OR purchase_unit_id=#{id} OR sales_unit_id=#{id}) AND del_flag='0' AND (#{enabledOnly}=false OR status='0')")
  long unitItems(@Param("id") Long id, @Param("enabledOnly") boolean enabledOnly);
}
