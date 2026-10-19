package com.ruoyi.query.web;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.query.engine.QueryRuntimeService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** 所有运行时方法通过业务适配器检查列表权限。 */
@RestController
@RequestMapping("/query/runtime/{queryKey}")
public class QueryRuntimeController {
  private final QueryRuntimeService service;

  /** 注入业务授权后的运行时服务。 */
  public QueryRuntimeController(QueryRuntimeService service) {
    this.service = service;
  }

  /** 输出安全字段元数据。 */
  @GetMapping("/metadata")
  public R<?> metadata(@PathVariable String queryKey) {
    return R.ok(service.metadata(queryKey));
  }

  /** 分页搜索选项，包含未删除的停用档案。 */
  @GetMapping("/options/{field}")
  public R<?> options(
      @PathVariable String queryKey,
      @PathVariable String field,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer page) {
    return R.ok(service.options(queryKey, field, keyword, null, page));
  }

  /** 批量回显历史查询值。 */
  @GetMapping("/options/{field}/resolve")
  public R<?> resolve(
      @PathVariable String queryKey, @PathVariable String field, @RequestParam List<Long> ids) {
    return R.ok(service.options(queryKey, field, null, ids, 1));
  }
}
