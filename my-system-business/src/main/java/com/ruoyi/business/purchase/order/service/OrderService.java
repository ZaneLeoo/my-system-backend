package com.ruoyi.business.purchase.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.business.masterdata.category.mapper.ItemCategoryMapper;
import com.ruoyi.business.masterdata.item.domain.Item;
import com.ruoyi.business.masterdata.item.mapper.ItemMapper;
import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.business.masterdata.supplier.mapper.SupplierMapper;
import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.business.masterdata.unit.mapper.UnitMapper;
import com.ruoyi.business.purchase.arrival.domain.ReceiptProgress;
import com.ruoyi.business.purchase.order.domain.*;
import com.ruoyi.business.purchase.order.dto.*;
import com.ruoyi.business.purchase.order.mapper.*;
import com.ruoyi.business.purchase.order.vo.*;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 整单事务入口，功能权限由 Controller 检查，当前不按部门隔离订单。 */
@Service
public class OrderService {
  private final OrderMapper orders;
  private final OrderLineMapper lines;
  private final SupplierMapper suppliers;
  private final ItemMapper items;
  private final UnitMapper units;
  private final ItemCategoryMapper categories;
  private final OrderCalculator calculator;
  private final Validator validator;

  /** 注入持久化、公共验证与纯金额计算组件，不依赖档案维护 Service。 */
  public OrderService(
      OrderMapper orders,
      OrderLineMapper lines,
      SupplierMapper suppliers,
      ItemMapper items,
      UnitMapper units,
      ItemCategoryMapper categories,
      OrderCalculator calculator,
      Validator validator) {
    this.orders = orders;
    this.lines = lines;
    this.suppliers = suppliers;
    this.items = items;
    this.units = units;
    this.categories = categories;
    this.calculator = calculator;
    this.validator = validator;
  }

