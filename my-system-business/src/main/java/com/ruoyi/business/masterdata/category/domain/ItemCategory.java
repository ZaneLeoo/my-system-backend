package com.ruoyi.business.masterdata.category.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/** 存货分类持久化实体；不继承含查询辅助字段的旧 BaseEntity。 */
@TableName("md_item_category")
public class ItemCategory {
  /** 分类 ID。 */
  @TableId(type = IdType.AUTO)
  private Long categoryId;

  /** 上级分类 ID。 */
  private Long parentId;

  /** 分类编码。 */
  private String categoryCode;

  /** 分类名称。 */
  private String categoryName;

  /** 显示顺序。 */
  private Integer sortOrder;

  /** 启停状态。 */
  private CategoryStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;

  /** 逻辑删除标记。 */
  @TableLogic(value = CategoryConstants.NORMAL, delval = CategoryConstants.DELETED)
  private String delFlag;

  /** 创建账号。 */
  @TableField(fill = FieldFill.INSERT)
  private String createBy;

  /** 创建时间。 */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /** 修改账号。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  /** 修改时间。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  /** 返回分类 ID。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置分类 ID。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回上级分类 ID。 */
  public Long getParentId() {
    return parentId;
  }

  /** 设置上级分类 ID。 */
  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  /** 返回分类编码。 */
  public String getCategoryCode() {
    return categoryCode;
  }

  /** 设置分类编码。 */
  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
  }

  /** 返回分类名称。 */
  public String getCategoryName() {
    return categoryName;
  }

  /** 设置分类名称。 */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  /** 返回显示顺序。 */
  public Integer getSortOrder() {
    return sortOrder;
  }

  /** 设置显示顺序。 */
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  /** 返回启停状态。 */
  public CategoryStatus getStatus() {
    return status;
  }

  /** 设置启停状态。 */
  public void setStatus(CategoryStatus status) {
    this.status = status;
  }

  /** 返回备注。 */
  public String getRemark() {
    return remark;
  }

  /** 设置备注。 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 返回编辑版本号。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置编辑版本号。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回逻辑删除标记。 */
  public String getDelFlag() {
    return delFlag;
  }

  /** 设置逻辑删除标记。 */
  public void setDelFlag(String delFlag) {
    this.delFlag = delFlag;
  }

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
