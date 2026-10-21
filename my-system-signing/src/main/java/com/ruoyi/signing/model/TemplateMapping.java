package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 标量或动态明细表的映射。 */
@Getter
@Setter
public class TemplateMapping {
  /** 平台控件 ID。 */
  private String componentId;

  /** 控件类型，用于检测模板变化。 */
  private String componentType;

  /** 开发者登记的稳定业务字段键。 */
  private String fieldKey;

  /** 动态表列标识到明细字段键的映射。 */
  private Map<String, String> columns = new LinkedHashMap<String, String>();
}
