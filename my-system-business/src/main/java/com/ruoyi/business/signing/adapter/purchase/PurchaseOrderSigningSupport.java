package com.ruoyi.business.signing.adapter.purchase;

import com.ruoyi.business.purchase.order.domain.OrderStatus;
import com.ruoyi.business.purchase.order.dto.OrderQuery;
import com.ruoyi.business.purchase.order.service.OrderService;
import com.ruoyi.business.purchase.order.vo.OrderView;
import com.ruoyi.business.signing.dto.SigningSourceQuery;
import com.ruoyi.business.signing.dto.SigningSourceView;
import com.ruoyi.business.signing.spi.SigningBusinessSupport;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 采购订单访问与来源选择，签署控制器不再引用订单服务。 */
@Component
public class PurchaseOrderSigningSupport implements SigningBusinessSupport {
  private final OrderService orders;

  /** 注入来源查询服务。 */
  public PurchaseOrderSigningSupport(OrderService orders) {
    this.orders = orders;
  }

  /** {@inheritDoc} */
  public String businessType() {
    return "purchase.order";
  }

  /** {@inheritDoc} */
  public String businessName() {
    return "采购订单";
  }

  /** {@inheritDoc} */
  public boolean canAccess() {
    return SecurityUtils.hasPermi("purchase:order:query");
  }

  /** {@inheritDoc} */
  public void authorize(String businessId) {
    requireAccess();
    if (businessId == null || !businessId.matches("[1-9][0-9]{0,18}")) {
      throw new ServiceException("订单 ID 不合法");
    }
    orders.detail(Long.valueOf(businessId));
  }

  /** 检查来源业务权限。 */
  private void requireAccess() {
    if (!canAccess()) {
      throw new ServiceException("需要采购订单查询权限", 403);
    }
  }

  /** {@inheritDoc} */
  public PageResult<SigningSourceView> sources(SigningSourceQuery query) {
    requireAccess();
    OrderQuery request = new OrderQuery();
    request.setPageNum(query.getPageNum());
    request.setPageSize(query.getPageSize());
    request.setOrderNo(query.getKeyword());
    request.setStatus(OrderStatus.APPROVED.name());
    request.setSortField("orderDate");
    request.setSortDirection("desc");
    PageResult<OrderView> page = orders.list(request);
    List<SigningSourceView> rows = new ArrayList<SigningSourceView>();
    for (OrderView order : page.getRows()) {
      SigningSourceView row = new SigningSourceView();
      row.setId(String.valueOf(order.getOrderId()));
      row.setNumber(order.getOrderNo());
      row.setTitle(order.getOrderNo() + "采购合同");
      row.setBusinessDate(order.getOrderDate());
      row.setCounterpartyName(order.getSupplierName());
      rows.add(row);
    }
    return new PageResult<SigningSourceView>(rows, page.getTotal());
  }
}
