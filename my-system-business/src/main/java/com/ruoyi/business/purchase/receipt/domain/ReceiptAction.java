package com.ruoyi.business.purchase.receipt.domain;

import java.util.Arrays;

/** 明确状态转换，不允许客户端直接指定任意状态。 */
public enum ReceiptAction {
  SUBMIT("submit", ReceiptStatus.DRAFT, ReceiptStatus.SUBMITTED),
  WITHDRAW("withdraw", ReceiptStatus.SUBMITTED, ReceiptStatus.DRAFT),
  APPROVE("approve", ReceiptStatus.SUBMITTED, ReceiptStatus.APPROVED),
  REJECT("reject", ReceiptStatus.SUBMITTED, ReceiptStatus.DRAFT),
  VOID("void", ReceiptStatus.APPROVED, ReceiptStatus.VOIDED);

  private final String path;
  private final ReceiptStatus source;
  private final ReceiptStatus target;

  /** 绑定接口路径及允许的起止状态。 */
  ReceiptAction(String path, ReceiptStatus source, ReceiptStatus target) {
    this.path = path;
    this.source = source;
    this.target = target;
  }

  /** 返回允许的起始状态。 */
  public ReceiptStatus getSource() {
    return source;
  }

  /** 返回成功后的状态。 */
  public ReceiptStatus getTarget() {
    return target;
  }

  /** 从受控路由解析操作。 */
  public static ReceiptAction fromPath(String path) {
    return Arrays.stream(values())
        .filter(v -> v.path.equals(path))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("不支持的入库操作"));
  }
}
