package com.ruoyi.business.masterdata.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper;
import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.dto.SupplierQuery;
import com.ruoyi.business.masterdata.supplier.mapper.SupplierMapper;
import com.ruoyi.business.masterdata.supplier.service.SupplierService;
import com.ruoyi.business.masterdata.supplier.vo.SupplierOption;
import com.ruoyi.business.masterdata.supplier.vo.SupplierView;
import com.ruoyi.common.constant.LogicDeleteConstants;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 供应商业务实现：数据库分页、唯一约束和单行事务版本控制。 */
@Service
public class SupplierServiceImpl implements SupplierService {
  private final SupplierMapper mapper;
  private final MasterDataReferenceMapper references;

  /** 注入供应商数据访问。 */
  public SupplierServiceImpl(SupplierMapper mapper, MasterDataReferenceMapper references) {
    this.mapper = mapper;
    this.references = references;
  }

  /** 在唯一一次列表查询前开启分页，转换 VO 前保留总数，最终清理线程状态。 */
  @Override
  public PageResult<SupplierView> list(SupplierQuery query) {
    if (query.getPageNum() == null
        || query.getPageNum() < 1
        || query.getPageSize() == null
        || query.getPageSize() < 1
        || query.getPageSize() > 100) {
      throw new ServiceException("分页参数无效，每页允许 1～100 条");
    }
    LambdaQueryWrapper<Supplier> wrapper = Wrappers.lambdaQuery();
    if (query.getSupplierCode() != null)
      wrapper.like(Supplier::getSupplierCode, escapeLike(query.getSupplierCode()));
    if (query.getSupplierName() != null)
      wrapper.like(Supplier::getSupplierName, escapeLike(query.getSupplierName()));
    if (query.getStatus() != null)
      wrapper.eq(Supplier::getStatus, EnableStatus.fromCode(query.getStatus()));
    SFunction<Supplier, ?> field = sortField(query.getSortField());
    boolean ascending =
        switch (query.getSortDirection() == null ? "" : query.getSortDirection()) {
          case "asc" -> true;
          case "desc" -> false;
          default -> throw new ServiceException("排序方向必须为 asc 或 desc");
        };
    wrapper.orderBy(true, ascending, field).orderByAsc(Supplier::getSupplierId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<Supplier> suppliers = mapper.selectList(wrapper);
      long total = new PageInfo<>(suppliers).getTotal();
      return new PageResult<>(suppliers.stream().map(SupplierView::from).toList(), total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 完整参照选项保留停用旧值，不返回审计、版本或删除标记。 */
  @Override
  public List<SupplierOption> options() {
    return mapper
        .selectList(
            Wrappers.<Supplier>lambdaQuery()
                .orderByAsc(Supplier::getSupplierCode, Supplier::getSupplierId))
        .stream()
        .map(SupplierOption::from)
        .toList();
  }

  /** 查询有效详情，不返回内部删除标记。 */
  @Override
  public SupplierView detail(Long id) {
    return SupplierView.from(requireSupplier(id));
  }

  /** 新增只接受维护字段；数据库唯一索引兜底并发重复。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public SupplierView create(SaveSupplierRequest request) {
    if (request.getSupplierId() != null || request.getVersion() != null) {
      throw new ServiceException("新增供应商不能指定 ID 或版本号");
    }
    checkUnique(request, null);
    Supplier supplier = new Supplier();
    copyFields(request, supplier);
    supplier.setVersion(0);
    try {
      mapper.insert(supplier);
    } catch (DuplicateKeyException exception) {
      throw duplicateError();
    }
    return SupplierView.from(requireSupplier(supplier.getSupplierId()));
  }

  /** 持有供应商行锁检查版本，再按旧版本执行原子更新。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(SaveSupplierRequest request) {
    Supplier supplier = lockExisting(request.getSupplierId(), request.getVersion());
    if (request.getStatus() == EnableStatus.DISABLED
        && references.supplierPurchaseDocuments(supplier.getSupplierId(), true) > 0)
      throw new ServiceException("存在未完成采购单据，不能停用供应商");
    checkUnique(request, supplier.getSupplierId());
    copyFields(request, supplier);
    if (supplier.getVersion() == Integer.MAX_VALUE) throw new ServiceException("供应商版本已达上限，请联系管理员");
    supplier.setVersion(supplier.getVersion() + 1);
    try {
      int affected =
          mapper.update(
              supplier,
              Wrappers.<Supplier>lambdaUpdate()
                  .eq(Supplier::getSupplierId, supplier.getSupplierId())
                  .eq(Supplier::getVersion, request.getVersion()));
      if (affected != 1) throw new ServiceException("供应商已变更，请刷新后重试");
    } catch (DuplicateKeyException exception) {
      throw duplicateError();
    }
  }

  /** 持锁校验当前版本，使用实体删除保证审计填充。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    Supplier supplier = lockExisting(id, version);
    if (references.supplierPurchaseDocuments(id, false) > 0)
      throw new ServiceException("存在采购单据引用，不能删除供应商");
    if (mapper.deleteById(supplier) != 1) throw new ServiceException("供应商已变更，请刷新后重试");
  }

  /** 将允许的接口字段映射到类型安全的列，不拼接用户排序表达式。 */
  private SFunction<Supplier, ?> sortField(String field) {
    return switch (field == null ? "supplierCode" : field) {
      case "supplierCode" -> Supplier::getSupplierCode;
      case "supplierName" -> Supplier::getSupplierName;
      case "updateTime" -> Supplier::getUpdateTime;
      default -> throw new ServiceException("不支持的排序字段");
    };
  }

  /** 将用户输入视为包含文本，避免百分号和下划线意外扩大匹配范围。 */
  private String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 锁住目标行并基于最新提交值判断页面版本。 */
  private Supplier lockExisting(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0) {
      throw new ServiceException("修改或删除供应商必须指定有效 ID 和版本号");
    }
    if (mapper.lockSupplier(id, LogicDeleteConstants.NOT_DELETED) == null) {
      throw new ServiceException("供应商不存在或已删除");
    }
    Supplier supplier = requireSupplier(id);
    if (!version.equals(supplier.getVersion())) throw new ServiceException("供应商已变更，请刷新后重试");
    return supplier;
  }

  /** 获取有效实体，统一处理不存在和已删除记录。 */
  private Supplier requireSupplier(Long id) {
    if (id == null || id <= 0) throw new ServiceException("供应商 ID 必须为正数");
    Supplier supplier = mapper.selectById(id);
    if (supplier == null) throw new ServiceException("供应商不存在或已删除");
    return supplier;
  }

  /** 提前区分编码占用，范围包括已删除记录。 */
  private void checkUnique(SaveSupplierRequest request, Long excludedId) {
    if (mapper.countCodeIncludingDeleted(request.getSupplierCode(), excludedId) > 0) {
      throw new ServiceException("供应商编码已占用（含已删除供应商）");
    }
  }

  /** 并发写入发生唯一索引冲突时返回受控提示，不泄露数据库错误。 */
  private ServiceException duplicateError() {
    return new ServiceException("供应商编码已占用（含已删除供应商），请修改后重试");
  }

  /** 仅复制允许用户维护的字段，创建审计和内部标记由公共机制维护。 */
  private void copyFields(SaveSupplierRequest request, Supplier supplier) {
    supplier.setSupplierCode(request.getSupplierCode());
    supplier.setSupplierName(request.getSupplierName());
    supplier.setShortName(request.getShortName());
    supplier.setContactName(request.getContactName());
    supplier.setContactPhone(request.getContactPhone());
    supplier.setAddress(request.getAddress());
    supplier.setStatus(request.getStatus());
    supplier.setRemark(request.getRemark());
  }
}
