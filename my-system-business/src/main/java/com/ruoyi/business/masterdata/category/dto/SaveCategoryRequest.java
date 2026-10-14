package com.ruoyi.business.masterdata.category.dto;

import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import jakarta.validation.constraints.*;

/** 分类保存参数；客户端不能指定审计字段和删除标记。 */
public class SaveCategoryRequest {
  /** 分类 ID，新增时不指定。 */
  @Positive private Long categoryId;

  /** 上级分类 ID，0 为顶级。 */
  @NotNull @PositiveOrZero private Long parentId;

  /** 分类编码。 */
  @NotBlank
  @Size(max = 64)
  private String categoryCode;

  /** 分类名称。 */
  @NotBlank
  @Size(max = 100)
  private String categoryName;

  /** 显示顺序。 */
  @NotNull @PositiveOrZero private Integer sortOrder;

  /** 启停状态。 */
  @NotNull private CategoryStatus status;

  /** 备注。 */
  @Size(max = 500)
  private String remark = "";

  /** 编辑版本号，新增时不指定。 */
  @PositiveOrZero private Integer version;

  /** 创建供请求绑定和普通 Java 调用使用的空对象。 */
  public SaveCategoryRequest() {}

  /** 使用完整字段创建对象，复用 Setter 的规范化规则。 */
  public SaveCategoryRequest(
      Long categoryId,
      Long parentId,
      String categoryCode,
      String categoryName,
      Integer sortOrder,
      CategoryStatus status,
      String remark,
      Integer version) {
    setCategoryId(categoryId);
    setParentId(parentId);
    setCategoryCode(categoryCode);
    setCategoryName(categoryName);
    setSortOrder(sortOrder);
    setStatus(status);
    setRemark(remark);
    setVersion(version);
  }

  /** 返回分类 ID，新增时不指定。 */
  public Long getCategoryId() {
    return categoryId;
  }

  /** 设置分类 ID，新增时不指定。 */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** 返回上级分类 ID，0 为顶级。 */
  public Long getParentId() {
    return parentId;
  }

  /** 设置上级分类 ID，0 为顶级。 */
  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  /** 返回分类编码。 */
  public String getCategoryCode() {
    return categoryCode;
  }

  /** 设置分类编码，并清理首尾空白。 */
  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode == null ? null : categoryCode.strip();
  }

  /** 返回分类名称。 */
  public String getCategoryName() {
    return categoryName;
  }

  /** 设置分类名称，并清理首尾空白。 */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName == null ? null : categoryName.strip();
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

  /** 设置备注，将空值规范为无备注并清理首尾空白。 */
  public void setRemark(String remark) {
    this.remark = remark == null ? "" : remark.strip();
  }

  /** 返回编辑版本号，新增时不指定。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置编辑版本号，新增时不指定。 */
  public void setVersion(Integer version) {
    this.version = version;
  }
}
