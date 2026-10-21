package com.ruoyi.signing.model.enums;

/** 签署领域的SigningJobType取值，独立于平台数字状态。 */
public enum SigningJobType {
  PREPARE,
  CREATE_FLOW,
  START_FLOW,
  SYNC,
  REVOKE,
  ARCHIVE
}
