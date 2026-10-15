package com.ruoyi.business.masterdata.unit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.business.masterdata.unit.dto.SaveUnitRequest;
import com.ruoyi.business.masterdata.unit.dto.UnitQuery;
import com.ruoyi.business.masterdata.unit.mapper.UnitMapper;
import com.ruoyi.business.masterdata.unit.service.UnitService;
import com.ruoyi.business.masterdata.unit.vo.UnitView;
import com.ruoyi.common.constant.LogicDeleteConstants;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 单位业务实现：数据库分页、唯一约束和单行事务版本控制。 */
@Service
public class UnitServiceImpl implements UnitService {
  private final UnitMapper mapper;

  /** 注入单位数据访问。 */
  public UnitServiceImpl(UnitMapper mapper) {
    this.mapper = mapper;
  }

  /** 在唯一一次列表查询前开启分页，转换 VO 前保留总数，最终清理线程状态。 */
  @Override
  public PageResult<UnitView> list(UnitQuery query) {
    if (query.getPageNum() == null
        || query.getPageNum() < 1
        || query.getPageSize() == null
        || query.getPageSize() < 1
        || query.getPageSize() > 100) {
      throw new ServiceException("分页参数无效，每页允许 1～100 条");
    }
    LambdaQueryWrapper<Unit> wrapper = Wrappers.lambdaQuery();
    if (query.getUnitCode() != null)
      wrapper.like(Unit::getUnitCode, escapeLike(query.getUnitCode()));
    if (query.getUnitName() != null)
      wrapper.like(Unit::getUnitName, escapeLike(query.getUnitName()));
    if (query.getStatus() != null)
      wrapper.eq(Unit::getStatus, EnableStatus.fromCode(query.getStatus()));
    SFunction<Unit, ?> field = sortField(query.getSortField());
    boolean ascending =
        switch (query.getSortDirection() == null ? "" : query.getSortDirection()) {
          case "asc" -> true;
          case "desc" -> false;
          default -> throw new ServiceException("排序方向必须为 asc 或 desc");
        };
    wrapper.orderBy(true, ascending, field).orderByAsc(Unit::getUnitId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<Unit> units = mapper.selectList(wrapper);
      long total = new PageInfo<>(units).getTotal();
      return new PageResult<>(units.stream().map(UnitView::from).toList(), total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 查询有效详情，不返回内部删除标记。 */
  @Override
  public UnitView detail(Long id) {
    return UnitView.from(requireUnit(id));
  }

  /** 新增只接受维护字段；数据库唯一索引兜底并发重复。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public UnitView create(SaveUnitRequest request) {
    if (request.getUnitId() != null || request.getVersion() != null) {
      throw new ServiceException("新增单位不能指定 ID 或版本号");
    }
    checkUnique(request, null);
    Unit unit = new Unit();
    copyFields(request, unit);
    unit.setVersion(0);
    try {
      mapper.insert(unit);
    } catch (DuplicateKeyException exception) {
      throw duplicateError();
    }
    return UnitView.from(requireUnit(unit.getUnitId()));
  }

  /** 持有单位行锁检查版本，再按旧版本执行原子更新。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveUnitRequest request) {
    Unit unit = lockExisting(request.getUnitId(), request.getVersion());
    checkUnique(request, unit.getUnitId());
    copyFields(request, unit);
    if (unit.getVersion() == Integer.MAX_VALUE) throw new ServiceException("单位版本已达上限，请联系管理员");
    unit.setVersion(unit.getVersion() + 1);
    try {
      int affected =
          mapper.update(
              unit,
              Wrappers.<Unit>lambdaUpdate()
                  .eq(Unit::getUnitId, unit.getUnitId())
                  .eq(Unit::getVersion, request.getVersion()));
      if (affected != 1) throw new ServiceException("单位已变更，请刷新后重试");
    } catch (DuplicateKeyException exception) {
      throw duplicateError();
    }
  }

  /** 持锁校验当前版本，使用实体删除保证审计填充。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    Unit unit = lockExisting(id, version);
    if (mapper.deleteById(unit) != 1) throw new ServiceException("单位已变更，请刷新后重试");
  }

  /** 将允许的接口字段映射到类型安全的列，不拼接用户排序表达式。 */
  private SFunction<Unit, ?> sortField(String field) {
    return switch (field == null ? "unitCode" : field) {
      case "unitCode" -> Unit::getUnitCode;
      case "unitName" -> Unit::getUnitName;
      case "updateTime" -> Unit::getUpdateTime;
      default -> throw new ServiceException("不支持的排序字段");
    };
  }

  /** 将用户输入视为包含文本，避免百分号和下划线意外扩大匹配范围。 */
  private String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 锁住目标行并基于最新提交值判断页面版本。 */
  private Unit lockExisting(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0) {
      throw new ServiceException("修改或删除单位必须指定有效 ID 和版本号");
    }
    if (mapper.lockUnit(id, LogicDeleteConstants.NOT_DELETED) == null) {
      throw new ServiceException("单位不存在或已删除");
    }
    Unit unit = requireUnit(id);
    if (!version.equals(unit.getVersion())) throw new ServiceException("单位已变更，请刷新后重试");
    return unit;
  }

  /** 获取有效实体，统一处理不存在和已删除记录。 */
  private Unit requireUnit(Long id) {
    if (id == null || id <= 0) throw new ServiceException("单位 ID 必须为正数");
    Unit unit = mapper.selectById(id);
    if (unit == null) throw new ServiceException("单位不存在或已删除");
    return unit;
  }

  /** 提前区分编码和名称占用，范围包括已删除记录。 */
  private void checkUnique(SaveUnitRequest request, Long excludedId) {
    if (mapper.countCodeIncludingDeleted(request.getUnitCode(), excludedId) > 0) {
      throw new ServiceException("单位编码已占用（含已删除单位）");
    }
    if (mapper.countNameIncludingDeleted(request.getUnitName(), excludedId) > 0) {
      throw new ServiceException("单位名称已占用（含已删除单位）");
    }
  }

  /** 并发写入发生唯一索引冲突时返回受控提示，不泄露数据库错误。 */
  private ServiceException duplicateError() {
    return new ServiceException("单位编码或名称已占用（含已删除单位），请修改后重试");
  }

  /** 仅复制允许用户维护的字段，创建审计和内部标记由公共机制维护。 */
  private void copyFields(SaveUnitRequest request, Unit unit) {
    unit.setUnitCode(request.getUnitCode());
    unit.setUnitName(request.getUnitName());
    unit.setStatus(request.getStatus());
    unit.setRemark(request.getRemark());
  }
}
