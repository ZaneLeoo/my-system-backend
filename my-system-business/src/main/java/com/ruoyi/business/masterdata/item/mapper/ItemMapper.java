package com.ruoyi.business.masterdata.item.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ruoyi.business.masterdata.item.domain.Item;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 料品持久化与单次关联查询。 */
public interface ItemMapper extends BaseMapper<Item> {
  /**
   * 服务层构建白名单 Wrapper，所有筛选值通过绑定参数传递。
   *
   * @param wrapper 由服务端白名单或查询编译器构建的条件，所有用户值通过参数绑定传递
   * @return 符合条件的料品列表
   */
  @Select(
      "SELECT i.item_id AS itemId, i.item_code AS itemCode, i.item_name AS itemName, i.specification AS specification, i.category_id AS categoryId, i.base_unit_id AS baseUnitId, i.purchase_unit_id AS purchaseUnitId, i.sales_unit_id AS salesUnitId, i.purchase_enabled AS purchaseEnabled, i.default_tax_rate AS defaultTaxRate, i.status AS status, i.remark AS remark, i.version AS version, i.create_by AS createBy, i.create_time AS createTime, i.update_by AS updateBy, i.update_time AS updateTime, c.category_name AS categoryName, b.unit_name AS baseUnitName, p.unit_name AS purchaseUnitName, s.unit_name AS salesUnitName FROM md_item i LEFT JOIN md_item_category c ON c.category_id=i.category_id AND c.del_flag='0' LEFT JOIN md_unit b ON b.unit_id=i.base_unit_id AND b.del_flag='0' LEFT JOIN md_unit p ON p.unit_id=i.purchase_unit_id AND p.del_flag='0' LEFT JOIN md_unit s ON s.unit_id=i.sales_unit_id AND s.del_flag='0'  ${ew.customSqlSegment}")
  List<ItemView> selectViews(@Param(Constants.WRAPPER) Wrapper<Item> wrapper);

  /**
   * 公共查询使用同一返回投影，并为列表和 count 设置超时。
   *
   * @param wrapper 由服务端白名单或查询编译器构建的条件，所有用户值通过参数绑定传递
   * @return 符合条件的料品列表
   */
  @Select(
      "SELECT i.item_id AS itemId, i.item_code AS itemCode, i.item_name AS itemName, i.specification AS specification, i.category_id AS categoryId, i.base_unit_id AS baseUnitId, i.purchase_unit_id AS purchaseUnitId, i.sales_unit_id AS salesUnitId, i.purchase_enabled AS purchaseEnabled, i.default_tax_rate AS defaultTaxRate, i.status AS status, i.remark AS remark, i.version AS version, i.create_by AS createBy, i.create_time AS createTime, i.update_by AS updateBy, i.update_time AS updateTime, c.category_name AS categoryName, b.unit_name AS baseUnitName, p.unit_name AS purchaseUnitName, s.unit_name AS salesUnitName FROM md_item i LEFT JOIN md_item_category c ON c.category_id=i.category_id AND c.del_flag='0' LEFT JOIN md_unit b ON b.unit_id=i.base_unit_id AND b.del_flag='0' LEFT JOIN md_unit p ON p.unit_id=i.purchase_unit_id AND p.del_flag='0' LEFT JOIN md_unit s ON s.unit_id=i.sales_unit_id AND s.del_flag='0'  ${ew.customSqlSegment}")
  @org.apache.ibatis.annotations.Options(timeout = 3)
  List<ItemView> selectSearchViews(@Param(Constants.WRAPPER) Wrapper<Item> wrapper);

  /**
   * 包括已删除记录的编码占用校验。
   *
   * @param code 待检查的档案编码，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_item WHERE item_code=#{code} AND (#{excludedId} IS NULL OR item_id!=#{excludedId})")
  long countCode(@Param("code") String code, @Param("excludedId") Long excludedId);
}
