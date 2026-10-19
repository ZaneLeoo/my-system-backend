package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** QuerySnapshot 数据约定。 */
@Getter
@Setter
public class QuerySnapshot {
  /** 该快照对应的查询对象是否启用。 */
  private Boolean enabled = true;

  /** 稳定业务查询编码，供运行时和前端引用。 */
  private String queryKey;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 当前查询配置发布版本，用于拒绝旧页面请求。 */
  private Integer configVersion;

  /** 发布或草稿校验后开放的完整字段定义。 */
  private List<FieldSpec> fields = new ArrayList<>();

  /** 已登记关联标识与中文名称的映射，供高级查询展示。 */
  private Map<String, String> relations = new LinkedHashMap<>();
}
