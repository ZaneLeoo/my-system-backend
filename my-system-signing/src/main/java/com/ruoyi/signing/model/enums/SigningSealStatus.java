package com.ruoyi.signing.model.enums;

/** 平台无关的印章状态，未知状态不可用于签署。 */
public enum SigningSealStatus {
  ENABLED,
  PENDING,
  REJECTED,
  DISABLED,
  REVOKED,
  UNKNOWN
}
