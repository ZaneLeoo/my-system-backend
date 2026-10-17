package com.ruoyi.business.masterdata.supplier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 供应商数据访问；手写 SQL 显式表达删除过滤及编码占用范围。 */
public interface SupplierMapper extends BaseMapper<Supplier> {
  /** 锁住有效供应商行；后续版本检查和写入必须位于同一事务。 */
  @Select(
      "SELECT supplier_id FROM md_supplier WHERE supplier_id = #{id} AND del_flag = #{delFlag} FOR UPDATE")
  Long lockSupplier(@Param("id") Long id, @Param("delFlag") String delFlag);

  /** 检查包含已删除记录的编码占用，遵循数据库排序规则。 */
  @Select(
      "SELECT COUNT(*) FROM md_supplier WHERE supplier_code = #{code} AND (#{excludedId} IS NULL OR supplier_id != #{excludedId})")
  long countCodeIncludingDeleted(@Param("code") String code, @Param("excludedId") Long excludedId);
}
