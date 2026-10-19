package com.ruoyi.query.metadata;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;

/** MetadataTable 数据约定。 */
@TableName("sys_metadata_table")
public class MetadataTable extends AuditEntity {
  @TableId(type = IdType.AUTO)
  private Long tableId;

  private String schemaName;
  private String tableName;
  private String tableComment;
  private String displayName;
  private String structureSignature;
  private Boolean available;
  private LocalDateTime synchronizedTime;
  private Integer version;

  /** 返回 tableId。 */
  public Long getTableId() {
    return tableId;
  }

  /** 设置 tableId。 */
  public void setTableId(Long value) {
    this.tableId = value;
  }

  /** 返回 schemaName。 */
  public String getSchemaName() {
    return schemaName;
  }

  /** 设置 schemaName。 */
  public void setSchemaName(String value) {
    this.schemaName = value;
  }

  /** 返回 tableName。 */
  public String getTableName() {
    return tableName;
  }

  /** 设置 tableName。 */
  public void setTableName(String value) {
    this.tableName = value;
  }

  /** 返回 tableComment。 */
  public String getTableComment() {
    return tableComment;
  }

  /** 设置 tableComment。 */
  public void setTableComment(String value) {
    this.tableComment = value;
  }

  /** 返回 displayName。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置 displayName。 */
  public void setDisplayName(String value) {
    this.displayName = value;
  }

  /** 返回 structureSignature。 */
  public String getStructureSignature() {
    return structureSignature;
  }

  /** 设置 structureSignature。 */
  public void setStructureSignature(String value) {
    this.structureSignature = value;
  }

  /** 返回 available。 */
  public Boolean getAvailable() {
    return available;
  }

  /** 设置 available。 */
  public void setAvailable(Boolean value) {
    this.available = value;
  }

  /** 返回 synchronizedTime。 */
  public LocalDateTime getSynchronizedTime() {
    return synchronizedTime;
  }

  /** 设置 synchronizedTime。 */
  public void setSynchronizedTime(LocalDateTime value) {
    this.synchronizedTime = value;
  }

  /** 返回 version。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置 version。 */
  public void setVersion(Integer value) {
    this.version = value;
  }
}
