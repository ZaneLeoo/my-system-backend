package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;

/** SaveDefinitionRequest 数据约定。 */
public class SaveDefinitionRequest {
  private Integer version;
  private String displayName;
  private Boolean enabled = true;
  private List<SaveFieldRequest> fields = new ArrayList<>();

  /** 返回 version。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置 version。 */
  public void setVersion(Integer value) {
    this.version = value;
  }

  /** 返回 displayName。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置 displayName。 */
  public void setDisplayName(String value) {
    this.displayName = value;
  }

  /** 返回 enabled。 */
  public Boolean getEnabled() {
    return enabled;
  }

  /** 设置 enabled。 */
  public void setEnabled(Boolean value) {
    this.enabled = value;
  }

  /** 返回 fields。 */
  public List<SaveFieldRequest> getFields() {
    return fields;
  }

  /** 设置 fields。 */
  public void setFields(List<SaveFieldRequest> value) {
    this.fields = value;
  }
}
