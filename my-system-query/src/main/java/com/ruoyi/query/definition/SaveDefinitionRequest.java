package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** SaveDefinitionRequest 数据约定。 */
@Getter
@Setter
public class SaveDefinitionRequest {
  /** 编辑时读取的草稿版本，用于整份配置的并发校验。 */
  private Integer version;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 是否启用当前查询对象或查询字段。 */
  private Boolean enabled = true;

  /** 本次保存的完整字段草稿；原有但未提交的字段会停用。 */
  private List<SaveFieldRequest> fields = new ArrayList<>();
}
