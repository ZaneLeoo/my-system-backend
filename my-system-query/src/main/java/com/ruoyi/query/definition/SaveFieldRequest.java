package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;

/** 查询字段写入参数，不绑定审计信息。 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class SaveFieldRequest {
  private Long fieldId;

  private Long definitionId;
  private String fieldKey;
  private String sourceKey;
  private Long columnId;
  private String label;
  private String component;
  private String operatorsJson;
  private String defaultOperator;
  private String optionProvider;
  private Boolean commonVisible;
  private Integer displayOrder;
  private Boolean sortable;
  private Boolean enabled;

  /** 返回 fieldId。 */
  public Long getFieldId() {
    return fieldId;
  }

  /** 设置 fieldId。 */
  public void setFieldId(Long value) {
    this.fieldId = value;
  }

  /** 返回 definitionId。 */
  public Long getDefinitionId() {
    return definitionId;
  }

  /** 设置 definitionId。 */
  public void setDefinitionId(Long value) {
    this.definitionId = value;
  }

  /** 返回 fieldKey。 */
  public String getFieldKey() {
    return fieldKey;
  }

  /** 设置 fieldKey。 */
  public void setFieldKey(String value) {
    this.fieldKey = value;
  }

  /** 返回 sourceKey。 */
  public String getSourceKey() {
    return sourceKey;
  }

  /** 设置 sourceKey。 */
  public void setSourceKey(String value) {
    this.sourceKey = value;
  }

  /** 返回 columnId。 */
  public Long getColumnId() {
    return columnId;
  }

  /** 设置 columnId。 */
  public void setColumnId(Long value) {
    this.columnId = value;
  }

  /** 返回 label。 */
  public String getLabel() {
    return label;
  }

  /** 设置 label。 */
  public void setLabel(String value) {
    this.label = value;
  }

  /** 返回 component。 */
  public String getComponent() {
    return component;
  }

  /** 设置 component。 */
  public void setComponent(String value) {
    this.component = value;
  }

  /** 返回 operatorsJson。 */
  public String getOperatorsJson() {
    return operatorsJson;
  }

  /** 设置 operatorsJson。 */
  public void setOperatorsJson(String value) {
    this.operatorsJson = value;
  }

  /** 返回 defaultOperator。 */
  public String getDefaultOperator() {
    return defaultOperator;
  }

  /** 设置 defaultOperator。 */
  public void setDefaultOperator(String value) {
    this.defaultOperator = value;
  }

  /** 返回 optionProvider。 */
  public String getOptionProvider() {
    return optionProvider;
  }

  /** 设置 optionProvider。 */
  public void setOptionProvider(String value) {
    this.optionProvider = value;
  }

  /** 返回 commonVisible。 */
  public Boolean getCommonVisible() {
    return commonVisible;
  }

  /** 设置 commonVisible。 */
  public void setCommonVisible(Boolean value) {
    this.commonVisible = value;
  }

  /** 返回 displayOrder。 */
  public Integer getDisplayOrder() {
    return displayOrder;
  }

  /** 设置 displayOrder。 */
  public void setDisplayOrder(Integer value) {
    this.displayOrder = value;
  }

  /** 返回 sortable。 */
  public Boolean getSortable() {
    return sortable;
  }

  /** 设置 sortable。 */
  public void setSortable(Boolean value) {
    this.sortable = value;
  }

  /** 返回 enabled。 */
  public Boolean getEnabled() {
    return enabled;
  }

  /** 设置 enabled。 */
  public void setEnabled(Boolean value) {
    this.enabled = value;
  }
}
