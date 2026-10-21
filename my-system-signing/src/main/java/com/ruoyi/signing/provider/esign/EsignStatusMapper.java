package com.ruoyi.signing.provider.esign;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.enums.SigningStatus;

/** 平台流程状态仅在此转换为公共状态。 */
public final class EsignStatusMapper {
  /** 转换单个签署方状态；未知值保留 UNKNOWN，不猜测签署成功。 */
  public String party(Object value) {
    String status = com.ruoyi.signing.application.SigningSupport.numberText(value);
    switch (status) {
      case "0":
        return "WAITING";
      case "1":
        return "SIGNING";
      case "2":
        return "SIGNED";
      case "3":
        return "APPROVAL_PENDING";
      case "4":
        return "REJECTED";
      default:
        return "UNKNOWN";
    }
  }

  /** 转换权威详情状态，未知状态拒绝猜测。 */
  public SigningStatus flow(Object value) {
    String status = com.ruoyi.signing.application.SigningSupport.numberText(value);
    switch (status) {
      case "0":
        return SigningStatus.DRAFT;
      case "1":
        return SigningStatus.SIGNING;
      case "2":
        return SigningStatus.COMPLETED;
      case "3":
        return SigningStatus.REVOKED;
      case "5":
        return SigningStatus.EXPIRED;
      case "7":
        return SigningStatus.REJECTED;
      default:
        throw new SigningException("平台返回未知签署状态，请核对");
    }
  }
}
