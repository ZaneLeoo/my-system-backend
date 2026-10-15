package com.ruoyi.common.core.page;

import java.util.List;

/** 新业务分页数据，由 R 包装状态码和消息。 */
public class PageResult<T> {
  private List<T> rows = List.of();
  private long total;

  /** 创建空分页结果，供普通 JavaBean 使用。 */
  public PageResult() {}

  /** 使用当前页数据与过滤后的总记录数创建结果。 */
  public PageResult(List<T> rows, long total) {
    this.rows = rows;
    this.total = total;
  }

  /** 返回当前页数据。 */
  public List<T> getRows() {
    return rows;
  }

  /** 设置当前页数据。 */
  public void setRows(List<T> rows) {
    this.rows = rows;
  }

  /** 返回过滤后的总记录数。 */
  public long getTotal() {
    return total;
  }

  /** 设置过滤后的总记录数。 */
  public void setTotal(long total) {
    this.total = total;
  }
}
