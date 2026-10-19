package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;

/** FieldSpec 数据约定。 */
public class FieldSpec {
  private String fieldKey;
  private String sourceKey;
  private Long columnId;
  private String columnName;
  private String columnType;
  private String dataType;
  private Long maxLength;
  private Integer numericScale;
  private Boolean nullable;
  private String label;
  private String component;
  private List<String> operators = new ArrayList<>();
  private String defaultOperator;
  private String optionProvider;
  private Boolean commonVisible;
  private Integer displayOrder;
  private Boolean sortable;

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

  /** 返回 columnName。 */
  public String getColumnName() {
    return columnName;
  }

  /** 设置 columnName。 */
  public void setColumnName(String value) {
    this.columnName = value;
  }

  /** 返回 columnType。 */
  public String getColumnType() {
    return columnType;
  }

  /** 设置 columnType。 */
  public void setColumnType(String value) {
    this.columnType = value;
  }

  /** 返回 dataType。 */
  public String getDataType() {
    return dataType;
  }

  /** 设置 dataType。 */
  public void setDataType(String value) {
    this.dataType = value;
  }

  /** 返回 maxLength。 */
  public Long getMaxLength() {
    return maxLength;
  }

  /** 设置 maxLength。 */
  public void setMaxLength(Long value) {
    this.maxLength = value;
  }

  /** 返回 numericScale。 */
  public Integer getNumericScale() {
    return numericScale;
  }

  /** 设置 numericScale。 */
  public void setNumericScale(Integer value) {
    this.numericScale = value;
  }

  /** 返回 nullable。 */
  public Boolean getNullable() {
    return nullable;
  }

  /** 设置 nullable。 */
  public void setNullable(Boolean value) {
    this.nullable = value;
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

  /** 返回 operators。 */
  public List<String> getOperators() {
    return operators;
  }

  /** 设置 operators。 */
  public void setOperators(List<String> value) {
    this.operators = value;
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

  /** 从完整物理类型读取十进制精度，避免单独存储后与数据库类型不一致。 */
  public Integer getNumericPrecision() {
    if (columnType == null) {
      return null;
    }
    Matcher matcher =
        java.util.regex.Pattern.compile("(?i)decimal\\(([0-9]+),[0-9]+\\)").matcher(columnType);
    return matcher.matches() ? Integer.valueOf(matcher.group(1)) : null;
  }
}
