package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;

/** QueryNode 数据约定。 */
public class QueryNode {
  private String type;
  private String logic;
  private String field;
  private String op;
  private Object value;
  private Object value2;
  private String relation;
  private String mode;
  private List<QueryNode> children = new ArrayList<>();

  /** 返回 type。 */
  public String getType() {
    return type;
  }

  /** 设置 type。 */
  public void setType(String value) {
    this.type = value;
  }

  /** 返回 logic。 */
  public String getLogic() {
    return logic;
  }

  /** 设置 logic。 */
  public void setLogic(String value) {
    this.logic = value;
  }

  /** 返回 field。 */
  public String getField() {
    return field;
  }

  /** 设置 field。 */
  public void setField(String value) {
    this.field = value;
  }

  /** 返回 op。 */
  public String getOp() {
    return op;
  }

  /** 设置 op。 */
  public void setOp(String value) {
    this.op = value;
  }

  /** 返回 value。 */
  public Object getValue() {
    return value;
  }

  /** 设置 value。 */
  public void setValue(Object value) {
    this.value = value;
  }

  /** 返回 value2。 */
  public Object getValue2() {
    return value2;
  }

  /** 设置 value2。 */
  public void setValue2(Object value) {
    this.value2 = value;
  }

  /** 返回 relation。 */
  public String getRelation() {
    return relation;
  }

  /** 设置 relation。 */
  public void setRelation(String value) {
    this.relation = value;
  }

  /** 返回 mode。 */
  public String getMode() {
    return mode;
  }

  /** 设置 mode。 */
  public void setMode(String value) {
    this.mode = value;
  }

  /** 返回 children。 */
  public List<QueryNode> getChildren() {
    return children;
  }

  /** 设置 children。 */
  public void setChildren(List<QueryNode> value) {
    this.children = value;
  }
}
