package com.ruoyi.common.constant;

/** 新业务统一使用的逻辑删除标记，与若依既有数据库约定一致。 */
public final class LogicDeleteConstants {
  /** 正常记录。 */
  public static final String NOT_DELETED = "0";

  /** 已逻辑删除的记录。 */
  public static final String DELETED = "2";

  /** 常量类不允许实例化。 */
  private LogicDeleteConstants() {}
}
