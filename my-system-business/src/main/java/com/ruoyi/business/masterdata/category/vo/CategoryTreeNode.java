package com.ruoyi.business.masterdata.category.vo;

import java.util.List;

/** 上级分类选择节点，停用节点保留展示但不可选择。 */
public record CategoryTreeNode(
    Long id, String label, boolean disabled, List<CategoryTreeNode> children) {}
