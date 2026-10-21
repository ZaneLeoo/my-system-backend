package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 回调收件箱和操作事件。 */
@Getter
@Setter
public class SigningEvent {
  /** 本系统记录或接收事件的北京时间，并非平台实际签署时间。 */
  private String createTime;

  /** 事件 ID。 */
  private String id;

  /** 所属任务，可在消费阶段关联。 */
  private String taskId;

  /** 已验签原始事件的去重摘要。 */
  private String deduplicationKey;

  /** 事件类型。 */
  private String kind;

  /** 受保护的事件内容，不写日志。 */
  private String payload;

  /** 用户或技术账号。 */
  private String actor;
}
