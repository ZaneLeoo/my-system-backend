package com.ruoyi.business.signing.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 版本化签署命令，禁止客户端直接更新状态。 */
@Getter
@Setter
public class SigningActionRequest {
  /** 页面读取版本。 */
  @NotNull @PositiveOrZero private Long version;

  /** 撤销原因，其他操作可为空。 */
  @Size(max = 500)
  private String reason;

  /** 人工核对的平台流程 ID。 */
  @Size(max = 100)
  private String flowId;
}
