package com.ruoyi.business.purchase.receipt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.inventory.service.InventoryPostingService;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.business.masterdata.category.mapper.ItemCategoryMapper;
import com.ruoyi.business.masterdata.item.domain.Item;
import com.ruoyi.business.masterdata.item.mapper.ItemMapper;
import com.ruoyi.business.masterdata.location.domain.Location;
import com.ruoyi.business.masterdata.location.mapper.LocationMapper;
import com.ruoyi.business.masterdata.supplier.domain.Supplier;
import com.ruoyi.business.masterdata.supplier.mapper.SupplierMapper;
import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.business.masterdata.unit.mapper.UnitMapper;
import com.ruoyi.business.masterdata.warehouse.domain.Warehouse;
import com.ruoyi.business.masterdata.warehouse.mapper.WarehouseMapper;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.business.purchase.arrival.mapper.*;
import com.ruoyi.business.purchase.order.domain.OrderRules;
import com.ruoyi.business.purchase.order.mapper.OrderMapper;
import com.ruoyi.business.purchase.order.vo.OrderDefaults;
import com.ruoyi.business.purchase.receipt.domain.*;
import com.ruoyi.business.purchase.receipt.dto.*;
import com.ruoyi.business.purchase.receipt.mapper.*;
import com.ruoyi.business.purchase.receipt.vo.*;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 入库整单、来源执行量和库存记账的原子事务入口。 */
@Service
public class ReceiptService {
  private final ReceiptMapper receipts;
  private final ReceiptLineMapper lines;
  private final ArrivalMapper arrivals;
  private final ArrivalLineMapper arrivalLines;
  private final ArrivalLineReferenceMapper references;
  private final ItemCategoryMapper categories;
  private final WarehouseMapper warehouses;
  private final LocationMapper locations;
  private final SupplierMapper suppliers;
  private final UnitMapper units;
  private final ItemMapper items;
  private final OrderMapper orders;
  private final InventoryPostingService inventory;
  private final Validator validator;

  /** 注入持久化、公共校验和库存事务服务，不依赖档案维护服务。 */
  public ReceiptService(
      ReceiptMapper receipts,
      ReceiptLineMapper lines,
      ArrivalMapper arrivals,
      ArrivalLineMapper arrivalLines,
      ArrivalLineReferenceMapper references,
      ItemCategoryMapper categories,
      WarehouseMapper warehouses,
      LocationMapper locations,
      SupplierMapper suppliers,
      UnitMapper units,
      ItemMapper items,
      OrderMapper orders,
      InventoryPostingService inventory,
      Validator validator) {
    this.receipts = receipts;
    this.lines = lines;
    this.arrivals = arrivals;
    this.arrivalLines = arrivalLines;
    this.references = references;
    this.categories = categories;
    this.warehouses = warehouses;
    this.locations = locations;
    this.suppliers = suppliers;
    this.units = units;
    this.items = items;
    this.orders = orders;
    this.inventory = inventory;
    this.validator = validator;
  }

