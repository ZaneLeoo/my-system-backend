package com.ruoyi.business.masterdata.category.domain;

/** 分类根节点及 MP 注解使用的删除标记。 */
public final class CategoryConstants {
  public static final long ROOT_ID = 0L;
  public static final String NORMAL = "0";
  public static final String DELETED = "2";

  /** 限制整棵子树深度，保证树展示和 JSON 序列化的合理边界。 */
  public static final int MAX_DEPTH = 32;

  /** 工具常量类不允许实例化。 */
  private CategoryConstants() {}
}
