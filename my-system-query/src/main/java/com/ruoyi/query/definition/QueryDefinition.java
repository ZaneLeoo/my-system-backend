package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.core.domain.AuditEntity;
import java.time.*;
import java.util.*;

/** QueryDefinition 数据约定。 */
@TableName("sys_query_definition")
public class QueryDefinition extends AuditEntity {
  @TableId(type = IdType.AUTO)
  private Long definitionId;

  private String queryKey;
  private String displayName;
  private String adapterKey;
  private Integer draftVersion;
  private Integer publishedRevision;
  private Boolean enabled;

  /** 返回 definitionId。 */
  public Long getDefinitionId() {
    return definitionId;
  }

  /** 设置 definitionId。 */
  public void setDefinitionId(Long value) {
    this.definitionId = value;
  }

  /** 返回 queryKey。 */
  public String getQueryKey() {
    return queryKey;
  }

  /** 设置 queryKey。 */
  public void setQueryKey(String value) {
    this.queryKey = value;
  }

  /** 返回 displayName。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置 displayName。 */
  public void setDisplayName(String value) {
    this.displayName = value;
  }

  /** 返回 adapterKey。 */
  public String getAdapterKey() {
    return adapterKey;
  }

  /** 设置 adapterKey。 */
  public void setAdapterKey(String value) {
    this.adapterKey = value;
  }

  /** 返回 draftVersion。 */
  public Integer getDraftVersion() {
    return draftVersion;
  }

  /** 设置 draftVersion。 */
  public void setDraftVersion(Integer value) {
    this.draftVersion = value;
  }

  /** 返回 publishedRevision。 */
  public Integer getPublishedRevision() {
    return publishedRevision;
  }

  /** 设置 publishedRevision。 */
  public void setPublishedRevision(Integer value) {
    this.publishedRevision = value;
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
