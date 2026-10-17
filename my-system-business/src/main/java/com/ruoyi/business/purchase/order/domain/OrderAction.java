package com.ruoyi.business.purchase.order.domain;

import java.util.Arrays;

/** 明确状态转换，不允许客户端直接指定任意状态。 */
public enum OrderAction {
  SUBMIT("submit", OrderStatus.DRAFT, OrderStatus.SUBMITTED),
  WITHDRAW("withdraw", OrderStatus.SUBMITTED, OrderStatus.DRAFT),
  APPROVE("approve", OrderStatus.SUBMITTED, OrderStatus.APPROVED),
  REJECT("reject", OrderStatus.SUBMITTED, OrderStatus.DRAFT),
  CLOSE("close", OrderStatus.APPROVED, OrderStatus.CLOSED);

  private final String path;
  private final OrderStatus source;
  private final OrderStatus target;

  /** 绑定接口路径及允许的起止状态。 */
  OrderAction(String path, OrderStatus source, OrderStatus target) {
    this.path = path;
    this.source = source;
    this.target = target;
  }

  /** 返回允许的起始状态。 */
  public OrderStatus getSource() {
    return source;
  }

  /** 返回成功后的状态。 */
  public OrderStatus getTarget() {
    return target;
  }

  /** 从受控路由解析操作。 */
  public static OrderAction fromPath(String path) {
    return Arrays.stream(values())
        .filter(v -> v.path.equals(path))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("不支持的订单操作"));
  }
}
