package com.ruoyi.business.masterdata.category.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 分类树表查询参数，使用普通 JavaBean 属性绑定 URL 参数。 */
@Getter
@Setter
public class CategoryQuery {
  /** 分类编码筛选。 */
  @Size(max = 64)
  private String categoryCode;

  /** 分类名称筛选。 */
  @Size(max = 100)
  private String categoryName;

  /** 启停状态筛选。 */
  @Pattern(regexp = "[01]", message = "分类状态必须为 0 或 1")
  private String status;

  /** 创建供请求绑定和普通 Java 调用使用的空对象。 */
  public CategoryQuery() {}

  /** 使用完整字段创建对象，复用 Setter 的规范化规则。 */
  public CategoryQuery(String categoryCode, String categoryName, String status) {
    setCategoryCode(categoryCode);
    setCategoryName(categoryName);
    setStatus(status);
  }
}
