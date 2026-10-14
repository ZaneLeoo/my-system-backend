package com.ruoyi.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

/** 新业务持久化审计基类；不包含查询条件或业务字段。 */
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

  /** 返回创建账号。 */
  public String getCreateBy() {
    return createBy;
  }

  /** 设置创建账号。 */
  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  /** 返回创建时间。 */
  public LocalDateTime getCreateTime() {
    return createTime;
  }

  /** 设置创建时间。 */
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  /** 返回修改账号。 */
  public String getUpdateBy() {
    return updateBy;
  }

  /** 设置修改账号。 */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  /** 返回修改时间。 */
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  /** 设置修改时间。 */
  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }
}
