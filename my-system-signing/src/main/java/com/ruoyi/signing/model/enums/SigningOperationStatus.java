package com.ruoyi.signing.model.enums;

/** 签署领域的SigningOperationStatus取值，独立于平台数字状态。 */
public enum SigningOperationStatus {
  IDLE,
  QUEUED,
  RUNNING,
  FAILED,
  UNKNOWN
}
