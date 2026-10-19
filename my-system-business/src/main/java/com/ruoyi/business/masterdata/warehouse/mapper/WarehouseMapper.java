package com.ruoyi.business.masterdata.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import org.apache.ibatis.annotations.*;

/** 仓库数据访问。 */
public interface WarehouseMapper extends BaseMapper<Warehouse> {
  /**
   * 检查包含已删除记录的编码占用，由唯一索引兜底并发。
   *
   * @param code 待检查的档案编码，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_warehouse WHERE warehouse_code=#{code} AND (#{excludedId} IS NULL OR warehouse_id!=#{excludedId})")
  long countCode(@Param("code") String code, @Param("excludedId") Long excludedId);
}
