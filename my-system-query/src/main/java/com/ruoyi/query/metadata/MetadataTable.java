package com.ruoyi.query.metadata;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** MetadataTable 数据约定。 */
@Getter
@Setter
@TableName("sys_metadata_table")
public class MetadataTable extends AuditEntity {
  /** 元数据表目录 ID。 */
  @TableId(type = IdType.AUTO)
  private Long tableId;

  /** 当前应用使用的数据库名称。 */
  private String schemaName;

  /** 开发者登记的数据库物理表名。 */
  private String tableName;

  /** 数据库原始表注释，中文显示名称可单独维护。 */
  private String tableComment;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 有序物理字段结构的 SHA-256 签名，用于确认同步内容。 */
  private String structureSignature;

  /** 对应的物理表或字段是否仍有效；失效目录保留以追踪配置引用。 */
  private Boolean available;

  /** 最近一次导入或同步目录的北京时间。 */
  private LocalDateTime synchronizedTime;

  /** 目录编辑版本，用于同步及名称维护的并发检查。 */
  private Integer version;
}
