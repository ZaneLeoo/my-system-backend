package com.ruoyi.business.masterdata.category.vo;

import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import java.time.LocalDateTime;

/** 分类详情与平铺树表响应，与持久化基类独立。 */
public class CategoryView {
  /** 分类 ID。 */
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

  /** 创建账号。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改账号。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 是否真正匹配筛选条件，false 表示层级上下文。 */
  private boolean matched;

  /** 创建供请求绑定和普通 Java 调用使用的空对象。 */
  public CategoryView() {}

  /** 使用完整字段创建对象，复用 Setter 的规范化规则。 */
  public CategoryView(
      Long categoryId,
      Long parentId,
      String categoryCode,
      String categoryName,
      Integer sortOrder,
      CategoryStatus status,
      String remark,
      Integer version,
      String createBy,
      LocalDateTime createTime,
      String updateBy,
      LocalDateTime updateTime,
      boolean matched) {
    setCategoryId(categoryId);
    setParentId(parentId);
    setCategoryCode(categoryCode);
    setCategoryName(categoryName);
    setSortOrder(sortOrder);
    setStatus(status);
    setRemark(remark);
    setVersion(version);
    setCreateBy(createBy);
    setCreateTime(createTime);
    setUpdateBy(updateBy);
    setUpdateTime(updateTime);
    setMatched(matched);
  }

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

  /** 返回是否真正匹配筛选条件，false 表示层级上下文。 */
  public boolean isMatched() {
    return matched;
  }

  /** 设置是否真正匹配筛选条件，false 表示层级上下文。 */
  public void setMatched(boolean matched) {
    this.matched = matched;
  }

  /** 从实体转换为只读接口数据，不暴露逻辑删除标记。 */
  public static CategoryView from(ItemCategory category, boolean matched) {
    return new CategoryView(
        category.getCategoryId(),
        category.getParentId(),
        category.getCategoryCode(),
        category.getCategoryName(),
        category.getSortOrder(),
        category.getStatus(),
        category.getRemark(),
        category.getVersion(),
        category.getCreateBy(),
        category.getCreateTime(),
        category.getUpdateBy(),
        category.getUpdateTime(),
        matched);
  }
}
