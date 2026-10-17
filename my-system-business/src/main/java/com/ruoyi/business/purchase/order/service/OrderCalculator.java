package com.ruoyi.business.purchase.order.service;

import com.ruoyi.business.purchase.order.domain.OrderRules;
import com.ruoyi.business.purchase.order.domain.PriceTaxMode;
import com.ruoyi.business.purchase.order.dto.AmountInput;
import com.ruoyi.business.purchase.order.vo.LineAmounts;
import com.ruoyi.business.purchase.order.vo.OrderCalculation;
import com.ruoyi.common.exception.ServiceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 只计算金额，不访问档案或写入数据库。 */
@Component
public class OrderCalculator {
  /** 按录入口径逐行舍入并汇总，不使用换算后的单价反算金额。 */
  public OrderCalculation calculate(
      PriceTaxMode mode,
      List<? extends AmountInput> inputs,
      int quantityDecimals,
      int priceDecimals) {
    if (mode == null || inputs == null || inputs.isEmpty() || inputs.size() > OrderRules.MAX_LINES)
      throw new ServiceException("订单须包含 1～200 行有效明细及计价方式");
    if (quantityDecimals < 0
        || quantityDecimals > OrderRules.MAX_DECIMALS
        || priceDecimals < 0
        || priceDecimals > OrderRules.MAX_DECIMALS) throw new ServiceException("订单精度规则无效");
    BigDecimal net = BigDecimal.ZERO, tax = BigDecimal.ZERO, gross = BigDecimal.ZERO;
    List<LineAmounts> lines = new ArrayList<>();
    for (int i = 0; i < inputs.size(); i++) {
      AmountInput input = inputs.get(i);
      if (input == null) throw new ServiceException("第 " + (i + 1) + " 行不能为空");
      checkNumber(input.getQuantity(), quantityDecimals, true, "数量", i);
      checkNumber(input.getUnitPrice(), priceDecimals, false, "单价", i);
      BigDecimal rate = input.getTaxRate();
      if (rate == null
          || rate.signum() < 0
          || rate.compareTo(new BigDecimal("100")) > 0
          || rate.stripTrailingZeros().scale() > 2)
        throw new ServiceException("第 " + (i + 1) + " 行须填写 0～100 且最多两位小数的税率");
      BigDecimal factor = BigDecimal.ONE.add(rate.movePointLeft(2));
      LineAmounts line = new LineAmounts();
      if (mode == PriceTaxMode.TAX_INCLUDED) {
        line.setGrossPrice(input.getUnitPrice());
        line.setNetPrice(input.getUnitPrice().divide(factor, priceDecimals, OrderRules.ROUNDING));
        line.setGrossAmount(money(input.getQuantity().multiply(input.getUnitPrice())));
        line.setNetAmount(
            line.getGrossAmount().divide(factor, OrderRules.MONEY_DECIMALS, OrderRules.ROUNDING));
        line.setTax(line.getGrossAmount().subtract(line.getNetAmount()));
      } else {
        line.setNetPrice(input.getUnitPrice());
        line.setGrossPrice(
            input.getUnitPrice().multiply(factor).setScale(priceDecimals, OrderRules.ROUNDING));
        line.setNetAmount(money(input.getQuantity().multiply(input.getUnitPrice())));
        line.setTax(money(line.getNetAmount().multiply(rate.movePointLeft(2))));
        line.setGrossAmount(line.getNetAmount().add(line.getTax()));
      }
      if (line.getNetPrice().compareTo(OrderRules.PRICE_LIMIT) >= 0
          || line.getGrossPrice().compareTo(OrderRules.PRICE_LIMIT) >= 0)
        throw new ServiceException("第 " + (i + 1) + " 行换算后的单价超出允许范围");
      checkMoney(line.getGrossAmount());
      net = net.add(line.getNetAmount());
      tax = tax.add(line.getTax());
      gross = gross.add(line.getGrossAmount());
      checkMoney(gross);
      lines.add(line);
    }
    OrderCalculation result = new OrderCalculation();
    result.setLines(lines);
    result.setNetAmount(money(net));
    result.setTax(money(tax));
    result.setGrossAmount(money(gross));
    return result;
  }

  /** 输入只校验有效小数位，不把数据库补齐的末尾零当成超精度。 */
  private void checkNumber(
      BigDecimal value, int decimals, boolean positive, String label, int index) {
    if (value == null
        || value.signum() < 0
        || (positive && value.signum() == 0)
        || value.stripTrailingZeros().scale() > decimals
        || value.compareTo(positive ? OrderRules.QUANTITY_LIMIT : OrderRules.PRICE_LIMIT) >= 0)
      throw new ServiceException(
          "第 "
              + (index + 1)
              + " 行"
              + label
              + "无效，最多 "
              + decimals
              + " 位小数且整数部分最多 "
              + (positive ? 9 : 12)
              + " 位");
  }

  /** 统一逐行金额舍入方式。 */
  private BigDecimal money(BigDecimal value) {
    return value.setScale(OrderRules.MONEY_DECIMALS, OrderRules.ROUNDING);
  }

  /** 数据库溢出前返回可理解的业务提示。 */
  private void checkMoney(BigDecimal value) {
    if (value.compareTo(OrderRules.MONEY_LIMIT) >= 0) throw new ServiceException("订单金额超出允许范围");
  }
}
