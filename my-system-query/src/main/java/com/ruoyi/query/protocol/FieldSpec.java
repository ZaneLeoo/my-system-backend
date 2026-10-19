package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import lombok.Getter;
import lombok.Setter;

/** FieldSpec 数据约定。 */
@Getter
@Setter
public class FieldSpec {
  /** 稳定字段标识，供条件树和后续查询方案引用。 */
  private String fieldKey;

  /** 受控来源分组标识，例如主表 main 或明细 lines。 */
  private String sourceKey;

  /** 元数据字段 ID，用于校验字段来源及结构变化。 */
  private Long columnId;

  /** 数据库中的物理字段名。 */
  private String columnName;

  /** 数据库完整物理类型，例如 varchar(64) 或 decimal(20,2)。 */
  private String columnType;

  /** 查询框架识别的逻辑类型，例如 STRING、INTEGER 或 DECIMAL。 */
  private String dataType;

  /** 文本字段的最大字符长度，非文本字段可为空。 */
  private Long maxLength;

  /** 小数字段允许的小数位数，非小数字段可为空。 */
  private Integer numericScale;

  /** 数据库是否允许该字段为 NULL，决定能否使用空值运算符。 */
  private Boolean nullable;

  /** 查询界面显示的中文字段名称。 */
  private String label;

  /** 与字段类型匹配的查询控件标识。 */
  private String component;

  /** 当前发布字段允许使用的运算符集合。 */
  private List<String> operators = new ArrayList<>();

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

  /** 从完整物理类型读取十进制精度，避免单独存储后与数据库类型不一致。 */
  public Integer getNumericPrecision() {
    if (columnType == null) {
      return null;
    }
    Matcher matcher =
        java.util.regex.Pattern.compile("(?i)decimal\\(([0-9]+),[0-9]+\\)").matcher(columnType);
    return matcher.matches() ? Integer.valueOf(matcher.group(1)) : null;
  }
}
