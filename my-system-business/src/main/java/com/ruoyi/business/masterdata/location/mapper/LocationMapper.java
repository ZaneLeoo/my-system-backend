package com.ruoyi.business.masterdata.location.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.masterdata.location.domain.Location;
import org.apache.ibatis.annotations.*;

/** 货位数据访问。 */
public interface LocationMapper extends BaseMapper<Location> {
  /** 检查包含已删除记录的编码占用，由唯一索引兜底并发。 */
  @Select(
      "SELECT COUNT(*) FROM md_location WHERE location_code=#{code} AND (#{excludedId} IS NULL OR location_id!=#{excludedId}) AND warehouse_id=#{warehouseId}")
  long countCode(
      @Param("code") String code,
      @Param("excludedId") Long excludedId,
      @Param("warehouseId") Long warehouseId);
}
