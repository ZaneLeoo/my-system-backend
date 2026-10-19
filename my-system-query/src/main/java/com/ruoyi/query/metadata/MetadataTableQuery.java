package com.ruoyi.query.metadata;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/** 数据目录分页查询，关键字匹配中文名称或数据库表名。 */
@Getter
public class MetadataTableQuery extends PageQuery {
  /** 编码、名称或规格的搜索关键字，空白按当前查询规则处理。 */
  @Size(max = 200, message = "查询关键字最多 200 个字符")
  private String keyword;

  /** 将空白输入统一为空条件。 */
  public void setKeyword(String keyword) {
    this.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
  }
}
