package com.ruoyi.business.masterdata.category.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;

/** 存货分类实体，继承公共审计与逻辑删除约定。 */
@TableName("md_item_category")
public class ItemCategory extends BusinessEntity {
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
}
