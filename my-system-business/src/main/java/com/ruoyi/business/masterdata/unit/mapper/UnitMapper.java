package com.ruoyi.business.masterdata.unit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.unit.domain.Unit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 单位数据访问；手写 SQL 显式表达删除过滤及编码占用范围。 */
public interface UnitMapper extends BaseMapper<Unit> {
  /**
   * 锁住有效单位行；后续版本检查和写入必须位于同一事务。
   *
   * @param id 计量单位 ID
   * @param delFlag 逻辑删除标记，由公共逻辑删除约定提供
   * @return 已锁定的单位 ID，无匹配记录时返回 null
   */
  @Select("SELECT unit_id FROM md_unit WHERE unit_id = #{id} AND del_flag = #{delFlag} FOR UPDATE")
  Long lockUnit(@Param("id") Long id, @Param("delFlag") String delFlag);

  /**
   * 检查包含已删除记录的编码占用，遵循数据库排序规则。
   *
   * @param code 待检查的档案编码，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_unit WHERE unit_code = #{code} AND (#{excludedId} IS NULL OR unit_id != #{excludedId})")
  long countCodeIncludingDeleted(@Param("code") String code, @Param("excludedId") Long excludedId);

  /**
   * 检查包含已删除记录的名称占用，遵循数据库排序规则。
   *
   * @param name 待检查的单位名称，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_unit WHERE unit_name = #{name} AND (#{excludedId} IS NULL OR unit_id != #{excludedId})")
  long countNameIncludingDeleted(@Param("name") String name, @Param("excludedId") Long excludedId);
}
