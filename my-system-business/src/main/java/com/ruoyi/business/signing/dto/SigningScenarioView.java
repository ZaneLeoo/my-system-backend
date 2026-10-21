package com.ruoyi.business.signing.dto;

import lombok.Getter;
import lombok.Setter;

/** 已登记签署场景及访问能力。 */
@Getter
@Setter
public class SigningScenarioView {
  /** 稳定业务编码。 */
  private String businessType;

  /** 中文名称。 */
  private String name;

  /** 是否具备来源查询权限。 */
  private boolean accessible;
}