  /** 服务端分页、静态排序字段及绑定查询参数，异常时也清理分页上下文。 */
  public PageResult<ReceiptView> list(ReceiptQuery q) {
    validate(q);
    if (q.getBeginDate() != null
        && q.getEndDate() != null
        && q.getBeginDate().isAfter(q.getEndDate())) {
      throw new ServiceException("开始日期不能晚于结束日期");
    }
    LambdaQueryWrapper<PurchaseReceipt> w = Wrappers.<PurchaseReceipt>lambdaQuery();
    if (q.getReceiptNo() != null) {
      w.like(PurchaseReceipt::getReceiptNo, escape(q.getReceiptNo()));
    }
    if (q.getSupplierId() != null) {
      w.eq(PurchaseReceipt::getSupplierId, q.getSupplierId());
    }
    if (q.getWarehouseId() != null) {
      w.apply(
          "EXISTS (SELECT 1 FROM purchase_receipt_line l WHERE l.receipt_id=purchase_receipt.receipt_id AND l.del_flag='0' AND l.warehouse_id={0})",
          q.getWarehouseId());
    }
    if (q.getStatus() != null) {
      w.eq(PurchaseReceipt::getStatus, ReceiptStatus.fromCode(q.getStatus()));
    }
    if (q.getBeginDate() != null) {
      w.ge(PurchaseReceipt::getReceiptDate, q.getBeginDate());
    }
    if (q.getEndDate() != null) {
      w.le(PurchaseReceipt::getReceiptDate, q.getEndDate());
    }
    if (q.getArrivalId() != null) {
      w.apply(
          "EXISTS (SELECT 1 FROM purchase_receipt_line l WHERE l.receipt_id=purchase_receipt.receipt_id AND l.del_flag='0' AND l.arrival_id={0})",
          q.getArrivalId());
    }
    if (q.getOrderId() != null) {
      w.apply(
          "EXISTS (SELECT 1 FROM purchase_receipt_line l WHERE l.receipt_id=purchase_receipt.receipt_id AND l.del_flag='0' AND l.order_id={0})",
          q.getOrderId());
    }
    boolean asc = "asc".equals(q.getSortDirection());
    switch (q.getSortField() == null ? "receiptDate" : q.getSortField()) {
      case "receiptNo" -> w.orderBy(true, asc, PurchaseReceipt::getReceiptNo);
      case "receiptDate" -> w.orderBy(true, asc, PurchaseReceipt::getReceiptDate);
      case "updateTime" -> w.orderBy(true, asc, PurchaseReceipt::getUpdateTime);
      default -> throw new ServiceException("不支持的排序字段");
    }
    w.orderByDesc(PurchaseReceipt::getReceiptId);
    try {
      PageHelper.startPage(q.getPageNum(), q.getPageSize()).setReasonable(false);
      List<PurchaseReceipt> found = receipts.selectList(w);
      long total = new PageInfo<>(found).getTotal();
      PageHelper.clearPage();
      List<ReceiptView> result = found.stream().map(ReceiptView::from).toList();
      if (!found.isEmpty()) {
        Map<Long, List<PurchaseReceiptLine>> byReceipt =
            lines
                .selectList(
                    Wrappers.<PurchaseReceiptLine>lambdaQuery()
                        .in(
                            PurchaseReceiptLine::getReceiptId,
                            found.stream().map(PurchaseReceipt::getReceiptId).toList())
                        .orderByAsc(
                            PurchaseReceiptLine::getReceiptId,
                            PurchaseReceiptLine::getLineNo,
                            PurchaseReceiptLine::getLineId))
                .stream()
                .collect(Collectors.groupingBy(PurchaseReceiptLine::getReceiptId));
        for (ReceiptView view : result) {
          view.setWarehouseNames(
              warehouseNames(byReceipt.getOrDefault(view.getReceiptId(), List.of())));
        }
      }
      return new PageResult<>(result, total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 返回保存快照，批量补充来源当前执行量。 */
  @Transactional(readOnly = true)
  public ReceiptView detail(Long id) {
    if (id == null || id <= 0) {
      throw new ServiceException("入库单 ID 必须为正数");
    }
    PurchaseReceipt header = receipts.selectById(id);
    if (header == null) {
      throw new ServiceException("入库单不存在或已删除");
    }
    ReceiptView view = ReceiptView.from(header);
    List<PurchaseReceiptLine> saved = existingLines(id);
    view.setWarehouseNames(warehouseNames(saved));
    Map<Long, PurchaseArrivalLine> sources =
        saved.isEmpty()
            ? Map.<Long, PurchaseArrivalLine>of()
            : arrivalLines
                .selectByIds(
                    saved.stream().map(PurchaseReceiptLine::getArrivalLineId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(PurchaseArrivalLine::getLineId, Function.identity()));
    view.setLines(
        saved.stream()
            .map(
                row -> {
                  ReceiptLineView result = ReceiptLineView.from(row);
                  PurchaseArrivalLine source = sources.get(row.getArrivalLineId());
                  if (source != null) {
                    result.setArrivalQuantity(source.getQuantity());
                    result.setPendingReceiptQuantity(source.getPendingReceiptQuantity());
                    result.setReceivedQuantity(source.getReceivedQuantity());
                    result.setAvailableQuantity(available(source));
                  }
                  return result;
                })
            .toList());
    return view;
  }

  /** 当前有效账号及部门作为新单经办快照。 */
  public ReceiptDefaults defaults() {
    OrderDefaults user = orders.defaults(SecurityUtils.getUserId(), SecurityUtils.getDeptId());
    if (user == null) {
      throw new ServiceException("当前账号或部门无效");
    }
    ReceiptDefaults d = new ReceiptDefaults();
    d.setReceiverId(user.getBuyerId());
    d.setReceiverName(user.getBuyerName());
    d.setDeptId(user.getDeptId());
    d.setDeptName(user.getDeptName());
    return d;
  }

  /** 来源参照权限独立，查询不依赖订单或到货维护权限。 */
  public PageResult<ArrivalLineReference> references(ArrivalLineReferenceQuery q) {
    validate(q);
    try {
      PageHelper.startPage(q.getPageNum(), q.getPageSize()).setReasonable(false);
      List<ArrivalLineReference> found =
          references.search(q, q.getKeyword() == null ? null : escape(q.getKeyword()));
      long total = new PageInfo<>(found).getTotal();
      return new PageResult<>(found, total);
    } finally {
      PageHelper.clearPage();
    }
  }

  /** 批量复核可选来源，保留输入顺序，失效来源不返回。 */
  public List<ArrivalLineReference> resolve(List<Long> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > ReceiptRules.MAX_REFERENCE_IDS
        || ids.stream().anyMatch(i -> i == null || i <= 0)
        || new HashSet<>(ids).size() != ids.size()) {
      throw new ServiceException("请指定 1～100 个不重复的有效到货行 ID");
    }
    Map<Long, ArrivalLineReference> found =
        references.resolve(ids).stream()
            .collect(Collectors.toMap(ArrivalLineReference::getArrivalLineId, Function.identity()));
    return ids.stream().map(found::get).filter(Objects::nonNull).toList();
  }

  /** 新增草稿校验余量但不占用，所有快照仅取自持锁来源。 */
  @Transactional(rollbackFor = Exception.class)
  public ReceiptView create(SaveReceiptRequest request) {
    validate(request);
    if (request.getReceiptId() != null
        || request.getVersion() != null
        || request.getLines().stream().anyMatch(r -> r.getLineId() != null)) {
      throw new ServiceException("新增不能指定单据 ID、行 ID 或版本号");
    }
    lockTree();
    Sources sources = lockSources(request.getLines());
    PurchaseReceipt header = new PurchaseReceipt();
    ReceiptDefaults d = defaults();
    header.setReceiverId(d.getReceiverId());
    header.setReceiverName(d.getReceiverName());
    header.setDeptId(d.getDeptId());
    header.setDeptName(d.getDeptName());
    header.setStatus(ReceiptStatus.DRAFT);
    header.setVersion(0);
    header.setReviewRemark("");
    header.setVoidReason("");
    header.setReceiptNo("PENDING-" + UUID.randomUUID());
    Map<Long, Location> places =
        validateInput(
            request.getReceiptDate(), request.getSupplierId(), request.getLines(), sources, true);
    copyHeader(request, header, sources);
    receipts.insert(header);
    header.setReceiptNo(
        ReceiptRules.NUMBER_PREFIX
            + LocalDate.now(OrderRules.TIME_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE)
            + String.format(Locale.ROOT, "%010d", header.getReceiptId()));
    if (receipts.updateById(header) != 1) {
      throw stale();
    }
    saveLines(header, request.getLines(), sources, places, List.of());
    return detail(header.getReceiptId());
  }

  /** 按当前版本编辑草稿，原位维护稳定明细 ID。 */
  @Transactional(rollbackFor = Exception.class)
  public ReceiptView update(SaveReceiptRequest request) {
    validate(request);
    lockTree();
    PurchaseReceipt header = lock(request.getReceiptId(), request.getVersion());
    requireStatus(header, ReceiptStatus.DRAFT);
    Sources source = lockSources(request.getLines());
    Map<Long, Location> places =
        validateInput(
            request.getReceiptDate(), request.getSupplierId(), request.getLines(), source, true);
    copyHeader(request, header, source);
    saveLines(header, request.getLines(), source, places, existingLines(header.getReceiptId()));
    updateVersion(header);
    return detail(header.getReceiptId());
  }

  /** 草稿删除不产生库存流水，实体逻辑删除触发审计。 */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Integer version) {
    lockTree();
    PurchaseReceipt header = lock(id, version);
    requireStatus(header, ReceiptStatus.DRAFT);
    for (PurchaseReceiptLine line : existingLines(id)) {
      if (lines.deleteById(line) != 1) {
        throw stale();
      }
    }
    if (receipts.deleteById(header) != 1) {
      throw stale();
    }
  }

  /** 唯一状态入口，来源数量、库存流水和版本在同一事务内成功或回滚。 */
  @Transactional(rollbackFor = Exception.class)
  public ReceiptView transition(Long id, ReceiptAction action, ReceiptActionRequest request) {
    validate(request);
    if (action == null) {
      throw new ServiceException("状态操作不能为空");
    }
    lockTree();
    PurchaseReceipt header = lock(id, request.getVersion());
    requireStatus(header, action.getSource());
    if ((action == ReceiptAction.REJECT || action == ReceiptAction.VOID)
        && request.getReason().isBlank()) {
      throw new ServiceException("驳回或作废必须填写原因");
    }
    List<PurchaseReceiptLine> saved = existingLines(id);
    List<ReceiptLineInput> input = saved.stream().map(this::toInput).toList();
    Sources source = lockSources(input);
    if (action == ReceiptAction.SUBMIT || action == ReceiptAction.APPROVE) {
      validateInput(
          header.getReceiptDate(),
          header.getSupplierId(),
          input,
          source,
          action == ReceiptAction.SUBMIT);
    } else {
      lockWarehouses(input);
    }
    postQuantities(input, source, action);
    if (action == ReceiptAction.APPROVE) {
      inventory.receive(header, saved);
    }
    if (action == ReceiptAction.VOID) {
      inventory.reverse(header, saved, request.getReason());
    }
    String actor = SecurityUtils.getUsername();
    LocalDateTime time = LocalDateTime.now(OrderRules.TIME_ZONE);
    switch (action) {
      case SUBMIT -> {
        header.setSubmittedBy(actor);
        header.setSubmittedTime(time);
        header.setReviewedBy(null);
        header.setReviewedTime(null);
        header.setReviewRemark("");
      }
      case WITHDRAW -> {
        header.setSubmittedBy(null);
        header.setSubmittedTime(null);
      }
      case APPROVE, REJECT -> {
        header.setReviewedBy(actor);
        header.setReviewedTime(time);
        header.setReviewRemark(request.getReason());
      }
      case VOID -> {
        header.setVoidedBy(actor);
        header.setVoidedTime(time);
        header.setVoidReason(request.getReason());
      }
    }
    header.setStatus(action.getTarget());
    updateVersion(header);
    return detail(id);
  }

  /** 先取得公共事务锁，与分类、档案及到货作废协调。 */
  private void lockTree() {
    if (categories.lockTree() == null) {
      throw new ServiceException("缺少分类事务锁");
    }
  }

  /** 来源按 ID 加锁，允许同一来源被不同货位重复引用。 */
  private Sources lockSources(List<ReceiptLineInput> input) {
    List<Long> ids =
        input.stream().map(ReceiptLineInput::getArrivalLineId).distinct().sorted().toList();
    List<PurchaseArrivalLine> initial = arrivalLines.selectByIds(ids);
    if (initial.size() != ids.size()) {
      throw new ServiceException("来源到货明细不存在或已删除");
    }
    LinkedHashMap<Long, PurchaseArrival> headers = new LinkedHashMap<Long, PurchaseArrival>();
    for (Long id :
        initial.stream().map(PurchaseArrivalLine::getArrivalId).distinct().sorted().toList()) {
      PurchaseArrival h =
          arrivals.selectOne(
              Wrappers.<PurchaseArrival>lambdaQuery()
                  .eq(PurchaseArrival::getArrivalId, id)
                  .last("FOR UPDATE"));
      if (h == null) {
        throw new ServiceException("来源到货单不存在或已删除");
      }
      headers.put(id, h);
    }
    List<PurchaseArrivalLine> selected =
        arrivalLines.selectList(
            Wrappers.<PurchaseArrivalLine>lambdaQuery()
                .in(PurchaseArrivalLine::getLineId, ids)
                .orderByAsc(PurchaseArrivalLine::getLineId)
                .last("FOR UPDATE"));
    if (selected.size() != ids.size()) {
      throw new ServiceException("来源到货明细已失效");
    }
    return new Sources(
        headers,
        selected.stream()
            .collect(Collectors.toMap(PurchaseArrivalLine::getLineId, Function.identity())));
  }

  /** 校验来源分组、日期、精度及启用档案，按来源汇总校验可用量。 */
  private Map<Long, Location> validateInput(
      LocalDate date,
      Long supplierId,
      List<ReceiptLineInput> input,
      Sources source,
      boolean checkAvailable) {
    Map<Long, Warehouse> selectedWarehouses = lockWarehouses(input);
    if (selectedWarehouses.values().stream().anyMatch(w -> w.getStatus() != EnableStatus.ENABLED)) {
      throw new ServiceException("入库仓库已停用");
    }
    Supplier supplier =
        suppliers.selectOne(
            Wrappers.<com.ruoyi.business.masterdata.supplier.domain.Supplier>lambdaQuery()
                .eq(
                    com.ruoyi.business.masterdata.supplier.domain.Supplier::getSupplierId,
                    supplierId)
                .last("FOR UPDATE"));
    if (supplier == null || supplier.getStatus() != EnableStatus.ENABLED) {
      throw new ServiceException("供应商无效或已停用");
    }
    List<Long> placeIds =
        input.stream()
            .map(ReceiptLineInput::getLocationId)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    HashMap<Long, Location> places = new HashMap<Long, Location>();
    for (Long id : placeIds) {
      Location place =
          locations.selectOne(
              Wrappers.<Location>lambdaQuery().eq(Location::getLocationId, id).last("FOR UPDATE"));
      if (place == null || place.getStatus() != EnableStatus.ENABLED) {
        throw new ServiceException("货位无效、停用或不属于当前仓库");
      }
      places.put(id, place);
    }
    Collection<PurchaseArrivalLine> sourceLines = source.lines.values();
    HashMap<Long, Unit> lockedUnits =
        new HashMap<Long, com.ruoyi.business.masterdata.unit.domain.Unit>();
    for (Long id :
        sourceLines.stream().map(PurchaseArrivalLine::getUnitId).distinct().sorted().toList()) {
      lockedUnits.put(
          id,
          units.selectOne(
              Wrappers.<com.ruoyi.business.masterdata.unit.domain.Unit>lambdaQuery()
                  .eq(com.ruoyi.business.masterdata.unit.domain.Unit::getUnitId, id)
                  .last("FOR UPDATE")));
    }
    HashMap<Long, Item> lockedItems =
        new HashMap<Long, com.ruoyi.business.masterdata.item.domain.Item>();
    for (Long id :
        sourceLines.stream().map(PurchaseArrivalLine::getItemId).distinct().sorted().toList()) {
      lockedItems.put(
          id,
          items.selectOne(
              Wrappers.<com.ruoyi.business.masterdata.item.domain.Item>lambdaQuery()
                  .eq(com.ruoyi.business.masterdata.item.domain.Item::getItemId, id)
                  .last("FOR UPDATE")));
    }
    HashSet<String> seen = new HashSet<String>();
    for (ReceiptLineInput row : input) {
      PurchaseArrivalLine line = source.lines.get(row.getArrivalLineId());
      PurchaseArrival header = source.headers.get(line.getArrivalId());
      if (header.getStatus() != ArrivalStatus.APPROVED) {
        throw new ServiceException("来源到货单未审核或已作废");
      }
      if (!supplierId.equals(header.getSupplierId())) {
        throw new ServiceException("一张入库单只能参照同供应商的到货明细");
      }
      Warehouse warehouse = selectedWarehouses.get(row.getWarehouseId());
      Location place = places.get(row.getLocationId());
      if (place != null && !row.getWarehouseId().equals(place.getWarehouseId())) {
        throw new ServiceException("货位不属于本行入库仓库");
      }
      if (date.isBefore(header.getArrivalDate())) {
        throw new ServiceException("入库日期不能早于来源到货日期");
      }
      if (row.getQuantity().stripTrailingZeros().scale() > line.getQuantityDecimalPlaces()) {
        throw new ServiceException("入库数量超出来源允许的小数位");
      }
      if (Boolean.TRUE.equals(warehouse.getLocationEnabled()) != (row.getLocationId() != null)) {
        throw new ServiceException(
            Boolean.TRUE.equals(warehouse.getLocationEnabled()) ? "请选择入库货位" : "当前仓库不使用货位，不能指定货位");
      }
      if (!seen.add(
          row.getArrivalLineId() + ":" + row.getWarehouseId() + ":" + row.getLocationId())) {
        throw new ServiceException("同一来源到货行不能重复使用相同仓库和货位");
      }
      Unit unit = lockedUnits.get(line.getUnitId());
      Item item = lockedItems.get(line.getItemId());
      if (unit == null || unit.getStatus() != EnableStatus.ENABLED) {
        throw new ServiceException("库存单位无效或停用");
      }
      if (item == null
          || item.getStatus() != EnableStatus.ENABLED
          || !Boolean.TRUE.equals(item.getPurchaseEnabled())) {
        throw new ServiceException("料品无效、停用或不允许采购");
      }
      if (!line.getUnitId().equals(item.getBaseUnitId())) {
        throw new ServiceException("来源单位与库存单位不一致");
      }
      ItemCategory category = categories.selectById(item.getCategoryId());
      if (category == null || category.getStatus() != CategoryStatus.ENABLED) {
        throw new ServiceException("料品分类无效或停用");
      }
    }
    if (checkAvailable) {
      for (Entry<Long, BigDecimal> entry : totals(input).entrySet()) {
        if (entry.getValue().compareTo(available(source.lines.get(entry.getKey()))) > 0) {
          throw new ServiceException("来源到货行可入库数量不足，请刷新后重试");
        }
      }
    }
    return places;
  }

  /** 实际仓库按 ID 顺序加锁，再由调用方锁货位，支持跨仓库明细。 */
  private Map<Long, Warehouse> lockWarehouses(List<ReceiptLineInput> input) {
    LinkedHashMap<Long, Warehouse> result = new LinkedHashMap<Long, Warehouse>();
    for (Long id :
        input.stream().map(ReceiptLineInput::getWarehouseId).distinct().sorted().toList()) {
      result.put(id, lockWarehouse(id));
    }
    return result;
  }

  /** 从明细快照汇总列表仓库摘要，不读取当前档案名称。 */
  private List<String> warehouseNames(List<PurchaseReceiptLine> rows) {
    return rows.stream()
        .map(r -> r.getWarehouseCode() + " / " + r.getWarehouseName())
        .distinct()
        .toList();
  }

  /** 当前仓库行锁与仓库、货位维护共用。 */
  private Warehouse lockWarehouse(Long id) {
    Warehouse w =
        warehouses.selectOne(
            Wrappers.<Warehouse>lambdaQuery().eq(Warehouse::getWarehouseId, id).last("FOR UPDATE"));
    if (w == null) {
      throw new ServiceException("仓库不存在或已删除");
    }
    return w;
  }

  /** 表头仅保存供应商等整单快照，实际仓库由明细独立保存。 */
  private void copyHeader(SaveReceiptRequest r, PurchaseReceipt h, Sources source) {
    PurchaseArrival first = source.headers.values().iterator().next();
    h.setReceiptDate(r.getReceiptDate());
    h.setSupplierId(first.getSupplierId());
    h.setSupplierCode(first.getSupplierCode());
    h.setSupplierName(first.getSupplierName());
    h.setRemark(r.getRemark());
    h.setLineCount(r.getLines().size());
  }

  /** 明细保存仅复制受控字段，拒绝跨单行 ID 和复活已删除行。 */
  private void saveLines(
      PurchaseReceipt h,
      List<ReceiptLineInput> input,
      Sources source,
      Map<Long, Location> places,
      List<PurchaseReceiptLine> old) {
    Map<Long, PurchaseReceiptLine> existing =
        old.stream().collect(Collectors.toMap(PurchaseReceiptLine::getLineId, Function.identity()));
    Map<Long, Warehouse> selectedWarehouses = lockWarehouses(input);
    HashSet<Long> retained = new HashSet<Long>();
    for (int i = 0; i < input.size(); i++) {
      ReceiptLineInput r = input.get(i);
      PurchaseReceiptLine row =
          r.getLineId() == null ? new PurchaseReceiptLine() : existing.get(r.getLineId());
      if (row == null || (r.getLineId() != null && !retained.add(r.getLineId()))) {
        throw new ServiceException("入库行已失效、重复或不属于当前单据");
      }
      PurchaseArrivalLine s = source.lines.get(r.getArrivalLineId());
      PurchaseArrival a = source.headers.get(s.getArrivalId());
      Location p = places.get(r.getLocationId());
      row.setReceiptId(h.getReceiptId());
      row.setLineNo(i + 1);
      row.setArrivalId(a.getArrivalId());
      row.setArrivalLineId(s.getLineId());
      row.setArrivalNo(a.getArrivalNo());
      row.setArrivalLineNo(s.getLineNo());
      row.setOrderId(s.getOrderId());
      row.setOrderLineId(s.getOrderLineId());
      row.setOrderNo(s.getOrderNo());
      row.setOrderLineNo(s.getOrderLineNo());
      row.setItemId(s.getItemId());
      row.setItemCode(s.getItemCode());
      row.setItemName(s.getItemName());
      row.setSpecification(s.getSpecification());
      row.setUnitId(s.getUnitId());
      row.setUnitName(s.getUnitName());
      Warehouse warehouse = selectedWarehouses.get(r.getWarehouseId());
      // 未更换仓库时保留已有快照，避免草稿编辑刷新历史名称。
      if (!Objects.equals(row.getWarehouseId(), warehouse.getWarehouseId())) {
        row.setWarehouseId(warehouse.getWarehouseId());
        row.setWarehouseCode(warehouse.getWarehouseCode());
        row.setWarehouseName(warehouse.getWarehouseName());
        row.setLocationEnabled(warehouse.getLocationEnabled());
      }
      row.setLocationId(r.getLocationId());
      row.setLocationCode(p == null ? "" : p.getLocationCode());
      row.setLocationName(p == null ? "" : p.getLocationName());
      row.setQuantity(r.getQuantity());
      row.setQuantityDecimalPlaces(s.getQuantityDecimalPlaces());
      row.setRemark(r.getRemark());
      if (row.getLineId() == null) {
        lines.insert(row);
      } else if (lines.updateById(row) != 1) {
        throw stale();
      }
    }
    for (PurchaseReceiptLine row : old) {
      if (!retained.contains(row.getLineId()) && lines.deleteById(row) != 1) {
        throw stale();
      }
    }
  }

  /** 每个来源行只更新一次累计数，每张到货单只递增一次版本。 */
  private void postQuantities(List<ReceiptLineInput> input, Sources source, ReceiptAction action) {
    for (Entry<Long, BigDecimal> entry : totals(input).entrySet()) {
      PurchaseArrivalLine row = source.lines.get(entry.getKey());
      BigDecimal q = entry.getValue();
      BigDecimal pending = row.getPendingReceiptQuantity();
      BigDecimal received = row.getReceivedQuantity();
      switch (action) {
        case SUBMIT -> pending = pending.add(q);
        case WITHDRAW, REJECT -> pending = pending.subtract(q);
        case APPROVE -> {
          pending = pending.subtract(q);
          received = received.add(q);
        }
        case VOID -> received = received.subtract(q);
      }
      if (pending.signum() < 0
          || received.signum() < 0
          || pending.add(received).compareTo(row.getQuantity()) > 0) {
        throw new ServiceException("到货入库累计数量异常或额度不足");
      }
      row.setPendingReceiptQuantity(pending);
      row.setReceivedQuantity(received);
      if (arrivalLines.updateById(row) != 1) {
        throw stale();
      }
    }
    for (PurchaseArrival h : source.headers.values()) {
      int v = h.getVersion();
      if (v == Integer.MAX_VALUE) {
        throw new ServiceException("来源到货单版本已达上限");
      }
      h.setVersion(v + 1);
      if (arrivals.update(
              h,
              Wrappers.<PurchaseArrival>lambdaUpdate()
                  .eq(PurchaseArrival::getArrivalId, h.getArrivalId())
                  .eq(PurchaseArrival::getVersion, v))
          != 1) {
        throw stale();
      }
    }
  }

  /** 按来源聚合拆分行，不按货位分别消耗来源额度。 */
  private Map<Long, BigDecimal> totals(List<ReceiptLineInput> input) {
    TreeMap<Long, BigDecimal> result = new TreeMap<Long, BigDecimal>();
    for (ReceiptLineInput row : input) {
      result.merge(row.getArrivalLineId(), row.getQuantity(), BigDecimal::add);
    }
    return result;
  }

  /** 保存快照转为统一来源校验输入。 */
  private ReceiptLineInput toInput(PurchaseReceiptLine line) {
    ReceiptLineInput r = new ReceiptLineInput();
    r.setLineId(line.getLineId());
    r.setArrivalLineId(line.getArrivalLineId());
    r.setWarehouseId(line.getWarehouseId());
    r.setLocationId(line.getLocationId());
    r.setQuantity(line.getQuantity());
    r.setRemark(line.getRemark());
    return r;
  }

  /** 当前可新增入库量，不包含已提交占用。 */
  private BigDecimal available(PurchaseArrivalLine row) {
    return row.getQuantity()
        .subtract(row.getPendingReceiptQuantity())
        .subtract(row.getReceivedQuantity());
  }

  /** 已有整单当前读并校验页面版本。 */
  private PurchaseReceipt lock(Long id, Integer version) {
    if (id == null || id <= 0 || version == null || version < 0) {
      throw new ServiceException("请指定有效入库单 ID 和版本号");
    }
    PurchaseReceipt h =
        receipts.selectOne(
            Wrappers.<PurchaseReceipt>lambdaQuery()
                .eq(PurchaseReceipt::getReceiptId, id)
                .last("FOR UPDATE"));
    if (h == null) {
      throw new ServiceException("入库单不存在或已删除");
    }
    if (!version.equals(h.getVersion())) {
      throw stale();
    }
    return h;
  }

  /** 行号稳定排序加载有效明细。 */
  private List<PurchaseReceiptLine> existingLines(Long id) {
    return lines.selectList(
        Wrappers.<PurchaseReceiptLine>lambdaQuery()
            .eq(PurchaseReceiptLine::getReceiptId, id)
            .orderByAsc(PurchaseReceiptLine::getLineNo, PurchaseReceiptLine::getLineId));
  }

  /** 受版本保护更新头信息及审计。 */
  private void updateVersion(PurchaseReceipt h) {
    int v = h.getVersion();
    if (v == Integer.MAX_VALUE) {
      throw new ServiceException("入库单版本已达上限");
    }
    h.setVersion(v + 1);
    if (receipts.update(
            h,
            Wrappers.<PurchaseReceipt>lambdaUpdate()
                .eq(PurchaseReceipt::getReceiptId, h.getReceiptId())
                .eq(PurchaseReceipt::getVersion, v))
        != 1) {
      throw stale();
    }
  }

  /** 拒绝状态不匹配的命令。 */
  private void requireStatus(PurchaseReceipt h, ReceiptStatus expected) {
    if (h.getStatus() != expected) {
      throw new ServiceException("当前入库单状态不允许此操作");
    }
  }

  /** 内部调用和接口调用执行相同字段约束。 */
  private void validate(Object value) {
    if (value == null) {
      throw new ServiceException("请求不能为空");
    }
    Set<ConstraintViolation<Object>> errors = validator.validate(value);
    if (!errors.isEmpty()) {
      throw new ConstraintViolationException(errors);
    }
  }

  /** 将 SQL 通配符当作普通查询文本。 */
  private String escape(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** 统一旧页面提示。 */
  private ServiceException stale() {
    return new ServiceException("入库单或来源到货单已变更，请刷新后重试");
  }

  /** 保存持锁后的来源集合。 */
  private static final class Sources {
    private final Map<Long, PurchaseArrival> headers;
    private final Map<Long, PurchaseArrivalLine> lines;

    /** 来源仅在当前事务内使用。 */
    private Sources(Map<Long, PurchaseArrival> headers, Map<Long, PurchaseArrivalLine> lines) {
      this.headers = headers;
      this.lines = lines;
    }
  }
}
