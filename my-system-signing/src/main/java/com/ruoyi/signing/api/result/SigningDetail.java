package com.ruoyi.signing.api.result;

import com.ruoyi.signing.model.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 通用服务内部详情；宿主需要转换文档及事件视图后对外返回。 */
@Getter
@Setter
public class SigningDetail {
  /** 最新任务。 */
  private SigningTask task;

  /** 私有文档元信息。 */
  private List<SigningDocument> documents;

  /** 当前签署方。 */
  private List<SigningParty> parties;

  /** 脱敏时间线。 */
  private List<SigningEvent> events;
}
