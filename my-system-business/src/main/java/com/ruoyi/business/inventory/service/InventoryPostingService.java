package com.ruoyi.business.inventory.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.business.inventory.domain.*;
import com.ruoyi.business.inventory.mapper.*;
import com.ruoyi.business.purchase.order.domain.OrderRules;
import com.ruoyi.business.purchase.receipt.domain.*;
import com.ruoyi.common.exception.ServiceException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** 库存唯一写入入口，强制加入调用方事务，无公开增减接口。 */
@Service
@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
public class InventoryPostingService {
  public static final String RECEIPT = "PURCHASE_RECEIPT";
  public static final String RECEIPT_VOID = "PURCHASE_RECEIPT_VOID";
  private static final BigDecimal MAX_QUANTITY = new BigDecimal("99999999999999.999999");
  private final InventoryBalanceMapper balances;
  private final InventoryTransactionMapper transactions;

  /** 注入库存持久化组件。 */
  public InventoryPostingService(
      InventoryBalanceMapper balances, InventoryTransactionMapper transactions) {
    this.balances = balances;
    this.transactions = transactions;
  }

  /** 审核入库逐行记账，固定维度顺序及行 ID 顺序。 */
  public void receive(PurchaseReceipt header, List<PurchaseReceiptLine> lines) {
    var time = LocalDateTime.now(OrderRules.TIME_ZONE);
    for (var row : ordered(lines))
      post(header, row, null, row.getQuantity(), header.getReceiptDate(), time, header.getRemark());
  }

  /** 追加原流水的反向记录，不修改原历史；任何维度不足整单回滚。 */
  public void reverse(PurchaseReceipt header, List<PurchaseReceiptLine> lines, String reason) {
    var original =
        transactions.selectList(
            Wrappers.<InventoryTransaction>lambdaQuery()
                .eq(InventoryTransaction::getReceiptId, header.getReceiptId())
                .eq(InventoryTransaction::getBusinessType, RECEIPT));
    var byLine = new HashMap<Long, InventoryTransaction>();
    for (var tx : original) byLine.put(tx.getReceiptLineId(), tx);
    if (byLine.size() != lines.size()) throw new ServiceException("入库记账流水不完整，不能作废");
    var time = LocalDateTime.now(OrderRules.TIME_ZONE);
    for (var row : ordered(lines)) {
      var tx = byLine.get(row.getLineId());
      if (tx == null
          || tx.getQuantityChange().compareTo(row.getQuantity()) != 0
          || !Objects.equals(tx.getItemId(), row.getItemId())
          || !Objects.equals(tx.getLocationId(), row.getLocationId())
          || !Objects.equals(tx.getWarehouseId(), row.getWarehouseId())
          || !Objects.equals(tx.getUnitId(), row.getUnitId()))
        throw new ServiceException("原入库流水与单据快照不一致");
      post(header, row, tx, tx.getQuantityChange().negate(), time.toLocalDate(), time, reason);
    }
  }

  /** 仓库在调用方已持锁；库存唯一索引兜底首次创建维度。 */
  private void post(
      PurchaseReceipt h,
      PurchaseReceiptLine row,
      InventoryTransaction original,
      BigDecimal delta,
      LocalDate date,
      LocalDateTime time,
      String reason) {
    var q =
        Wrappers.<InventoryBalance>lambdaQuery()
            .eq(InventoryBalance::getWarehouseId, row.getWarehouseId())
            .eq(InventoryBalance::getItemId, row.getItemId());
    if (row.getLocationId() == null) q.isNull(InventoryBalance::getLocationId);
    else q.eq(InventoryBalance::getLocationId, row.getLocationId());
    var balance = balances.selectOne(q.last("FOR UPDATE"));
    if (balance == null) {
      if (original != null) throw new ServiceException("原入库库存维度不存在");
      balance = new InventoryBalance();
      balance.setWarehouseId(row.getWarehouseId());
      balance.setLocationId(row.getLocationId());
      balance.setItemId(row.getItemId());
      balance.setUnitId(row.getUnitId());
      balances.insert(balance);
    }
    if (!balance.getUnitId().equals(row.getUnitId())
        || (original != null && !balance.getBalanceId().equals(original.getBalanceId())))
      throw new ServiceException("库存单位或维度与原记账不一致");
    BigDecimal before = balance.getQuantity(), after = before.add(delta);
    if (after.signum() < 0) throw new ServiceException("当前库存不足，不能冲销入库单");
    if (after.compareTo(MAX_QUANTITY) > 0) throw new ServiceException("库存数量超出数据库允许范围");
    long version = balance.getVersion();
    if (version == Long.MAX_VALUE) throw new ServiceException("库存版本已达上限");
    balance.setQuantity(after);
    balance.setVersion(version + 1);
    if (balances.update(
            balance,
            Wrappers.<InventoryBalance>lambdaUpdate()
                .eq(InventoryBalance::getBalanceId, balance.getBalanceId())
                .eq(InventoryBalance::getVersion, version))
        != 1) throw new ServiceException("库存已变更，请重试");
    var tx = new InventoryTransaction();
    tx.setBalanceId(balance.getBalanceId());
    tx.setBusinessType(original == null ? RECEIPT : RECEIPT_VOID);
    tx.setReceiptId(h.getReceiptId());
    tx.setReceiptNo(h.getReceiptNo());
    tx.setReceiptLineId(row.getLineId());
    tx.setWarehouseId(row.getWarehouseId());
    tx.setWarehouseCode(row.getWarehouseCode());
    tx.setWarehouseName(row.getWarehouseName());
    tx.setLocationId(row.getLocationId());
    tx.setLocationCode(row.getLocationCode());
    tx.setLocationName(row.getLocationName());
    tx.setItemId(row.getItemId());
    tx.setItemCode(row.getItemCode());
    tx.setItemName(row.getItemName());
    tx.setSpecification(row.getSpecification());
    tx.setUnitId(row.getUnitId());
    tx.setUnitName(row.getUnitName());
    tx.setQuantityChange(delta);
    tx.setQuantityBefore(before);
    tx.setQuantityAfter(after);
    tx.setBusinessDate(date);
    tx.setPostedTime(time);
    tx.setReversalOf(original == null ? null : original.getTransactionId());
    tx.setRemark(reason);
    transactions.insert(tx);
  }

  /** 统一维度顺序，多个来源记入同一维度时按稳定行 ID 执行。 */
  private List<PurchaseReceiptLine> ordered(List<PurchaseReceiptLine> lines) {
    return lines.stream()
        .sorted(
            Comparator.comparing(PurchaseReceiptLine::getWarehouseId)
                .thenComparing(
                    PurchaseReceiptLine::getLocationId,
                    Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(PurchaseReceiptLine::getItemId)
                .thenComparing(PurchaseReceiptLine::getLineId))
        .toList();
  }
}
