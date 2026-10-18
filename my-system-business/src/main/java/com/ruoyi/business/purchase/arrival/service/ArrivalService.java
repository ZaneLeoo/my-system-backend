package com.ruoyi.business.purchase.arrival.service;

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
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.business.masterdata.warehouse.mapper.WarehouseMapper;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.business.purchase.arrival.dto.*;
import com.ruoyi.business.purchase.arrival.mapper.*;
import com.ruoyi.business.purchase.arrival.vo.*;
import com.ruoyi.business.purchase.order.domain.*;
import com.ruoyi.business.purchase.order.mapper.*;
import com.ruoyi.business.purchase.order.vo.OrderDefaults;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 到货整单及订单执行数量的事务入口，人工审核及未来审批回调复用状态命令。 */
@Service
public class ArrivalService {
  private final ArrivalMapper arrivals;
  private final ArrivalLineMapper lines;
  private final OrderMapper orders;
  private final OrderLineMapper orderLines;
  private final OrderLineReferenceMapper references;
  private final WarehouseMapper warehouses;
  private final SupplierMapper suppliers;
  private final UnitMapper units;
  private final ItemMapper items;
  private final ItemCategoryMapper categories;
  private final Validator validator;
  private final com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper
      masterReferences;

  /** 注入持久化与公共校验组件，不反向依赖档案维护服务。 */
  public ArrivalService(
      ArrivalMapper arrivals,
      ArrivalLineMapper lines,
      OrderMapper orders,
      OrderLineMapper orderLines,
      OrderLineReferenceMapper references,
      WarehouseMapper warehouses,
      SupplierMapper suppliers,
      UnitMapper units,
      ItemMapper items,
      ItemCategoryMapper categories,
      Validator validator,
      com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper masterReferences) {
    this.arrivals = arrivals;
    this.lines = lines;
    this.orders = orders;
    this.orderLines = orderLines;
    this.references = references;
    this.warehouses = warehouses;
    this.suppliers = suppliers;
    this.units = units;
    this.items = items;
    this.categories = categories;
    this.validator = validator;
    this.masterReferences = masterReferences;
  }

