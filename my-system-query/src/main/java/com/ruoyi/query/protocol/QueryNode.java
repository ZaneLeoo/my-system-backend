package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** QueryNode 数据约定。 */
@Getter
@Setter
public class QueryNode {
  /** 条件节点类型：condition 普通条件、group 逻辑组、relation 关联组。 */
  private String type;

  /** 组内组合方式，取值为 AND 或 OR。 */
  private String logic;

  /** 当前查询配置中的稳定字段标识，不是物理列名。 */
  private String field;

  /** 当前字段允许的运算符，例如 eq、like 或 between。 */
  private String op;

  /** 单值、区间起始值或集合值；小数使用十进制字符串。 */
  private Object value;

  /** 区间结束值，仅 between 等明确支持双值的操作使用。 */
  private Object value2;

  /** 适配器登记的关联来源标识，仅关联组使用。 */
  private String relation;

  /** 关联判断方式，取值为 EXISTS 或 NOT_EXISTS。 */
  private String mode;

  /** 逻辑组或关联组的子条件，顺序保留原始条件树结构。 */
  private List<QueryNode> children = new ArrayList<>();
}
