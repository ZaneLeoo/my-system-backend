package com.ruoyi.signing.api.result;

import com.ruoyi.signing.model.enums.SigningStatus;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 平台无关的权威签署结果。 */
@Getter
@Setter
public class SigningFlowResult {
  /** 公共流程状态。 */
  private SigningStatus status;

  /** 各本地角色的签署状态。 */
  private Map<String, String> partyStatuses = new LinkedHashMap<String, String>();

  /** 流程中已绑定文件 ID，用于人工核对归属。 */
  private List<String> fileIds = new ArrayList<String>();
}