  /** 分页只覆盖主查询，转换前保留总数，所有异常路径清理线程上下文。 */
  public PageResult<ArrivalView> list(ArrivalQuery query) {
    validate(query);
    if (query.getBeginDate() != null
        && query.getEndDate() != null
        && query.getBeginDate().isAfter(query.getEndDate()))
      throw new ServiceException("开始日期不能晚于结束日期");
    LambdaQueryWrapper<PurchaseArrival> condition = Wrappers.lambdaQuery();
    if (query.getArrivalNo() != null)
      condition.like(PurchaseArrival::getArrivalNo, escape(query.getArrivalNo()));
    if (query.getSupplierId() != null)
      condition.eq(PurchaseArrival::getSupplierId, query.getSupplierId());
    if (query.getWarehouseId() != null)
      condition.eq(PurchaseArrival::getWarehouseId, query.getWarehouseId());
    if (query.getStatus() != null)
      condition.eq(PurchaseArrival::getStatus, ArrivalStatus.fromCode(query.getStatus()));
    if (query.getOrderId() != null)
      condition.apply(
          "EXISTS (SELECT 1 FROM purchase_arrival_line al WHERE al.arrival_id=purchase_arrival.arrival_id AND al.del_flag='0' AND al.order_id={0})",
          query.getOrderId());
    if (query.getBeginDate() != null)
      condition.ge(PurchaseArrival::getArrivalDate, query.getBeginDate());
    if (query.getEndDate() != null)
      condition.le(PurchaseArrival::getArrivalDate, query.getEndDate());
    SFunction<PurchaseArrival, ?> sort =
        switch (query.getSortField() == null ? "arrivalDate" : query.getSortField()) {
          case "arrivalNo" -> PurchaseArrival::getArrivalNo;
          case "arrivalDate" -> PurchaseArrival::getArrivalDate;
          case "updateTime" -> PurchaseArrival::getUpdateTime;
          default -> throw new ServiceException("不支持的排序字段");
        };
    condition
        .orderBy(true, "asc".equals(query.getSortDirection()), sort)
        .orderByDesc(PurchaseArrival::getArrivalId);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<PurchaseArrival> found = arrivals.selectList(condition);
      long total = new PageInfo<>(found).getTotal();
      var progress = receiptProgress(found.stream().map(PurchaseArrival::getArrivalId).toList());
      return new PageResult<>(
          found.stream()
              .map(
                  h -> {
                    var view = ArrivalView.from(h);
                    view.setReceiptProgress(
                        progress.getOrDefault(h.getArrivalId(), ReceiptProgress.NOT_RECEIVED));
                    return view;
                  })
              .toList(),
          total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 同一快照事务批量补充订单当前执行数量，避免逐行查询。 */
  @Transactional(readOnly = true)
  public ArrivalView detail(Long id) {
    ArrivalView view = ArrivalView.from(require(id));
    List<PurchaseArrivalLine> saved = existingLines(id);
    view.setReceiptProgress(
        receiptProgress(List.of(id)).getOrDefault(id, ReceiptProgress.NOT_RECEIVED));
    Map<Long, PurchaseOrderLine> source =
        saved.isEmpty()
            ? Map.of()
            : orderLines
                .selectByIds(
                    saved.stream().map(PurchaseArrivalLine::getOrderLineId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getLineId, Function.identity()));
    view.setLines(
        saved.stream()
            .map(
                row -> {
                  ArrivalLineView result = ArrivalLineView.from(row);
                  PurchaseOrderLine orderLine = source.get(row.getOrderLineId());
                  if (orderLine != null) {
                    result.setOrderQuantity(orderLine.getQuantity());
                    result.setPendingArrivalQuantity(orderLine.getPendingArrivalQuantity());
                    result.setArrivedQuantity(orderLine.getArrivedQuantity());
                    result.setAvailableQuantity(available(orderLine));
                  }
                  return result;
                })
            .toList());
    return view;
  }

  /** 使用当前有效账号和部门作为首次创建时的经办快照。 */
  public ArrivalDefaults defaults() {
    OrderDefaults user = orders.defaults(SecurityUtils.getUserId(), SecurityUtils.getDeptId());
    if (user == null) throw new ServiceException("当前账号或部门无效，不能创建到货单");
    ArrivalDefaults result = new ArrivalDefaults();
    result.setReceiverId(user.getBuyerId());
    result.setReceiverName(user.getBuyerName());
    result.setDeptId(user.getDeptId());
    result.setDeptName(user.getDeptName());
    return result;
  }

  /** 已审核订单行参照，关键词和排序均由服务端约束。 */
  public PageResult<OrderLineReference> references(OrderLineReferenceQuery query) {
    validate(query);
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize()).setReasonable(false);
      List<OrderLineReference> found =
          references.search(query, query.getKeyword() == null ? null : escape(query.getKeyword()));
      long total = new PageInfo<>(found).getTotal();
      return new PageResult<>(found, total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 批量复核来源，不接受重复、空或超过一百项的 ID。 */
  public List<OrderLineReference> resolve(List<Long> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > ArrivalRules.MAX_REFERENCE_IDS
        || ids.stream().anyMatch(id -> id == null || id <= 0)
        || new HashSet<>(ids).size() != ids.size())
      throw new ServiceException("请指定 1～100 个不重复的有效订单行 ID");
    Map<Long, OrderLineReference> found =
        references.resolve(ids).stream()
            .collect(Collectors.toMap(OrderLineReference::getOrderLineId, Function.identity()));
    return ids.stream().map(found::get).filter(Objects::nonNull).toList();
  }

  /** 新增草稿不占订单额度，来源快照、正式单号及明细原子保存。 */
  @Transactional(rollbackFor = Exception.class)
  public ArrivalView create(SaveArrivalRequest request) {
    validate(request);
    if (request.getArrivalId() != null
        || request.getVersion() != null
        || request.getLines().stream().anyMatch(l -> l.getLineId() != null))
      throw new ServiceException("新增到货单不能指定 ID、行 ID 或版本号");
    lockTree();
    PurchaseArrival arrival = new PurchaseArrival();
    ArrivalDefaults defaults = defaults();
    arrival.setReceiverId(defaults.getReceiverId());
    arrival.setReceiverName(defaults.getReceiverName());
    arrival.setDeptId(defaults.getDeptId());
    arrival.setDeptName(defaults.getDeptName());
    arrival.setStatus(ArrivalStatus.DRAFT);
    arrival.setVersion(0);
    arrival.setReviewRemark("");
    arrival.setVoidReason("");
    arrival.setArrivalNo("PENDING-" + UUID.randomUUID());
    Sources source =
        lockSources(request.getLines().stream().map(ArrivalLineInput::getOrderLineId).toList());
    copyHeader(request, arrival, source);
    validateInput(
        request.getArrivalDate(), request.getSupplierId(), request.getLines(), source, true);
    arrivals.insert(arrival);
    arrival.setArrivalNo(
        ArrivalRules.NUMBER_PREFIX
            + LocalDate.now(OrderRules.TIME_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE)
            + String.format(Locale.ROOT, "%010d", arrival.getArrivalId()));
    if (arrivals.updateById(arrival) != 1) throw stale();
    saveLines(arrival, request.getLines(), source, List.of());
    return detail(arrival.getArrivalId());
  }

  /** 仅当前版本草稿可编辑，保留经办人及稳定行 ID。 */
  @Transactional(rollbackFor = Exception.class)
  public ArrivalView update(SaveArrivalRequest request) {
    validate(request);
    lockTree();
    PurchaseArrival arrival = lock(request.getArrivalId(), request.getVersion());
    requireStatus(arrival, ArrivalStatus.DRAFT);
    Sources source =
        lockSources(request.getLines().stream().map(ArrivalLineInput::getOrderLineId).toList());
    copyHeader(request, arrival, source);
    validateInput(
        request.getArrivalDate(), request.getSupplierId(), request.getLines(), source, true);
    saveLines(arrival, request.getLines(), source, existingLines(arrival.getArrivalId()));
    updateVersion(arrival);
    return detail(arrival.getArrivalId());
  }

  /** 草稿逻辑删除携带实体以填充删除审计，正式单据只能受控作废。 */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    lockTree();
    PurchaseArrival arrival = lock(id, version);
    requireStatus(arrival, ArrivalStatus.DRAFT);
    for (PurchaseArrivalLine line : existingLines(id))
      if (lines.deleteById(line) != 1) throw stale();
    if (arrivals.deleteById(arrival) != 1) throw stale();
  }

  /** 唯一生效入口：校验状态与版本，反写来源，再切换状态，任何异常全部回滚。 */
  @Transactional(rollbackFor = Exception.class)
  public ArrivalView transition(Long id, ArrivalAction action, ArrivalActionRequest request) {
    validate(request);
    if (action == null) throw new ServiceException("状态操作不能为空");
    lockTree();
    PurchaseArrival arrival = lock(id, request.getVersion());
    requireStatus(arrival, action.getSource());
    if ((action == ArrivalAction.REJECT || action == ArrivalAction.VOID)
        && request.getReason().isBlank()) throw new ServiceException("驳回或作废必须填写原因");
    List<PurchaseArrivalLine> saved = existingLines(id);
    Sources source = lockSources(saved.stream().map(PurchaseArrivalLine::getOrderLineId).toList());
    if (action == ArrivalAction.SUBMIT || action == ArrivalAction.APPROVE) {
      validateWarehouse(arrival.getWarehouseId());
      validateMasterData(arrival.getSupplierId(), source);
      validateInput(
          arrival.getArrivalDate(),
          arrival.getSupplierId(),
          saved.stream().map(this::toInput).toList(),
          source,
          action == ArrivalAction.SUBMIT);
    }
    if (action == ArrivalAction.VOID && masterReferences.arrivalReceipts(id) > 0)
      throw new ServiceException("存在未作废的采购入库单，不能作废到货单");
    postQuantities(saved, source, action);
    String actor = SecurityUtils.getUsername();
    LocalDateTime time = LocalDateTime.now(OrderRules.TIME_ZONE);
    switch (action) {
      case SUBMIT -> {
        arrival.setSubmittedBy(actor);
        arrival.setSubmittedTime(time);
        arrival.setReviewedBy(null);
        arrival.setReviewedTime(null);
        arrival.setReviewRemark("");
      }
      case WITHDRAW -> {
        arrival.setSubmittedBy(null);
        arrival.setSubmittedTime(null);
      }
      case APPROVE, REJECT -> {
        arrival.setReviewedBy(actor);
        arrival.setReviewedTime(time);
        arrival.setReviewRemark(request.getReason());
      }
      case VOID -> {
        arrival.setVoidedBy(actor);
        arrival.setVoidedTime(time);
        arrival.setVoidReason(request.getReason());
      }
    }
    arrival.setStatus(action.getTarget());
    updateVersion(arrival);
    return detail(id);
  }

  /** 唯一数量反写路径，每次命令按来源订单递增一次版本，不接受客户端累计数。 */
  private void postQuantities(
      List<PurchaseArrivalLine> saved, Sources source, ArrivalAction action) {
    for (PurchaseArrivalLine row : saved) {
      PurchaseOrderLine target = source.lines.get(row.getOrderLineId());
      BigDecimal pending = target.getPendingArrivalQuantity(),
          arrived = target.getArrivedQuantity(),
          quantity = row.getQuantity();
      switch (action) {
        case SUBMIT -> pending = pending.add(quantity);
        case WITHDRAW, REJECT -> pending = pending.subtract(quantity);
        case APPROVE -> {
          pending = pending.subtract(quantity);
          arrived = arrived.add(quantity);
        }
        case VOID -> arrived = arrived.subtract(quantity);
      }
      if (pending.signum() < 0
          || arrived.signum() < 0
          || pending.add(arrived).compareTo(target.getQuantity()) > 0)
        throw new ServiceException("订单到货额度已变更或统计异常，请刷新后重试");
      target.setPendingArrivalQuantity(pending);
      target.setArrivedQuantity(arrived);
      if (orderLines.updateById(target) != 1) throw stale();
    }
    for (PurchaseOrder order : source.orders.values()) {
      int previous = order.getVersion();
      if (previous == Integer.MAX_VALUE) throw new ServiceException("来源订单版本已达上限");
      order.setVersion(previous + 1);
      if (orders.update(
              order,
              Wrappers.<PurchaseOrder>lambdaUpdate()
                  .eq(PurchaseOrder::getOrderId, order.getOrderId())
                  .eq(PurchaseOrder::getVersion, previous))
          != 1) throw stale();
    }
  }

  /** 校验来源审批、同供应商、日期及各来源精度，提交时重新核验可用额度。 */
  private void validateInput(
      LocalDate date,
      Long supplierId,
      List<ArrivalLineInput> input,
      Sources source,
      boolean checkAvailable) {
    for (ArrivalLineInput row : input) {
      PurchaseOrderLine line = source.lines.get(row.getOrderLineId());
      PurchaseOrder order = source.orders.get(line.getOrderId());
      if (order.getStatus() != OrderStatus.APPROVED)
        throw new ServiceException("来源订单未审核或已关闭，不能办理到货");
      if (!supplierId.equals(order.getSupplierId()))
        throw new ServiceException("一张到货单只能参照同一供应商的订单");
      if (date.isBefore(order.getOrderDate())) throw new ServiceException("到货日期不能早于来源订单日期");
      if (row.getQuantity().stripTrailingZeros().scale() > order.getQuantityDecimalPlaces())
        throw new ServiceException("到货数量超出来源订单允许的小数位：" + order.getQuantityDecimalPlaces());
      if (checkAvailable && row.getQuantity().compareTo(available(line)) > 0)
        throw new ServiceException(
            "订单 " + order.getOrderNo() + " 第 " + line.getLineNo() + " 行可新增到货数量不足");
    }
  }

  /** 按 ID 顺序锁来源主表及明细，关闭订单与到货操作共用分类树事务锁。 */
  private Sources lockSources(List<Long> ids) {
    if (ids.isEmpty() || new HashSet<>(ids).size() != ids.size())
      throw new ServiceException("来源订单行不能为空或重复");
    List<PurchaseOrderLine> initial = orderLines.selectByIds(ids);
    if (initial.size() != ids.size()) throw new ServiceException("来源订单行不存在或已删除");
    List<Long> orderIds =
        initial.stream().map(PurchaseOrderLine::getOrderId).distinct().sorted().toList();
    Map<Long, PurchaseOrder> headers = new LinkedHashMap<>();
    for (Long id : orderIds) {
      PurchaseOrder order =
          orders.selectOne(
              Wrappers.<PurchaseOrder>lambdaQuery()
                  .eq(PurchaseOrder::getOrderId, id)
                  .last("FOR UPDATE"));
      if (order == null) throw new ServiceException("来源订单不存在或已删除");
      headers.put(id, order);
    }
    Map<Long, PurchaseOrderLine> selected =
        orderLines
            .selectList(
                Wrappers.<PurchaseOrderLine>lambdaQuery()
                    .in(PurchaseOrderLine::getLineId, ids)
                    .orderByAsc(PurchaseOrderLine::getLineId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(PurchaseOrderLine::getLineId, Function.identity()));
    if (selected.size() != ids.size()) throw new ServiceException("来源订单明细已变更，请重新选择");
    return new Sources(headers, selected);
  }

  /** 持锁检查当前供应商、单位、料品及分类，退回和作废不受档案停用阻碍。 */
  private Supplier validateMasterData(Long supplierId, Sources source) {
    Supplier supplier =
        suppliers.selectOne(
            Wrappers.<Supplier>lambdaQuery()
                .eq(Supplier::getSupplierId, supplierId)
                .last("FOR UPDATE"));
    if (supplier == null || supplier.getStatus() != EnableStatus.ENABLED)
      throw new ServiceException("供应商无效或已停用");
    List<Long> unitIds =
        source.lines.values().stream()
            .map(PurchaseOrderLine::getUnitId)
            .distinct()
            .sorted()
            .toList();
    Map<Long, Unit> selectedUnits =
        units
            .selectList(
                Wrappers.<Unit>lambdaQuery()
                    .in(Unit::getUnitId, unitIds)
                    .orderByAsc(Unit::getUnitId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
    List<Long> itemIds =
        source.lines.values().stream()
            .map(PurchaseOrderLine::getItemId)
            .distinct()
            .sorted()
            .toList();
    Map<Long, Item> selectedItems =
        items
            .selectList(
                Wrappers.<Item>lambdaQuery()
                    .in(Item::getItemId, itemIds)
                    .orderByAsc(Item::getItemId)
                    .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Item::getItemId, Function.identity()));
    List<Long> categoryIds =
        selectedItems.values().stream().map(Item::getCategoryId).distinct().toList();
    Map<Long, ItemCategory> selectedCategories =
        categoryIds.isEmpty()
            ? Map.of()
            : categories.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(ItemCategory::getCategoryId, Function.identity()));
    for (PurchaseOrderLine line : source.lines.values()) {
      Unit unit = selectedUnits.get(line.getUnitId());
      Item item = selectedItems.get(line.getItemId());
      if (unit == null || unit.getStatus() != EnableStatus.ENABLED)
        throw new ServiceException("来源单位无效或已停用");
      if (item == null
          || item.getStatus() != EnableStatus.ENABLED
          || !Boolean.TRUE.equals(item.getPurchaseEnabled()))
        throw new ServiceException("料品无效、停用或不允许采购");
      ItemCategory category = selectedCategories.get(item.getCategoryId());
      if (category == null || category.getStatus() != CategoryStatus.ENABLED)
        throw new ServiceException("料品分类无效或已停用");
      if (!line.getUnitId().equals(item.getBaseUnitId()))
        throw new ServiceException("来源单位与料品库存单位不一致，暂不支持单位换算");
    }
    return supplier;
  }

  /** 预计仓库可不指定；指定时与仓库维护共用行锁，校验当前有效状态。 */
  private Warehouse validateWarehouse(Long id) {
    if (id == null) return null;
    Warehouse warehouse =
        warehouses.selectOne(
            Wrappers.<Warehouse>lambdaQuery().eq(Warehouse::getWarehouseId, id).last("FOR UPDATE"));
    if (warehouse == null || warehouse.getStatus() != EnableStatus.ENABLED)
      throw new ServiceException("预计入库仓库无效或已停用");
    return warehouse;
  }

  /** 复制维护字段及当前供应商、仓库快照，来源交易快照由订单行提供。 */
  private void copyHeader(SaveArrivalRequest request, PurchaseArrival target, Sources source) {
    Warehouse warehouse = validateWarehouse(request.getWarehouseId());
    Supplier supplier = validateMasterData(request.getSupplierId(), source);
    target.setArrivalDate(request.getArrivalDate());
    target.setSupplierId(supplier.getSupplierId());
    target.setSupplierCode(supplier.getSupplierCode());
    target.setSupplierName(supplier.getSupplierName());
    target.setWarehouseId(warehouse == null ? null : warehouse.getWarehouseId());
    target.setWarehouseCode(warehouse == null ? "" : warehouse.getWarehouseCode());
    target.setWarehouseName(warehouse == null ? "" : warehouse.getWarehouseName());
    target.setSupplierDeliveryNo(request.getSupplierDeliveryNo());
    target.setRemark(request.getRemark());
    target.setLineCount(request.getLines().size());
  }

  /** 原位更新已有行并逻辑删除移除行，禁止跨单、重复 ID 或复活已删除行。 */
  private void saveLines(
      PurchaseArrival arrival,
      List<ArrivalLineInput> input,
      Sources source,
      List<PurchaseArrivalLine> previous) {
    Map<Long, PurchaseArrivalLine> existing =
        previous.stream()
            .collect(Collectors.toMap(PurchaseArrivalLine::getLineId, Function.identity()));
    Set<Long> retained = new HashSet<>();
    for (int i = 0; i < input.size(); i++) {
      ArrivalLineInput request = input.get(i);
      PurchaseArrivalLine row =
          request.getLineId() == null
              ? new PurchaseArrivalLine()
              : existing.get(request.getLineId());
      if (row == null || (request.getLineId() != null && !retained.add(request.getLineId())))
        throw new ServiceException("到货行已失效、重复或不属于当前单据");
      PurchaseOrderLine line = source.lines.get(request.getOrderLineId());
      PurchaseOrder order = source.orders.get(line.getOrderId());
      row.setArrivalId(arrival.getArrivalId());
      row.setLineNo(i + 1);
      row.setOrderId(order.getOrderId());
      row.setOrderLineId(line.getLineId());
      row.setOrderNo(order.getOrderNo());
      row.setOrderLineNo(line.getLineNo());
      row.setItemId(line.getItemId());
      row.setItemCode(line.getItemCode());
      row.setItemName(line.getItemName());
      row.setSpecification(line.getSpecification());
      row.setUnitId(line.getUnitId());
      row.setUnitName(line.getUnitName());
      row.setQuantityDecimalPlaces(order.getQuantityDecimalPlaces());
      row.setQuantity(request.getQuantity());
      row.setRemark(request.getRemark());
      if (row.getLineId() == null) lines.insert(row);
      else if (lines.updateById(row) != 1) throw stale();
    }
    for (PurchaseArrivalLine old : previous)
      if (!retained.contains(old.getLineId()) && lines.deleteById(old) != 1) throw stale();
  }

  /** 将保存快照转为统一校验输入。 */
  private ArrivalLineInput toInput(PurchaseArrivalLine line) {
    ArrivalLineInput result = new ArrivalLineInput();
    result.setLineId(line.getLineId());
    result.setOrderLineId(line.getOrderLineId());
    result.setQuantity(line.getQuantity());
    result.setRemark(line.getRemark());
    return result;
  }

  /** 当前可用数量不包含已提交的占用。 */
  private BigDecimal available(PurchaseOrderLine line) {
    return line.getQuantity()
        .subtract(line.getPendingArrivalQuantity())
        .subtract(line.getArrivedQuantity());
  }

  /** 按每个来源行判断进度，不合计不同单位的数量。 */
  private Map<Long, ReceiptProgress> receiptProgress(List<Long> ids) {
    var result = new HashMap<Long, ReceiptProgress>();
    if (ids.isEmpty()) return result;
    var grouped =
        lines
            .selectList(
                Wrappers.<PurchaseArrivalLine>lambdaQuery()
                    .in(PurchaseArrivalLine::getArrivalId, ids))
            .stream()
            .collect(Collectors.groupingBy(PurchaseArrivalLine::getArrivalId));
    grouped.forEach(
        (id, rows) ->
            result.put(
                id,
                rows.stream().allMatch(r -> r.getReceivedQuantity().compareTo(r.getQuantity()) == 0)
                    ? ReceiptProgress.FULLY_RECEIVED
                    : rows.stream().anyMatch(r -> r.getReceivedQuantity().signum() > 0)
                        ? ReceiptProgress.PARTIALLY_RECEIVED
                        : ReceiptProgress.NOT_RECEIVED));
    return result;
  }

  /** 与订单及基础档案写操作共用跨实例事务锁。 */
  private void lockTree() {
    if (categories.lockTree() == null) throw new ServiceException("分类事务锁尚未初始化");
  }

  /** 返回未删除的到货单。 */
  private PurchaseArrival require(Long id) {
    if (id == null || id <= 0) throw new ServiceException("到货单 ID 必须为正数");
    PurchaseArrival result = arrivals.selectById(id);
    if (result == null) throw new ServiceException("到货单不存在或已删除");
    return result;
  }

  /** 行锁下检查整单版本，避免重复命令产生重复反写。 */
  private PurchaseArrival lock(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0)
      throw new ServiceException("必须指定有效到货单 ID 和版本号");
    PurchaseArrival result =
        arrivals.selectOne(
            Wrappers.<PurchaseArrival>lambdaQuery()
                .eq(PurchaseArrival::getArrivalId, id)
                .last("FOR UPDATE"));
    if (result == null) throw new ServiceException("到货单不存在或已删除");
    if (!version.equals(result.getVersion())) throw stale();
    return result;
  }

  /** 以稳定行号加载整单有效明细。 */
  private List<PurchaseArrivalLine> existingLines(Long id) {
    return lines.selectList(
        Wrappers.<PurchaseArrivalLine>lambdaQuery()
            .eq(PurchaseArrivalLine::getArrivalId, id)
            .orderByAsc(PurchaseArrivalLine::getLineNo, PurchaseArrivalLine::getLineId));
  }

  /** 所有整单变更递增一次版本并触发修改审计。 */
  private void updateVersion(PurchaseArrival arrival) {
    int previous = arrival.getVersion();
    if (previous == Integer.MAX_VALUE) throw new ServiceException("到货单版本已达上限");
    arrival.setVersion(previous + 1);
    if (arrivals.update(
            arrival,
            Wrappers.<PurchaseArrival>lambdaUpdate()
                .eq(PurchaseArrival::getArrivalId, arrival.getArrivalId())
                .eq(PurchaseArrival::getVersion, previous))
        != 1) throw stale();
  }

  /** 明确拒绝非法状态转换。 */
  private void requireStatus(PurchaseArrival arrival, ArrivalStatus expected) {
    if (arrival.getStatus() != expected) throw new ServiceException("当前到货单状态不允许此操作，请刷新后重试");
  }

  /** 内部调用与 HTTP 绑定均遵循同一输入规则。 */
  private void validate(Object input) {
    if (input == null) throw new ServiceException("请求不能为空");
    var errors = validator.validate(input);
    if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
  }

  /** 普通文本查询转义 SQL 通配符。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 返回统一并发冲突信息。 */
  private ServiceException stale() {
    return new ServiceException("到货单或来源订单已变更，请刷新后重试");
  }

  /** 当前事务持锁后的来源主表和明细集合。 */
  private static final class Sources {
    private final Map<Long, PurchaseOrder> orders;
    private final Map<Long, PurchaseOrderLine> lines;

    /** 保存当前读结果，仅在整单事务内使用。 */
    private Sources(Map<Long, PurchaseOrder> orders, Map<Long, PurchaseOrderLine> lines) {
      this.orders = orders;
      this.lines = lines;
    }
  }
}
