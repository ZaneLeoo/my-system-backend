package com.ruoyi.business.masterdata.location.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.location.domain.Location;
import org.apache.ibatis.annotations.*;

/** 货位数据访问。 */
public interface LocationMapper extends BaseMapper<Location> {
  /**
   * 检查包含已删除记录的编码占用，由唯一索引兜底并发。
   *
   * @param code 待检查的档案编码，按数据库排序规则比较
   * @param excludedId 本次修改的档案 ID，查重时排除自身；新增时可为空
   * @param warehouseId 所属仓库 ID
   * @return 符合引用或唯一性检查条件的记录数
   */
  @Select(
      "SELECT COUNT(*) FROM md_location WHERE location_code=#{code} AND (#{excludedId} IS NULL OR location_id!=#{excludedId}) AND warehouse_id=#{warehouseId}")
  long countCode(
      @Param("code") String code,
      @Param("excludedId") Long excludedId,
      @Param("warehouseId") Long warehouseId);
}
