package com.ruoyi.query.definition;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 查询字段写入参数，不绑定审计信息。 */
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class SaveFieldRequest {
  /** 查询字段配置 ID。 */
  private Long fieldId;

  /** 查询定义 ID；保存时以接口路径指定的所属对象为准。 */
  private Long definitionId;

  /** 稳定字段标识，供条件树和后续查询方案引用。 */
  private String fieldKey;

  /** 受控来源分组标识，例如主表 main 或明细 lines。 */
  private String sourceKey;

  /** 元数据字段 ID，用于校验字段来源及结构变化。 */
  private Long columnId;

  /** 查询界面显示的中文字段名称。 */
  private String label;

  /** 与字段类型匹配的查询控件标识。 */
  private String component;

  /** 允许运算符的 JSON 数组，默认运算符必须包含在其中。 */
  private String operatorsJson;

  /** 常用查询区和基础筛选默认使用的运算符。 */
  private String defaultOperator;

  /** 后端已登记的引用或枚举选项来源，普通字段为空字符串。 */
  private String optionProvider;

  /** 是否在业务列表的常用查询区展示，仅适用于主表字段。 */
  private Boolean commonVisible;

  /** 查询控件的展示顺序，常用布局拖拽后按此顺序保存。 */
  private Integer displayOrder;

  /** 是否允许按该主表字段进行服务端排序。 */
  private Boolean sortable;

  /** 是否启用当前查询对象或查询字段。 */
  private Boolean enabled;
}
