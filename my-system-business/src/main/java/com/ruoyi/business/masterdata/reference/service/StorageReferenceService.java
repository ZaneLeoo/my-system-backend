package com.ruoyi.business.masterdata.reference.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.business.masterdata.location.dto.LocationQuery;
import com.ruoyi.business.masterdata.location.mapper.LocationMapper;
import com.ruoyi.business.masterdata.location.service.LocationService;
import com.ruoyi.business.masterdata.location.vo.LocationView;
import com.ruoyi.business.masterdata.reference.vo.*;
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.business.masterdata.warehouse.dto.WarehouseQuery;
import com.ruoyi.business.masterdata.warehouse.mapper.WarehouseMapper;
import com.ruoyi.business.masterdata.warehouse.service.WarehouseService;
import com.ruoyi.business.masterdata.warehouse.vo.WarehouseView;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 独立授权的仓库、货位只读参照，保留停用旧值回显。 */
@Service
@Transactional(readOnly = true)
public class StorageReferenceService {
  private final WarehouseService warehouses;
  private final LocationService locations;
  private final WarehouseMapper warehouseMapper;
  private final LocationMapper locationMapper;

  /** 注入复用的查询能力及批量回显访问。 */
  public StorageReferenceService(
      WarehouseService warehouses,
      LocationService locations,
      WarehouseMapper warehouseMapper,
      LocationMapper locationMapper) {
    this.warehouses = warehouses;
    this.locations = locations;
    this.warehouseMapper = warehouseMapper;
    this.locationMapper = locationMapper;
  }

  /** 服务端固定只允许新选启用仓库。 */
  public PageResult<WarehouseReference> warehouses(WarehouseQuery query) {
    query.setStatus(EnableStatus.ENABLED.getCode());
    PageResult<WarehouseView> result = warehouses.list(query);
    return new PageResult<>(
        result.getRows().stream().map(WarehouseReference::from).toList(), result.getTotal());
  }

  /** 父仓库不可用时返回空候选，不改变其货位自身状态。 */
  public PageResult<LocationReference> locations(LocationQuery query) {
    Warehouse warehouse = warehouses.require(query.getWarehouseId());
    query.setStatus(EnableStatus.ENABLED.getCode());
    if (warehouse.getStatus() != EnableStatus.ENABLED
        || !Boolean.TRUE.equals(warehouse.getLocationEnabled())) {
      return new PageResult<>(List.of(), 0);
    }
    PageResult<LocationView> result = locations.list(query);
    return new PageResult<>(
        result.getRows().stream().map(LocationReference::from).toList(), result.getTotal());
  }

  /** 批量回显未删除仓库，按请求顺序去重，不暴露审计或版本字段。 */
  public List<WarehouseReference> warehouses(List<Long> ids) {
    List<Long> selected = validIds(ids);
    Map<Long, Warehouse> index =
        warehouseMapper.selectByIds(selected).stream()
            .collect(Collectors.toMap(w -> w.getWarehouseId(), Function.identity()));
    return selected.stream()
        .filter(index::containsKey)
        .map(id -> WarehouseReference.from(WarehouseView.from(index.get(id))))
        .toList();
  }

  /** 只回显当前仓库内的未删除货位，父仓库停用不隐藏旧值。 */
  public List<LocationReference> locations(Long warehouseId, List<Long> ids) {
    if (warehouseId == null || warehouseId <= 0) {
      throw new ServiceException("必须指定有效仓库 ID");
    }
    List<Long> selected = validIds(ids);
    Map<Long, Location> index =
        locationMapper
            .selectList(
                Wrappers.<Location>lambdaQuery()
                    .eq(Location::getWarehouseId, warehouseId)
                    .in(Location::getLocationId, selected))
            .stream()
            .collect(Collectors.toMap(Location::getLocationId, Function.identity()));
    return selected.stream()
        .filter(index::containsKey)
        .map(id -> LocationReference.from(LocationView.from(index.get(id))))
        .toList();
  }

  /** 批量参数在服务层同样限制数量及正数 ID。 */
  private List<Long> validIds(List<Long> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > 100
        || ids.stream().anyMatch(id -> id == null || id <= 0)) {
      throw new ServiceException("每次须指定 1～100 个有效 ID");
    }
    return new ArrayList<>(new LinkedHashSet<>(ids));
  }
}
