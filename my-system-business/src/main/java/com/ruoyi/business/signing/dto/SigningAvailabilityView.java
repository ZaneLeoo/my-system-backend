package com.ruoyi.business.signing.dto;

import com.ruoyi.signing.model.enums.SigningStatus;
import lombok.Getter;
import lombok.Setter;

/** 单据发起电子签署前的名额检查结果。 */
@Getter
@Setter
public class SigningAvailabilityView {
  /** 当前单据是否允许创建新的签署草稿。 */
  private boolean available;

  /** 已占用签署名额的任务 ID；允许创建时为空。 */
  private String taskId;

  /** 已占用签署名额的任务状态；允许创建时为空。 */
  private SigningStatus status;
}
