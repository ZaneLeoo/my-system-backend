package com.ruoyi.business.purchase.arrival.domain;

import java.util.Arrays;

/** 明确状态转换，不允许客户端直接指定任意状态。 */
public enum ArrivalAction {
  SUBMIT("submit", ArrivalStatus.DRAFT, ArrivalStatus.SUBMITTED),
  WITHDRAW("withdraw", ArrivalStatus.SUBMITTED, ArrivalStatus.DRAFT),
  APPROVE("approve", ArrivalStatus.SUBMITTED, ArrivalStatus.APPROVED),
  REJECT("reject", ArrivalStatus.SUBMITTED, ArrivalStatus.DRAFT),
  VOID("void", ArrivalStatus.APPROVED, ArrivalStatus.VOIDED);

  private final String path;
  private final ArrivalStatus source;
  private final ArrivalStatus target;

  /** 绑定接口路径及允许的起止状态。 */
  ArrivalAction(String path, ArrivalStatus source, ArrivalStatus target) {
    this.path = path;
    this.source = source;
    this.target = target;
  }

  /** 返回允许的起始状态。 */
  public ArrivalStatus getSource() {
    return source;
  }

  /** 返回成功后的状态。 */
  public ArrivalStatus getTarget() {
    return target;
  }

  /** 从受控路由解析操作。 */
  public static ArrivalAction fromPath(String path) {
    return Arrays.stream(values())
        .filter(v -> v.path.equals(path))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("不支持的到货操作"));
  }
}
