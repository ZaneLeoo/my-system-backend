package com.ruoyi.business.masterdata.category.vo;

import java.util.List;

/** 分类选择树节点，停用节点展示但不可选择。 */
public class CategoryTreeNode {
  /** 分类 ID。 */
  private Long id;

  /** 分类显示名称。 */
  private String label;

  /** 是否禁止选择。 */
  private boolean disabled;

  /** 子分类节点。 */
  private List<CategoryTreeNode> children = List.of();

  /** 创建供请求绑定和普通 Java 调用使用的空对象。 */
  public CategoryTreeNode() {}

  /** 使用完整字段创建对象，复用 Setter 的规范化规则。 */
  public CategoryTreeNode(
      Long id, String label, boolean disabled, List<CategoryTreeNode> children) {
    setId(id);
    setLabel(label);
    setDisabled(disabled);
    setChildren(children);
  }

  /** 返回分类 ID。 */
  public Long getId() {
    return id;
  }

  /** 设置分类 ID。 */
  public void setId(Long id) {
    this.id = id;
  }

  /** 返回分类显示名称。 */
  public String getLabel() {
    return label;
  }

  /** 设置分类显示名称。 */
  public void setLabel(String label) {
    this.label = label;
  }

  /** 返回是否禁止选择。 */
  public boolean isDisabled() {
    return disabled;
  }

  /** 设置是否禁止选择。 */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /** 返回子分类节点。 */
  public List<CategoryTreeNode> getChildren() {
    return children;
  }

  /** 设置子分类节点。 */
  public void setChildren(List<CategoryTreeNode> children) {
    this.children = children;
  }
}
