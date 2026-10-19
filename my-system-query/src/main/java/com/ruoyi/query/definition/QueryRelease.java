package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** QueryRelease 数据约定。 */
@Getter
@Setter
@TableName("sys_query_release")
public class QueryRelease extends AuditEntity {
  /** 不可变查询发布记录 ID。 */
  @TableId(type = IdType.AUTO)
  private Long releaseId;

  /** 所属查询定义 ID。 */
  private Long definitionId;

  /** 本次发布版本号，同一查询定义内唯一且递增。 */
  private Integer revision;

  /** 发布时冻结的完整查询配置 JSON。 */
  private String configSnapshot;

  /** 发布字段 ID 和物理类型组成的结构签名。 */
  private String structureSignature;
}
