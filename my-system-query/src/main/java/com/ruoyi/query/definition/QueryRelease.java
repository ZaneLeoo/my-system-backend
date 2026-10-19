package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;

/** QueryRelease 数据约定。 */
@TableName("sys_query_release")
public class QueryRelease extends AuditEntity {
  @TableId(type = IdType.AUTO)
  private Long releaseId;

  private Long definitionId;
  private Integer revision;
  private String configSnapshot;
  private String structureSignature;

  /** 返回 releaseId。 */
  public Long getReleaseId() {
    return releaseId;
  }

  /** 设置 releaseId。 */
  public void setReleaseId(Long value) {
    this.releaseId = value;
  }

  /** 返回 definitionId。 */
  public Long getDefinitionId() {
    return definitionId;
  }

  /** 设置 definitionId。 */
  public void setDefinitionId(Long value) {
    this.definitionId = value;
  }

  /** 返回 revision。 */
  public Integer getRevision() {
    return revision;
  }

  /** 设置 revision。 */
  public void setRevision(Integer value) {
    this.revision = value;
  }

  /** 返回 configSnapshot。 */
  public String getConfigSnapshot() {
    return configSnapshot;
  }

  /** 设置 configSnapshot。 */
  public void setConfigSnapshot(String value) {
    this.configSnapshot = value;
  }

  /** 返回 structureSignature。 */
  public String getStructureSignature() {
    return structureSignature;
  }

  /** 设置 structureSignature。 */
  public void setStructureSignature(String value) {
    this.structureSignature = value;
  }
}
