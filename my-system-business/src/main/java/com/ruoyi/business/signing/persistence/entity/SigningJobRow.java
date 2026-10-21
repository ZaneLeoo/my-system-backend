package com.ruoyi.business.signing.persistence.entity;

import com.ruoyi.signing.model.SigningJob;
import lombok.Getter;
import lombok.Setter;

/** 数据库领取结果，状态不进入公共任务请求。 */
@Getter
@Setter
public class SigningJobRow extends SigningJob {
  /** 持久化执行状态。 */
  private String status;
}