  /** 按白名单查询，分页仅覆盖一次主表读取，转换前保留总数。 */
  @Transactional(readOnly = true)
  public PageResult<OrderView> list(OrderQuery query) {
    validate(query);
    if (query.getBeginDate() != null
        && query.getEndDate() != null
        && query.getBeginDate().isAfter(query.getEndDate()))
      throw new ServiceException("开始日期不能晚于结束日期");
    LambdaQueryWrapper<PurchaseOrder> wrapper = Wrappers.lambdaQuery();
    if (query.getOrderNo() != null)
      wrapper.like(PurchaseOrder::getOrderNo, escape(query.getOrderNo()));
    if (query.getSupplierId() != null)
      wrapper.eq(PurchaseOrder::getSupplierId, query.getSupplierId());
    if (query.getStatus() != null)
      wrapper.eq(PurchaseOrder::getStatus, OrderStatus.fromCode(query.getStatus()));
    if (query.getBeginDate() != null) wrapper.ge(PurchaseOrder::getOrderDate, query.getBeginDate());
    if (query.getEndDate() != null) wrapper.le(PurchaseOrder::getOrderDate, query.getEndDate());
    SFunction<PurchaseOrder, ?> sort =
        switch (query.getSortField() == null ? "orderDate" : query.getSortField()) {
          case "orderNo" -> PurchaseOrder::getOrderNo;
          case "orderDate" -> PurchaseOrder::getOrderDate;
          case "updateTime" -> PurchaseOrder::getUpdateTime;
          case "grossAmount" -> PurchaseOrder::getGrossAmount;
          default -> throw new ServiceException("不支持的排序字段");
        };
    wrapper
        .orderBy(true, "asc".equals(query.getSortDirection()), sort)
        .orderByDesc(PurchaseOrder::getOrderId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<PurchaseOrder> found = orders.selectList(wrapper);
      long total = new PageInfo<>(found).getTotal();
      PageHelper.clearPage();
      Map<Long, OrderExecutionProgress> progress =
          found.isEmpty()
              ? Map.of()
              : orders
                  .executionProgress(found.stream().map(PurchaseOrder::getOrderId).toList())
                  .stream()
                  .collect(
                      Collectors.toMap(OrderExecutionProgress::getOrderId, Function.identity()));
      return new PageResult<>(
          found.stream()
              .map(
                  row -> {
                    OrderView view = OrderView.from(row);
                    var execution = progress.get(row.getOrderId());
                    if (execution != null) {
                      view.setArrivalProgress(execution.getArrivalProgress());
                      view.setReceiptProgress(execution.getReceiptProgress());
                    }
                    return view;
                  })
              .toList(),
          total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 同一只读事务返回主表与有序明细，避免读到不同提交版本。 */
  @Transactional(readOnly = true)
  public OrderView detail(Long id) {
    PurchaseOrder order = require(id);
    OrderView view = OrderView.from(order);
    var receipts =
        orders.receiptQuantities(List.of(id)).stream()
            .collect(
                Collectors.toMap(OrderLineReceiptQuantities::getOrderLineId, Function.identity()));
    view.setLines(
        existingLines(id).stream()
            .map(
                row -> {
                  var line = OrderLineView.from(row);
                  var receipt = receipts.get(row.getLineId());
                  if (receipt != null) {
                    line.setPendingReceiptQuantity(receipt.getPendingReceiptQuantity());
                    line.setReceivedQuantity(receipt.getReceivedQuantity());
                  }
                  line.setArrivedNotReceivedQuantity(
                      row.getArrivedQuantity().subtract(line.getReceivedQuantity()));
                  return line;
                })
            .toList());
    boolean anyReceived =
        view.getLines().stream().anyMatch(line -> line.getReceivedQuantity().signum() > 0);
    boolean allReceived =
        !view.getLines().isEmpty()
            && view.getLines().stream()
                .allMatch(line -> line.getReceivedQuantity().compareTo(line.getQuantity()) == 0);
    view.setReceiptProgress(
        allReceived
            ? ReceiptProgress.FULLY_RECEIVED
            : anyReceived ? ReceiptProgress.PARTIALLY_RECEIVED : ReceiptProgress.NOT_RECEIVED);
    boolean any = view.getLines().stream().anyMatch(line -> line.getArrivedQuantity().signum() > 0);
    boolean all =
        !view.getLines().isEmpty()
            && view.getLines().stream()
                .allMatch(line -> line.getArrivedQuantity().compareTo(line.getQuantity()) == 0);
    view.setArrivalProgress(
        all
            ? ArrivalProgress.FULLY_ARRIVED
            : any ? ArrivalProgress.PARTIALLY_ARRIVED : ArrivalProgress.NOT_ARRIVED);
    return view;
  }

  /** 获取当前有效员工、部门及系统精度，不开放人员维护数据。 */
  public OrderDefaults defaults() {
    OrderDefaults result = orders.defaults(SecurityUtils.getUserId(), SecurityUtils.getDeptId());
    if (result == null) throw new ServiceException("当前员工或部门无效，不能创建采购订单");
    result.setQuantityDecimalPlaces(configDecimals(OrderRules.QUANTITY_CONFIG));
    result.setPriceDecimalPlaces(configDecimals(OrderRules.PRICE_CONFIG));
    result.setAmountDecimalPlaces(OrderRules.MONEY_DECIMALS);
    result.setRoundingMode(OrderRules.ROUNDING.name());
    return result;
  }

  /** 新单使用当前参数，旧单仅使用服务器保存的精度和计价方式。 */
  @Transactional(readOnly = true)
  public OrderCalculation calculate(CalculationRequest request) {
    validate(request);
    int quantity, price;
    if (request.getOrderId() != null) {
      PurchaseOrder order = require(request.getOrderId());
      requireMode(order, request.getPriceTaxMode());
      quantity = order.getQuantityDecimalPlaces();
      price = order.getPriceDecimalPlaces();
    } else {
      quantity = configDecimals(OrderRules.QUANTITY_CONFIG);
      price = configDecimals(OrderRules.PRICE_CONFIG);
    }
    return calculator.calculate(request.getPriceTaxMode(), request.getLines(), quantity, price);
  }

  /** 新增草稿，正式单号、明细、金额及精度快照全部在同一事务提交。 */
  @Transactional(rollbackFor = Exception.class)
  public OrderView create(SaveOrderRequest request) {
    validate(request);
    if (request.getOrderId() != null
        || request.getVersion() != null
        || request.getLines().stream().anyMatch(l -> l.getLineId() != null))
      throw new ServiceException("新增订单不能指定订单 ID、行 ID 或版本号");
    lockTree();
    OrderDefaults defaults = defaults();
    PurchaseOrder order = new PurchaseOrder();
    order.setBuyerId(defaults.getBuyerId());
    order.setBuyerName(defaults.getBuyerName());
    order.setDeptId(defaults.getDeptId());
    order.setDeptName(defaults.getDeptName());
    order.setQuantityDecimalPlaces(defaults.getQuantityDecimalPlaces());
    order.setPriceDecimalPlaces(defaults.getPriceDecimalPlaces());
    order.setAmountDecimalPlaces(OrderRules.MONEY_DECIMALS);
    order.setRoundingMode(OrderRules.ROUNDING.name());
    order.setCurrency(OrderRules.CURRENCY);
    order.setStatus(OrderStatus.DRAFT);
    order.setVersion(0);
    order.setReviewRemark("");
    order.setCloseReason("");
    order.setOrderNo("PENDING-" + UUID.randomUUID());
    Map<Long, Item> selected = validateReferences(request.getSupplierId(), request.getLines());
    copyHeader(request, order);
    OrderCalculation amounts =
        calculator.calculate(
            request.getPriceTaxMode(),
            request.getLines(),
            order.getQuantityDecimalPlaces(),
            order.getPriceDecimalPlaces());
    setTotals(order, amounts, request.getLines().size());
    orders.insert(order);
    order.setOrderNo(
        "PO"
            + LocalDate.now(OrderRules.TIME_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE)
            + String.format(Locale.ROOT, "%010d", order.getOrderId()));
    if (orders.updateById(order) != 1) throw stale();
    saveLines(order, request.getLines(), selected, amounts, List.of());
    return detail(order.getOrderId());
  }

  /** 持锁校验整单版本，保留归属与精度，并按稳定行 ID 更新明细。 */
  @Transactional(rollbackFor = Exception.class)
  public OrderView update(SaveOrderRequest request) {
    validate(request);
    lockTree();
    PurchaseOrder order = lock(request.getOrderId(), request.getVersion());
    requireStatus(order, OrderStatus.DRAFT);
    requireMode(order, request.getPriceTaxMode());
    Map<Long, Item> selected = validateReferences(request.getSupplierId(), request.getLines());
    List<PurchaseOrderLine> previous = existingLines(order.getOrderId());
    OrderCalculation amounts =
        calculator.calculate(
            order.getPriceTaxMode(),
            request.getLines(),
            order.getQuantityDecimalPlaces(),
            order.getPriceDecimalPlaces());
    copyHeader(request, order);
    setTotals(order, amounts, request.getLines().size());
    saveLines(order, request.getLines(), selected, amounts, previous);
    updateVersion(order, request.getVersion());
    return detail(order.getOrderId());
  }

  /** 仅草稿可删除；父子实体逻辑删除均触发公共审计。 */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    lockTree();
    PurchaseOrder order = lock(id, version);
    requireStatus(order, OrderStatus.DRAFT);
    for (PurchaseOrderLine line : existingLines(id)) if (lines.deleteById(line) != 1) throw stale();
    if (orders.deleteById(order) != 1) throw stale();
  }

  /** 状态转换只修改表头，不重算或覆盖历史快照。 */
  @Transactional(rollbackFor = Exception.class)
  public OrderView transition(Long id, OrderAction action, OrderActionRequest request) {
    validate(request);
    lockTree();
    PurchaseOrder order = lock(id, request.getVersion());
    requireStatus(order, action.getSource());
    if ((action == OrderAction.REJECT || action == OrderAction.CLOSE)
        && request.getReason().isBlank()) throw new ServiceException("驳回或关闭订单必须填写原因");
    if (action == OrderAction.SUBMIT || action == OrderAction.APPROVE) {
      List<OrderLineInput> inputs =
          existingLines(id).stream().map(l -> toInput(l, order.getPriceTaxMode())).toList();
      validateReferences(order.getSupplierId(), inputs);
    }
    String actor = SecurityUtils.getUsername();
    LocalDateTime time = LocalDateTime.now(OrderRules.TIME_ZONE);
    switch (action) {
      case SUBMIT -> {
        order.setSubmittedBy(actor);
        order.setSubmittedTime(time);
        order.setReviewedBy(null);
        order.setReviewedTime(null);
        order.setReviewRemark("");
      }
      case WITHDRAW -> {
        order.setSubmittedBy(null);
        order.setSubmittedTime(null);
      }
      case APPROVE, REJECT -> {
        order.setReviewedBy(actor);
        order.setReviewedTime(time);
        order.setReviewRemark(request.getReason());
      }
      case CLOSE -> {
        if (orders.unfinishedArrivals(id) > 0)
          throw new ServiceException("存在草稿或已提交的到货单，请先处理后再关闭订单");
        order.setClosedBy(actor);
        order.setClosedTime(time);
        order.setCloseReason(request.getReason());
      }
    }
    order.setStatus(action.getTarget());
    updateVersion(order, request.getVersion());
    return detail(id);
  }

  /** 写入前以固定顺序锁住供应商、单位和料品，并批量验证分类。 */
  private Map<Long, Item> validateReferences(Long supplierId, List<OrderLineInput> input) {
    if (supplierId == null || input == null || input.isEmpty())
      throw new ServiceException("供应商与明细不能为空");
    Supplier supplier =
        suppliers.selectOne(
            Wrappers.<Supplier>lambdaQuery()
                .eq(Supplier::getSupplierId, supplierId)
                .last("FOR UPDATE"));
    if (supplier == null || supplier.getStatus() != EnableStatus.ENABLED)
      throw new ServiceException("请选择有效且启用的供应商");
    TreeSet<Long> unitIds = new TreeSet<>(), itemIds = new TreeSet<>();
    for (OrderLineInput line : input) {
      if (line == null || line.getUnitId() == null || line.getItemId() == null)
        throw new ServiceException("请选择料品及库存单位");
      unitIds.add(line.getUnitId());
      itemIds.add(line.getItemId());
    }
    Map<Long, Unit> unitIndex =
        units
            .selectList(
                Wrappers.<Unit>lambdaQuery()
                    .in(Unit::getUnitId, unitIds)
                    .orderByAsc(Unit::getUnitId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
    Map<Long, Item> itemIndex =
        items
            .selectList(
                Wrappers.<Item>lambdaQuery()
                    .in(Item::getItemId, itemIds)
                    .orderByAsc(Item::getItemId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Item::getItemId, Function.identity()));
    Set<Long> categoryIds =
        itemIndex.values().stream().map(Item::getCategoryId).collect(Collectors.toSet());
    Map<Long, ItemCategory> categoryIndex =
        categoryIds.isEmpty()
            ? Map.of()
            : categories.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(ItemCategory::getCategoryId, Function.identity()));
    for (OrderLineInput line : input) {
      Item item = itemIndex.get(line.getItemId());
      Unit unit = unitIndex.get(line.getUnitId());
      if (item == null
          || item.getStatus() != EnableStatus.ENABLED
          || !Boolean.TRUE.equals(item.getPurchaseEnabled()))
        throw new ServiceException("料品已失效、停用或不允许采购");
      ItemCategory category = categoryIndex.get(item.getCategoryId());
      if (category == null || category.getStatus() != CategoryStatus.ENABLED)
        throw new ServiceException("料品分类无效或已停用");
      if (unit == null || unit.getStatus() != EnableStatus.ENABLED)
        throw new ServiceException("单位无效或已停用");
      if (!line.getUnitId().equals(item.getBaseUnitId()))
        throw new ServiceException("暂不支持单位换算，订单单位必须等于料品库存单位");
    }
    return itemIndex;
  }

  /** 只复制维护字段和本次档案快照，绝不接受客户端归属、金额或状态。 */
  private void copyHeader(SaveOrderRequest request, PurchaseOrder order) {
    Supplier supplier =
        suppliers.selectOne(
            Wrappers.<Supplier>lambdaQuery()
                .eq(Supplier::getSupplierId, request.getSupplierId())
                .last("FOR UPDATE"));
    order.setOrderDate(request.getOrderDate());
    order.setSupplierId(supplier.getSupplierId());
    order.setSupplierCode(supplier.getSupplierCode());
    order.setSupplierName(supplier.getSupplierName());
    order.setContactName(request.getContactName());
    order.setContactPhone(request.getContactPhone());
    order.setAddress(request.getAddress());
    order.setRemark(request.getRemark());
    order.setPriceTaxMode(request.getPriceTaxMode());
  }

  /** 更新已有行、插入新行，移除遗漏行；禁止跨单行 ID、重复行 ID 或失效行复活。 */
  private void saveLines(
      PurchaseOrder order,
      List<OrderLineInput> input,
      Map<Long, Item> selected,
      OrderCalculation calculation,
      List<PurchaseOrderLine> previous) {
    Map<Long, PurchaseOrderLine> index =
        previous.stream()
            .collect(Collectors.toMap(PurchaseOrderLine::getLineId, Function.identity()));
    Set<Long> used = new HashSet<>();
    Map<Long, Unit> unitIndex =
        units
            .selectList(
                Wrappers.<Unit>lambdaQuery()
                    .in(
                        Unit::getUnitId,
                        input.stream().map(OrderLineInput::getUnitId).distinct().sorted().toList())
                    .orderByAsc(Unit::getUnitId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
    for (int i = 0; i < input.size(); i++) {
      OrderLineInput source = input.get(i);
      PurchaseOrderLine target;
      if (source.getLineId() == null) target = new PurchaseOrderLine();
      else {
        target = index.get(source.getLineId());
        if (target == null || !used.add(source.getLineId()))
          throw new ServiceException("明细行已变更、重复或不属于当前订单");
      }
      if (source.getExpectedDate() != null
          && source.getExpectedDate().isBefore(order.getOrderDate()))
        throw new ServiceException("第 " + (i + 1) + " 行预计到货日期不能早于订单日期");
      Item item = selected.get(source.getItemId());
      LineAmounts money = calculation.getLines().get(i);
      target.setOrderId(order.getOrderId());
      target.setLineNo(i + 1);
      target.setItemId(item.getItemId());
      target.setItemCode(item.getItemCode());
      target.setItemName(item.getItemName());
      target.setSpecification(item.getSpecification());
      target.setUnitId(source.getUnitId());
      target.setUnitName(unitIndex.get(source.getUnitId()).getUnitName());
      target.setQuantity(source.getQuantity());
      target.setTaxRate(source.getTaxRate());
      target.setNetPrice(money.getNetPrice());
      target.setGrossPrice(money.getGrossPrice());
      target.setNetAmount(money.getNetAmount());
      target.setTax(money.getTax());
      target.setGrossAmount(money.getGrossAmount());
      target.setExpectedDate(source.getExpectedDate());
      target.setRemark(source.getRemark());
      if (target.getLineId() == null) lines.insert(target);
      else if (lines.updateById(target) != 1) throw stale();
    }
    for (PurchaseOrderLine old : previous)
      if (!used.contains(old.getLineId()) && lines.deleteById(old) != 1) throw stale();
  }

  /** 对照状态前重新取得当前提交版本的主表行锁。 */
  private PurchaseOrder lock(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0)
      throw new ServiceException("必须指定有效订单 ID 和版本号");
    PurchaseOrder order =
        orders.selectOne(
            Wrappers.<PurchaseOrder>lambdaQuery()
                .eq(PurchaseOrder::getOrderId, id)
                .last("FOR UPDATE"));
    if (order == null) throw new ServiceException("订单不存在或已删除");
    if (!version.equals(order.getVersion())) throw stale();
    return order;
  }

  /** 分类锁协调既有料品修改与采购引用写入，所有写操作先取得该锁。 */
  private void lockTree() {
    if (categories.lockTree() == null) throw new ServiceException("分类事务锁尚未初始化");
  }

  /** 查询有效主表。 */
  private PurchaseOrder require(Long id) {
    if (id == null || id <= 0) throw new ServiceException("订单 ID 必须为正数");
    PurchaseOrder order = orders.selectById(id);
    if (order == null) throw new ServiceException("订单不存在或已删除");
    return order;
  }

  /** 按业务行号加载有效明细，不对每行重新请求档案。 */
  private List<PurchaseOrderLine> existingLines(Long id) {
    return lines.selectList(
        Wrappers.<PurchaseOrderLine>lambdaQuery()
            .eq(PurchaseOrderLine::getOrderId, id)
            .orderByAsc(PurchaseOrderLine::getLineNo, PurchaseOrderLine::getLineId));
  }

  /** 校验状态转换及草稿操作边界。 */
  private void requireStatus(PurchaseOrder order, OrderStatus status) {
    if (order.getStatus() != status) throw new ServiceException("当前订单状态不允许此操作，请刷新后重试");
  }

  /** 首次保存后不能通过接口更改计价口径。 */
  private void requireMode(PurchaseOrder order, PriceTaxMode mode) {
    if (order.getPriceTaxMode() != mode) throw new ServiceException("订单首次保存后不能切换计价方式");
  }

  /** 整单版本覆盖明细及全部状态操作，禁止整数溢出。 */
  private void updateVersion(PurchaseOrder order, Integer oldVersion) {
    if (oldVersion == Integer.MAX_VALUE) throw new ServiceException("订单版本已达上限");
    order.setVersion(oldVersion + 1);
    if (orders.update(
            order,
            Wrappers.<PurchaseOrder>lambdaUpdate()
                .eq(PurchaseOrder::getOrderId, order.getOrderId())
                .eq(PurchaseOrder::getVersion, oldVersion))
        != 1) throw stale();
  }

  /** 将已保存明细转成引用复核所需输入，不改变原金额。 */
  private OrderLineInput toInput(PurchaseOrderLine line, PriceTaxMode mode) {
    OrderLineInput input = new OrderLineInput();
    input.setItemId(line.getItemId());
    input.setUnitId(line.getUnitId());
    input.setQuantity(line.getQuantity());
    input.setTaxRate(line.getTaxRate());
    input.setUnitPrice(
        mode == PriceTaxMode.TAX_INCLUDED ? line.getGrossPrice() : line.getNetPrice());
    return input;
  }

  /** 汇总值只能来自纯计算结果。 */
  private void setTotals(PurchaseOrder order, OrderCalculation result, int size) {
    order.setLineCount(size);
    order.setNetAmount(result.getNetAmount());
    order.setTax(result.getTax());
    order.setGrossAmount(result.getGrossAmount());
  }

  /** 参数缺失采用已约定默认值，非法配置明确拒绝，不静默替换。 */
  private int configDecimals(String key) {
    String value = orders.configValue(key);
    if (value == null) return OrderRules.MAX_DECIMALS;
    if (!value.matches("[0-6]")) throw new ServiceException("系统参数 " + key + " 必须为 0～6 的整数");
    return Integer.parseInt(value);
  }

  /** Service 同样校验输入，避免未来 Agent 或其他内部调用绕过 Controller 校验。 */
  private <T> void validate(T request) {
    if (request == null) throw new ServiceException("请求不能为空");
    var violations = validator.validate(request);
    if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
  }

  /** 编号包含匹配按普通文本处理通配符。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 返回统一的整单冲突提示。 */
  private ServiceException stale() {
    return new ServiceException("订单已变更，请刷新后重试");
  }
}
