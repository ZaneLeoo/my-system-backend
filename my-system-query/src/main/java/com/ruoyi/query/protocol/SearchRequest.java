package com.ruoyi.query.protocol;

import com.baomidou.mybatisplus.annotation.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** SearchRequest 数据约定。 */
@Getter
@Setter
public class SearchRequest {
  /** 页码，从 1 开始，默认 1。 */
  private Integer pageNum = 1;

  /** 每页条数，默认 20，允许范围为 1～100。 */
  private Integer pageSize = 20;

  /** 客户端读取的配置发布版本，必须与当前发布版本一致。 */
  private Integer configVersion;

  /** 已开放的稳定排序字段标识，不接受 SQL 表达式。 */
  private String sortField;

  /** 排序方向，取值为 asc 或 desc。 */
  private String sortDirection = "asc";

  /** 用户条件树；服务端强制条件在树外独立添加。 */
  private QueryNode filter;
}
