package com.ruoyi.query.metadata;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.Size;

/** 数据目录分页查询，关键字匹配中文名称或数据库表名。 */
public class MetadataTableQuery extends PageQuery {
  @Size(max = 200, message = "查询关键字最多 200 个字符")
  private String keyword;

  /** 返回已清理首尾空白的关键字。 */
  public String getKeyword() {
    return keyword;
  }

  /** 将空白输入统一为空条件。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }
}
