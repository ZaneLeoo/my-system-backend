package com.ruoyi.query.metadata;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;

/** MetadataColumn 数据约定。 */
@TableName("sys_metadata_column")
public class MetadataColumn extends AuditEntity {
  @TableId(type = IdType.AUTO)
  private Long columnId;

  private Long tableId;
  private String columnName;
  private String columnComment;
  private String displayName;
  private String dataType;
  private String columnType;
  private Long maxLength;
  private Integer numericScale;
  private Boolean nullable;
  private Boolean primaryKey;
  private Integer ordinalPosition;
  private Boolean available;

  /** 返回 columnId。 */
  public Long getColumnId() {
    return columnId;
  }

  /** 设置 columnId。 */
  public void setColumnId(Long value) {
    this.columnId = value;
  }

  /** 返回 tableId。 */
  public Long getTableId() {
    return tableId;
  }

  /** 设置 tableId。 */
  public void setTableId(Long value) {
    this.tableId = value;
  }

  /** 返回 columnName。 */
  public String getColumnName() {
    return columnName;
  }

  /** 设置 columnName。 */
  public void setColumnName(String value) {
    this.columnName = value;
  }

  /** 返回 columnComment。 */
  public String getColumnComment() {
    return columnComment;
  }

  /** 设置 columnComment。 */
  public void setColumnComment(String value) {
    this.columnComment = value;
  }

  /** 返回 displayName。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置 displayName。 */
  public void setDisplayName(String value) {
    this.displayName = value;
  }

  /** 返回 dataType。 */
  public String getDataType() {
    return dataType;
  }

  /** 设置 dataType。 */
  public void setDataType(String value) {
    this.dataType = value;
  }

  /** 返回 columnType。 */
  public String getColumnType() {
    return columnType;
  }

  /** 设置 columnType。 */
  public void setColumnType(String value) {
    this.columnType = value;
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

  /** 返回 primaryKey。 */
  public Boolean getPrimaryKey() {
    return primaryKey;
  }

  /** 设置 primaryKey。 */
  public void setPrimaryKey(Boolean value) {
    this.primaryKey = value;
  }

  /** 返回 ordinalPosition。 */
  public Integer getOrdinalPosition() {
    return ordinalPosition;
  }

  /** 设置 ordinalPosition。 */
  public void setOrdinalPosition(Integer value) {
    this.ordinalPosition = value;
  }

  /** 返回 available。 */
  public Boolean getAvailable() {
    return available;
  }

  /** 设置 available。 */
  public void setAvailable(Boolean value) {
    this.available = value;
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
