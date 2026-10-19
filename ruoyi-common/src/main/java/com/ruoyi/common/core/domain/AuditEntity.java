package com.ruoyi.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 新业务持久化审计基类；不包含查询条件或业务字段。 */
@Getter
@Setter
public abstract class AuditEntity {
  /** 创建账号。 */
  @TableField(fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
  private String createBy;

  /** 创建时间。 */
  @TableField(fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
  private LocalDateTime createTime;

  /** 修改账号。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  /** 修改时间。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
