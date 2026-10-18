package com.ruoyi.business.inventory.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.inventory.dto.InventoryQuery;
import com.ruoyi.business.inventory.mapper.InventoryReadMapper;
import com.ruoyi.business.inventory.vo.*;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.exception.ServiceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/** 只读库存查询，不要求拥有基础档案维护权限。 */
@Service
public class InventoryQueryService {
  private final InventoryReadMapper mapper;
  private final Validator validator;

  /** 注入只读查询及参数验证。 */
  public InventoryQueryService(InventoryReadMapper mapper, Validator validator) {
    this.mapper = mapper;
    this.validator = validator;
  }

  /** 返回库存余额，不接受流水专用排序。 */
  public PageResult<BalanceView> balances(InventoryQuery q) {
    validate(q);
    if (q.getSortField() != null && !"quantity".equals(q.getSortField()))
      throw new ServiceException("余额不支持此排序字段");
    return page(q, () -> mapper.balances(q, escape(q.getKeyword())));
  }

  /** 返回实际记账流水，不接受余额专用排序。 */
  public PageResult<TransactionView> transactions(InventoryQuery q) {
    validate(q);
    if (q.getSortField() != null && !"postedTime".equals(q.getSortField()))
      throw new ServiceException("流水不支持此排序字段");
    return page(q, () -> mapper.transactions(q, escape(q.getKeyword()), escape(q.getReceiptNo())));
  }

  /** 保留查询总数，保证分页线程上下文清理。 */
  private <T> PageResult<T> page(InventoryQuery q, Supplier<List<T>> query) {
    try {
      PageHelper.startPage(q.getPageNum(), q.getPageSize()).setReasonable(false);
      var rows = query.get();
      return new PageResult<>(rows, new PageInfo<>(rows).getTotal());
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 字段、范围及相斥维度条件在服务入口统一验证。 */
  private void validate(InventoryQuery q) {
    if (q == null) throw new ServiceException("查询不能为空");
    var errors = validator.validate(q);
    if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
    if (q.getBeginTime() != null
        && q.getEndTime() != null
        && q.getBeginTime().isAfter(q.getEndTime())) throw new ServiceException("开始时间不能晚于结束时间");
    if (Boolean.TRUE.equals(q.getWithoutLocation()) && q.getLocationId() != null)
      throw new ServiceException("货位查询条件冲突");
  }

  /** 查询文本转义，空白视作未填写。 */
  private String escape(String value) {
    return value == null
        ? null
        : value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
