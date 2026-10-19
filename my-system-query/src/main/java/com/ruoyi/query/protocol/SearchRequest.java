package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;

/** SearchRequest 数据约定。 */
public class SearchRequest {
  private Integer pageNum = 1;
  private Integer pageSize = 20;
  private Integer configVersion;
  private String sortField;
  private String sortDirection = "asc";
  private QueryNode filter;

  /** 返回 pageNum。 */
  public Integer getPageNum() {
    return pageNum;
  }

  /** 设置 pageNum。 */
  public void setPageNum(Integer value) {
    this.pageNum = value;
  }

  /** 返回 pageSize。 */
  public Integer getPageSize() {
    return pageSize;
  }

  /** 设置 pageSize。 */
  public void setPageSize(Integer value) {
    this.pageSize = value;
  }

  /** 返回 configVersion。 */
  public Integer getConfigVersion() {
    return configVersion;
  }

  /** 设置 configVersion。 */
  public void setConfigVersion(Integer value) {
    this.configVersion = value;
  }

  /** 返回 sortField。 */
  public String getSortField() {
    return sortField;
  }

  /** 设置 sortField。 */
  public void setSortField(String value) {
    this.sortField = value;
  }

  /** 返回 sortDirection。 */
  public String getSortDirection() {
    return sortDirection;
  }

  /** 设置 sortDirection。 */
  public void setSortDirection(String value) {
    this.sortDirection = value;
  }

  /** 返回 filter。 */
  public QueryNode getFilter() {
    return filter;
  }

  /** 设置 filter。 */
  public void setFilter(QueryNode value) {
    this.filter = value;
  }
}
