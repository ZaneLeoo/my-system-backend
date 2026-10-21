package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 持久化执行任务。 */
@Getter
@Setter
public class SigningJob {
  /** 后台任务 ID。 */
  private String id;

  /** 签署任务 ID。 */
  private String taskId;

  /** 操作类型。 */
  private com.ruoyi.signing.model.enums.SigningJobType type;

  /** 本次领取令牌，防止租约过期后的旧写入。 */
  private String token;

  /** 尝试次数。 */
  private int attempt;
}
