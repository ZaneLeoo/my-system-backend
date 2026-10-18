package com.ruoyi.business.masterdata.location.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.business.masterdata.location.dto.*;
import com.ruoyi.business.masterdata.location.mapper.LocationMapper;
import com.ruoyi.business.masterdata.location.vo.LocationView;
import com.ruoyi.business.masterdata.reference.vo.WarehouseReference;
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.business.masterdata.warehouse.service.WarehouseService;
import com.ruoyi.business.masterdata.warehouse.vo.WarehouseView;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 平级货位维护，所有写入先锁所属仓库再锁货位。 */
@Service
public class LocationService {
  private final LocationMapper mapper;
  private final WarehouseService warehouses;
  private final Validator validator;

  /** 注入货位访问、仓库事务协调和参数校验。 */
  public LocationService(LocationMapper mapper, WarehouseService warehouses, Validator validator) {
    this.mapper = mapper;
    this.warehouses = warehouses;
    this.validator = validator;
  }

  /** 固定所属仓库分页查询，不因货位停用或父仓库停用隐藏维护数据。 */
  public PageResult<LocationView> list(LocationQuery query) {
    validate(query);
    warehouses.require(query.getWarehouseId());
    LambdaQueryWrapper<Location> condition =
        Wrappers.<Location>lambdaQuery().eq(Location::getWarehouseId, query.getWarehouseId());
    if (query.getLocationCode() != null)
      condition.like(Location::getLocationCode, escape(query.getLocationCode()));
    if (query.getLocationName() != null)
      condition.like(Location::getLocationName, escape(query.getLocationName()));
    if (query.getKeyword() != null)
      condition.and(
          w ->
              w.like(Location::getLocationCode, escape(query.getKeyword()))
                  .or()
                  .like(Location::getLocationName, escape(query.getKeyword())));
    if (query.getStatus() != null)
      condition.eq(Location::getStatus, EnableStatus.fromCode(query.getStatus()));
    SFunction<Location, ?> sort =
        switch (query.getSortField() == null ? "locationCode" : query.getSortField()) {
          case "locationCode" -> Location::getLocationCode;
          case "locationName" -> Location::getLocationName;
          case "updateTime" -> Location::getUpdateTime;
          default -> throw new ServiceException("不支持的排序字段");
        };
    condition
        .orderBy(true, "asc".equals(query.getSortDirection()), sort)
        .orderByAsc(Location::getLocationId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<Location> found = mapper.selectList(condition);
      long total = new PageInfo<>(found).getTotal();
      return new PageResult<>(found.stream().map(LocationView::from).toList(), total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 返回货位详情，不要求仓库维护权限。 */
  public LocationView detail(Long id) {
    return LocationView.from(require(id));
  }

  /** 返回货位页面和选择器必要的仓库上下文，不含审计或版本。 */
  public WarehouseReference context(Long warehouseId) {
    return WarehouseReference.from(WarehouseView.from(warehouses.require(warehouseId)));
  }

  /** 新增货位要求所属仓库启用且开启货位管理。 */
  @Transactional(rollbackFor = Exception.class)
  public LocationView create(SaveLocationRequest request) {
    validate(request);
    if (request.getLocationId() != null || request.getVersion() != null)
      throw new ServiceException("新增货位不能指定 ID 或版本号");
    Warehouse warehouse = warehouses.lock(request.getWarehouseId());
    requireSelectable(warehouse);
    checkCode(request, null);
    Location location = new Location();
    copy(request, location);
    location.setVersion(0);
    try {
      mapper.insert(location);
    } catch (DuplicateKeyException exception) {
      throw duplicate();
    }
    return LocationView.from(location);
  }

  /** 不允许更换所属仓库，仅重新启用时检查父仓库可用性。 */
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveLocationRequest request) {
    validate(request);
    Location initial = require(request.getLocationId());
    Warehouse warehouse = warehouses.lock(initial.getWarehouseId());
    Location location = lock(initial.getLocationId(), request.getVersion());
    if (!location.getWarehouseId().equals(request.getWarehouseId()))
      throw new ServiceException("货位创建后不能更换所属仓库");
    if (location.getStatus() != EnableStatus.ENABLED && request.getStatus() == EnableStatus.ENABLED)
      requireSelectable(warehouse);
    checkCode(request, location.getLocationId());
    copy(request, location);
    if (location.getVersion() == Integer.MAX_VALUE) throw new ServiceException("货位版本已达上限");
    location.setVersion(location.getVersion() + 1);
    try {
      if (mapper.update(
              location,
              Wrappers.<Location>lambdaUpdate()
                  .eq(Location::getLocationId, location.getLocationId())
                  .eq(Location::getVersion, request.getVersion()))
          != 1) throw stale();
    } catch (DuplicateKeyException exception) {
      throw duplicate();
    }
  }

  /** 固定锁顺序与版本检查后使用实体逻辑删除，触发公共审计。 */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    Location initial = require(id);
    warehouses.lock(initial.getWarehouseId());
    Location location = lock(id, version);
    if (mapper.deleteById(location) != 1) throw stale();
  }

  /** 读取当前有效货位，首次读取仅用于定位不可变的所属仓库。 */
  private Location require(Long id) {
    if (id == null || id <= 0) throw new ServiceException("货位 ID 必须为正数");
    Location result = mapper.selectById(id);
    if (result == null) throw new ServiceException("货位不存在或已删除");
    return result;
  }

  /** 已持仓库锁后取得货位当前读并检查页面版本。 */
  private Location lock(Long id, Integer version) {
    Location result =
        mapper.selectOne(
            Wrappers.<Location>lambdaQuery().eq(Location::getLocationId, id).last("FOR UPDATE"));
    if (result == null) throw new ServiceException("货位不存在或已删除");
    if (version == null || !version.equals(result.getVersion())) throw stale();
    return result;
  }

  /** 新选、创建及重新启用都需要父仓库处于可用状态。 */
  public void requireSelectable(Warehouse warehouse) {
    if (warehouse.getStatus() != EnableStatus.ENABLED) throw new ServiceException("所属仓库已停用");
    if (!Boolean.TRUE.equals(warehouse.getLocationEnabled()))
      throw new ServiceException("所属仓库未启用货位管理");
  }

  /** 编码只在同一仓库内唯一，并保留已删除货位的占用。 */
  private void checkCode(SaveLocationRequest request, Long excluded) {
    if (mapper.countCode(request.getLocationCode(), excluded, request.getWarehouseId()) > 0)
      throw duplicate();
  }

  /** 仅复制维护字段，所属仓库在更新前已验证不变。 */
  private void copy(SaveLocationRequest source, Location target) {
    target.setWarehouseId(source.getWarehouseId());
    target.setLocationCode(source.getLocationCode());
    target.setLocationName(source.getLocationName());
    target.setStatus(source.getStatus());
    target.setRemark(source.getRemark());
  }

  /** 参数约束对内部服务调用同样生效。 */
  private void validate(Object value) {
    if (value == null) throw new ServiceException("参数不能为空");
    var errors = validator.validate(value);
    if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
  }

  /** 唯一索引冲突转换为业务提示。 */
  private ServiceException duplicate() {
    return new ServiceException("当前仓库的货位编码已占用（含已删除货位）");
  }

  /** 统一旧版本冲突提示。 */
  private ServiceException stale() {
    return new ServiceException("货位已变更，请刷新后重试");
  }

  /** 将包含条件中的通配符作为普通文本。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
