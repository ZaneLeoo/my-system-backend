package com.ruoyi.common.core.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 新业务的受控分页参数，不接受任意 SQL 排序表达式。 */
@Getter
@Setter
public class PageQuery {
  /** 页码，从 1 开始，默认 1。 */
  @NotNull(message = "页码不能为空")
  @Min(value = 1, message = "页码必须大于零")
  private Integer pageNum = 1;

  /** 每页条数，默认 20，允许范围为 1～100。 */
  @NotNull(message = "每页条数不能为空")
  @Min(value = 1, message = "每页条数必须大于零")
  @Max(value = 100, message = "每页最多查询 100 条")
  private Integer pageSize = 20;
}
