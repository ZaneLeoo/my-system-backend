package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 允许对模板开放的业务字段。 */
@Getter
@Setter
public class SigningField {
  /** 稳定业务字段标识。 */
  private String key;

  /** 中文名称。 */
  private String label;

  /** TEXT、NUMBER、DATE 或 TABLE。 */
  private String type;

  /** TABLE 的可映射明细列。 */
  private List<SigningField> columns = new ArrayList<SigningField>();
}
