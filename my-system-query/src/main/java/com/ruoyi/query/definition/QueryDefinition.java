package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** QueryDefinition 数据约定。 */
@Getter
@Setter
@TableName("sys_query_definition")
public class QueryDefinition extends AuditEntity {
  /** 所属查询定义 ID。 */
  @TableId(type = IdType.AUTO)
  private Long definitionId;

  /** 稳定业务查询编码，供运行时和前端引用。 */
  private String queryKey;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 后端已注册的查询适配器编码。 */
  private String adapterKey;

  /** 草稿编辑版本，用于拒绝并发修改后的过期保存和发布。 */
  private Integer draftVersion;

  /** 当前发布版本，0 表示尚未发布。 */
  private Integer publishedRevision;

  /** 是否启用当前查询对象或查询字段。 */
  private Boolean enabled;
}
