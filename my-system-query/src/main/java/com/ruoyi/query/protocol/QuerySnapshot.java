package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;

/** QuerySnapshot 数据约定。 */
public class QuerySnapshot {
  private Boolean enabled = true;

  /** 返回启用状态。 */
  public Boolean getEnabled() {
    return enabled;
  }

  /** 设置启用状态。 */
  public void setEnabled(Boolean value) {
    enabled = value;
  }

  private String queryKey;
  private String displayName;
  private Integer configVersion;
  private List<FieldSpec> fields = new ArrayList<>();
  private Map<String, String> relations = new LinkedHashMap<>();

  /** 返回 queryKey。 */
  public String getQueryKey() {
    return queryKey;
  }

  /** 设置 queryKey。 */
  public void setQueryKey(String value) {
    this.queryKey = value;
  }

  /** 返回 displayName。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置 displayName。 */
  public void setDisplayName(String value) {
    this.displayName = value;
  }

  /** 返回 configVersion。 */
  public Integer getConfigVersion() {
    return configVersion;
  }

  /** 设置 configVersion。 */
  public void setConfigVersion(Integer value) {
    this.configVersion = value;
  }

  /** 返回 fields。 */
  public List<FieldSpec> getFields() {
    return fields;
  }

  /** 设置 fields。 */
  public void setFields(List<FieldSpec> value) {
    this.fields = value;
  }

  /** 返回 relations。 */
  public Map<String, String> getRelations() {
    return relations;
  }

  /** 设置 relations。 */
  public void setRelations(Map<String, String> value) {
    this.relations = value;
  }
}
