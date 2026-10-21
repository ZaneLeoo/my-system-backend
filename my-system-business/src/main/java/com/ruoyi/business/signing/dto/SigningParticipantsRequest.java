package com.ruoyi.business.signing.dto;

import com.ruoyi.signing.model.SigningParty;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 维护双方并显式确认当前文件上的位置。 */
@Getter
@Setter
public class SigningParticipantsRequest {
  /** 当前任务版本。 */
  @NotNull @PositiveOrZero private Long version;

  /** 我方和对方两个签署方；对方可选择企业／机构或个人。 */
  @NotNull
  @Size(min = 2, max = 2)
  private List<SigningParty> parties;

  /** 是否确认坐标对应最终文件。 */
  private boolean confirmPositions;
}
