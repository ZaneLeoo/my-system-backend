package com.ruoyi.business.signing.adapter.purchase;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.business.purchase.order.domain.OrderStatus;
import com.ruoyi.business.purchase.order.service.OrderService;
import com.ruoyi.business.purchase.order.vo.*;
import com.ruoyi.business.signing.config.SigningProperties;
import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.PartyRole;
import com.ruoyi.signing.model.enums.SigningPartyType;
import com.ruoyi.signing.spi.SigningBusinessAdapter;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;

/** 订单适配只生成合同业务快照，不改变采购和库存状态。 */
@Component
public class PurchaseOrderSigningAdapter implements SigningBusinessAdapter {
  private final OrderService orders;
  private final SigningProperties config;

  /** 注入现有业务查询及我方主体配置。 */
  public PurchaseOrderSigningAdapter(OrderService orders, SigningProperties config) {
    this.orders = orders;
    this.config = config;
  }

  /** {@inheritDoc} */
  public String businessType() {
    return "purchase.order";
  }

  /** {@inheritDoc} */
  public List<SigningField> fields() {
    return PurchaseOrderSigningFieldCatalog.fields();
  }

  /** {@inheritDoc} */
  public SigningParty defaultCounterparty(Map<String, Object> values) {
    SigningParty party = new SigningParty();
    party.setRole(PartyRole.COUNTERPARTY.name());
    party.setPartyType(SigningPartyType.ORGANIZATION);
    party.setOrganizationName(text(values, "supplierName"));
    party.setTransactorName(text(values, "contactName"));
    party.setTransactorAccount(text(values, "contactPhone"));
    party.setSignOrder(2);
    return party;
  }

  /** 金额使用十进制字符串，空值保留。 */
  private String decimal(BigDecimal value) {
    return value == null ? null : value.toPlainString();
  }

  /** {@inheritDoc} */
  public BusinessSnapshot snapshot(String businessId) {
    if (!businessId.matches("[1-9][0-9]{0,18}")) {
      throw new SigningException("订单 ID 不合法");
    }
    OrderView order = orders.detail(Long.valueOf(businessId));
    if (order.getStatus() != OrderStatus.APPROVED) {
      throw new SigningException("仅已审核采购订单允许发起签署");
    }
    BusinessSnapshot result = new BusinessSnapshot();
    result.setTitle(order.getOrderNo() + "采购合同");
    Map<String, Object> values =
        map(
            "orderNo",
            order.getOrderNo(),
            "orderDate",
            String.valueOf(order.getOrderDate()),
            "supplierCode",
            order.getSupplierCode(),
            "supplierName",
            order.getSupplierName(),
            "contactName",
            order.getContactName(),
            "contactPhone",
            order.getContactPhone(),
            "address",
            order.getAddress(),
            "buyerName",
            order.getBuyerName(),
            "deptName",
            order.getDeptName(),
            "currency",
            order.getCurrency(),
            "netAmount",
            decimal(order.getNetAmount()),
            "tax",
            decimal(order.getTax()),
            "grossAmount",
            decimal(order.getGrossAmount()),
            "remark",
            order.getRemark(),
            "ourCompanyName",
            config.getOrganizationName());
    List<Object> lines = new ArrayList<Object>();
    for (OrderLineView line : order.getLines()) {
      lines.add(
          map(
              "lineNo",
              line.getLineNo(),
              "itemCode",
              line.getItemCode(),
              "itemName",
              line.getItemName(),
              "specification",
              line.getSpecification(),
              "unitName",
              line.getUnitName(),
              "quantity",
              decimal(line.getQuantity()),
              "netPrice",
              decimal(line.getNetPrice()),
              "grossPrice",
              decimal(line.getGrossPrice()),
              "taxRate",
              decimal(line.getTaxRate()),
              "netAmount",
              decimal(line.getNetAmount()),
              "tax",
              decimal(line.getTax()),
              "grossAmount",
              decimal(line.getGrossAmount()),
              "expectedDate",
              line.getExpectedDate() == null ? null : line.getExpectedDate().toString(),
              "remark",
              line.getRemark()));
    }
    values.put("lines", lines);
    result.setValues(values);
    return result;
  }
}
