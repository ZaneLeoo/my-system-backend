package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 本地模板业务映射，不复制平台编辑器。 */
@Getter
@Setter
public class SigningTemplateBinding {
  /** 创建配置的真实用户。 */
  private String createBy;

  /** 北京时间创建时间。 */
  private String createTime;

  /** 最近维护配置的真实用户。 */
  private String updateBy;

  /** 北京时间修改时间。 */
  private String updateTime;

  /** 本地模板绑定 ID。 */
  private String id;

  /** 业务场景。 */
  private String businessType;

  /** 用户看到的模板名称。 */
  private String name;

  /** 平台应用环境。 */
  private String providerEnvironment;

  /** 平台模板 ID。 */
  private String templateId;

  /** 是否允许选择。 */
  private boolean enabled = true;

  /** 并发版本。 */
  private long version;

  /** 控件与业务字段映射。 */
  private List<TemplateMapping> mappings = new ArrayList<TemplateMapping>();
}
