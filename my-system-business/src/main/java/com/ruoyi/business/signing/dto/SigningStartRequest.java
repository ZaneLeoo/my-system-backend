package com.ruoyi.business.signing.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 发起时明确提交我方签署方式，不接受客户端印章名称或归属。 */
@Getter
@Setter
public class SigningStartRequest {
  /** 页面读取的任务版本。 */
  @NotNull @PositiveOrZero private Long version;

  /** 本次是否使用我方机构印章自动落章，必须明确指定。 */
  @NotNull private Boolean autoSign;

  /** 我方选定的印章编号，手动签署可空。 */
  @Size(max = 100)
  private String sealId;
}
