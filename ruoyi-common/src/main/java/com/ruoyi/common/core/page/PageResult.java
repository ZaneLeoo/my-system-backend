package com.ruoyi.common.core.page;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 新业务分页数据，由 R 包装状态码和消息。 */
@Getter
@Setter
public class PageResult<T> {
  /** 当前页记录，默认返回空列表。 */
  private List<T> rows = List.of();

  /** 应用全部筛选条件后的总记录数，不是当前页行数。 */
  private long total;

  /** 创建空分页结果，供普通 JavaBean 使用。 */
  public PageResult() {}

  /** 使用当前页数据与过滤后的总记录数创建结果。 */
  public PageResult(List<T> rows, long total) {
    this.rows = rows;
    this.total = total;
  }
}
