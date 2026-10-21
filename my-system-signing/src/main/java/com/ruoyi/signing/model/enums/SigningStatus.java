package com.ruoyi.signing.model.enums;

/** 签署领域的SigningStatus取值，独立于平台数字状态。 */
public enum SigningStatus {
  DRAFT,
  SIGNING,
  COMPLETED,
  REVOKED,
  EXPIRED,
  REJECTED
}
