package com.ruoyi.common.core.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 新业务的受控分页参数，不接受任意 SQL 排序表达式。 */
public class PageQuery {
  @NotNull(message = "页码不能为空")
  @Min(value = 1, message = "页码必须大于零")
  private Integer pageNum = 1;

  @NotNull(message = "每页条数不能为空")
  @Min(value = 1, message = "每页条数必须大于零")
  @Max(value = 100, message = "每页最多查询 100 条")
  private Integer pageSize = 20;

  /** 返回当前页码。 */
  public Integer getPageNum() {
    return pageNum;
  }

  /** 设置当前页码。 */
  public void setPageNum(Integer pageNum) {
    this.pageNum = pageNum;
  }

  /** 返回每页条数。 */
  public Integer getPageSize() {
    return pageSize;
  }

  /** 设置每页条数。 */
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }
}
