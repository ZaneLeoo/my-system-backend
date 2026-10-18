package com.ruoyi.business.masterdata.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.business.masterdata.location.mapper.LocationMapper;
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.business.masterdata.warehouse.dto.*;
import com.ruoyi.business.masterdata.warehouse.mapper.WarehouseMapper;
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

/** 仓库维护与所属货位的事务协调入口。 */
@Service
public class WarehouseService {
  private final WarehouseMapper mapper;
  private final LocationMapper locations;
  private final Validator validator;

  /** 注入仓库、货位访问及参数校验组件。 */
  public WarehouseService(WarehouseMapper mapper, LocationMapper locations, Validator validator) {
    this.mapper = mapper;
    this.locations = locations;
    this.validator = validator;
  }

  /** 全量条件的服务端分页，在转换响应前保留总数并清理分页上下文。 */
  public PageResult<WarehouseView> list(WarehouseQuery query) {
    validate(query);
    LambdaQueryWrapper<Warehouse> condition = Wrappers.lambdaQuery();
    if (query.getWarehouseCode() != null)
      condition.like(Warehouse::getWarehouseCode, escape(query.getWarehouseCode()));
    if (query.getWarehouseName() != null)
      condition.like(Warehouse::getWarehouseName, escape(query.getWarehouseName()));
    if (query.getKeyword() != null)
      condition.and(
          w ->
              w.like(Warehouse::getWarehouseCode, escape(query.getKeyword()))
                  .or()
                  .like(Warehouse::getWarehouseName, escape(query.getKeyword())));
    if (query.getStatus() != null)
      condition.eq(Warehouse::getStatus, EnableStatus.fromCode(query.getStatus()));
    if (query.getLocationEnabled() != null)
      condition.eq(Warehouse::getLocationEnabled, query.getLocationEnabled());
    SFunction<Warehouse, ?> sort =
        switch (query.getSortField() == null ? "warehouseCode" : query.getSortField()) {
          case "warehouseCode" -> Warehouse::getWarehouseCode;
          case "warehouseName" -> Warehouse::getWarehouseName;
          case "updateTime" -> Warehouse::getUpdateTime;
          default -> throw new ServiceException("不支持的排序字段");
        };
    condition
        .orderBy(true, "asc".equals(query.getSortDirection()), sort)
        .orderByAsc(Warehouse::getWarehouseId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<Warehouse> found = mapper.selectList(condition);
      long total = new PageInfo<>(found).getTotal();
      return new PageResult<>(found.stream().map(WarehouseView::from).toList(), total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 返回有效仓库的维护详情。 */
  public WarehouseView detail(Long id) {
    return WarehouseView.from(require(id));
  }

  /** 新增只接受维护字段，唯一索引保护并发编码冲突。 */
  @Transactional(rollbackFor = Exception.class)
  public WarehouseView create(SaveWarehouseRequest request) {
    validate(request);
    if (request.getWarehouseId() != null || request.getVersion() != null)
      throw new ServiceException("新增仓库不能指定 ID 或版本号");
    checkCode(request.getWarehouseCode(), null);
    Warehouse warehouse = new Warehouse();
    copy(request, warehouse);
    warehouse.setVersion(0);
    try {
      mapper.insert(warehouse);
    } catch (DuplicateKeyException exception) {
      throw duplicate();
    }
    return WarehouseView.from(warehouse);
  }

  /** 持仓库锁校验版本及货位管理变更，不修改货位自身状态。 */
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveWarehouseRequest request) {
    validate(request);
    Warehouse warehouse = lock(request.getWarehouseId());
    version(warehouse.getVersion(), request.getVersion());
    if (!Boolean.TRUE.equals(request.getLocationEnabled())
        && countLocations(warehouse.getWarehouseId()) > 0)
      throw new ServiceException("仓库存在未删除货位，不能关闭货位管理");
    checkCode(request.getWarehouseCode(), warehouse.getWarehouseId());
    copy(request, warehouse);
    if (warehouse.getVersion() == Integer.MAX_VALUE) throw new ServiceException("仓库版本已达上限");
    warehouse.setVersion(warehouse.getVersion() + 1);
    try {
      if (mapper.update(
              warehouse,
              Wrappers.<Warehouse>lambdaUpdate()
                  .eq(Warehouse::getWarehouseId, warehouse.getWarehouseId())
                  .eq(Warehouse::getVersion, request.getVersion()))
          != 1) throw stale();
    } catch (DuplicateKeyException exception) {
      throw duplicate();
    }
  }

  /** 删除前锁住仓库，所有未删除货位均阻止删除。 */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    Warehouse warehouse = lock(id);
    version(warehouse.getVersion(), version);
    if (countLocations(id) > 0) throw new ServiceException("仓库存在未删除货位，不能删除");
    if (mapper.deleteById(warehouse) != 1) throw stale();
  }

  /** 获取未删除仓库，用于只读上下文；调用方不得将实体直接作为接口响应。 */
  public Warehouse require(Long id) {
    if (id == null || id <= 0) throw new ServiceException("仓库 ID 必须为正数");
    Warehouse warehouse = mapper.selectById(id);
    if (warehouse == null) throw new ServiceException("仓库不存在或已删除");
    return warehouse;
  }

  /** 在调用方事务中先取得仓库行锁，返回持锁后的当前值。 */
  public Warehouse lock(Long id) {
    if (id == null || id <= 0) throw new ServiceException("仓库 ID 必须为正数");
    Warehouse warehouse =
        mapper.selectOne(
            Wrappers.<Warehouse>lambdaQuery().eq(Warehouse::getWarehouseId, id).last("FOR UPDATE"));
    if (warehouse == null) throw new ServiceException("仓库不存在或已删除");
    return warehouse;
  }

  /** 仓库锁保护下检查全部有效货位，包括已停用货位。 */
  private long countLocations(Long id) {
    return locations.selectCount(Wrappers.<Location>lambdaQuery().eq(Location::getWarehouseId, id));
  }

  /** 编码占用包含逻辑删除记录。 */
  private void checkCode(String code, Long excluded) {
    if (mapper.countCode(code, excluded) > 0) throw duplicate();
  }

  /** 只复制维护字段，审计与删除标记交给公共机制。 */
  private void copy(SaveWarehouseRequest source, Warehouse target) {
    target.setWarehouseCode(source.getWarehouseCode());
    target.setWarehouseName(source.getWarehouseName());
    target.setLocationEnabled(source.getLocationEnabled());
    target.setStatus(source.getStatus());
    target.setRemark(source.getRemark());
  }

  /** 统一服务内部调用和 HTTP 参数的校验规则。 */
  private void validate(Object value) {
    if (value == null) throw new ServiceException("参数不能为空");
    var errors = validator.validate(value);
    if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
  }

  /** 旧页面或遗漏版本号均不得覆盖新数据。 */
  private void version(Integer current, Integer expected) {
    if (expected == null || !expected.equals(current)) throw stale();
  }

  /** 返回可理解的编码占用提示，不暴露数据库信息。 */
  private ServiceException duplicate() {
    return new ServiceException("仓库编码已占用（含已删除仓库）");
  }

  /** 返回统一并发冲突提示。 */
  private ServiceException stale() {
    return new ServiceException("仓库已变更，请刷新后重试");
  }

  /** 包含查询把通配符作为普通文本。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
