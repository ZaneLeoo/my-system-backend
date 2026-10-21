package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 由宿主生成的业务内容快照。 */
@Getter
@Setter
public class BusinessSnapshot {
  /** 默认合同标题。 */
  private String title;

  /** 允许映射的业务字段与明细。 */
  private Map<String, Object> values = new LinkedHashMap<String, Object>();
}
