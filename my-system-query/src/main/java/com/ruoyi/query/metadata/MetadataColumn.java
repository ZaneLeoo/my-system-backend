package com.ruoyi.query.metadata;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import lombok.Getter;
import lombok.Setter;

/** MetadataColumn 数据约定。 */
@Getter
@Setter
@TableName("sys_metadata_column")
public class MetadataColumn extends AuditEntity {
  /** 元数据字段 ID，用于校验字段来源及结构变化。 */
  @TableId(type = IdType.AUTO)
  private Long columnId;

  /** 所属表目录 ID。 */
  private Long tableId;

  /** 数据库中的物理字段名。 */
  private String columnName;

  /** 数据库原始字段注释，结构同步时读取。 */
  private String columnComment;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 查询框架识别的逻辑类型，例如 STRING、INTEGER 或 DECIMAL。 */
  private String dataType;

  /** 数据库完整物理类型，例如 varchar(64) 或 decimal(20,2)。 */
  private String columnType;

  /** 文本字段的最大字符长度，非文本字段可为空。 */
  private Long maxLength;

  /** 小数字段允许的小数位数，非小数字段可为空。 */
  private Integer numericScale;

  /** 数据库是否允许该字段为 NULL，决定能否使用空值运算符。 */
  private Boolean nullable;

  /** 是否为数据库主键字段。 */
  private Boolean primaryKey;

  /** 字段在数据库表中的物理顺序，从 1 开始。 */
  private Integer ordinalPosition;

  /** 对应的物理表或字段是否仍有效；失效目录保留以追踪配置引用。 */
  private Boolean available;

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
