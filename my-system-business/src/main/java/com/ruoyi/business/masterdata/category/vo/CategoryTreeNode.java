package com.ruoyi.business.masterdata.category.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 分类选择树节点，停用节点展示但不可选择。 */
@Getter
@Setter
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
}
