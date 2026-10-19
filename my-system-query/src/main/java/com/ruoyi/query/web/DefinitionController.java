package com.ruoyi.query.web;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.query.definition.*;
import com.ruoyi.query.engine.QueryRuntimeService;
import com.ruoyi.query.protocol.SearchRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 查询配置草稿、预览和发布；不接受任意 SQL 或组件地址。 */
@RestController
@RequestMapping("/query/definitions")
public class DefinitionController {
  private final DefinitionService service;
  private final QueryRuntimeService runtime;

  /** 注入配置与运行时入口。 */
  public DefinitionController(DefinitionService service, QueryRuntimeService runtime) {
    this.service = service;
    this.runtime = runtime;
  }

  /** 查询已接入业务对象。 */
  @GetMapping
  @PreAuthorize("@ss.hasPermi('query:definition:list')")
  public R<?> list() {
    return R.ok(service.list());
  }

  /** 读取对象信息、字段草稿、来源和已注册提供器。 */
  @GetMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('query:definition:list')")
  public R<?> detail(@PathVariable Long id) {
    return R.ok(
        java.util.Map.of(
            "definition",
            service.require(id),
            "fields",
            service.fields(id),
            "sources",
            service.sources(id),
            "providers",
            service.providers(id)));
  }

  /** 按整份草稿版本保存。 */
  @PutMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('query:definition:edit')")
  @Log(title = "查询配置草稿", businessType = BusinessType.UPDATE)
  public R<?> save(@PathVariable Long id, @RequestBody SaveDefinitionRequest request) {
    return R.ok(service.save(id, request));
  }

  /** 预览已保存草稿，业务权限在运行时再次检查。 */
  @PostMapping("/{id}/preview")
  @PreAuthorize("@ss.hasPermi('query:definition:preview')")
  public R<?> preview(@PathVariable Long id, @RequestBody SearchRequest request) {
    return R.ok(runtime.preview(id, request));
  }

  /** 发布完整配置，不改变旧发布快照。 */
  @PostMapping("/{id}/publish")
  @PreAuthorize("@ss.hasPermi('query:definition:publish')")
  @Log(title = "查询配置发布", businessType = BusinessType.UPDATE)
  public R<?> publish(@PathVariable Long id, @RequestParam Integer version) {
    return R.ok(service.publish(id, version));
  }

  /** 查看不可变历史版本，仅管理权限可见内部配置。 */
  @GetMapping("/{id}/releases")
  @PreAuthorize("@ss.hasPermi('query:definition:list')")
  public R<?> releases(@PathVariable Long id) {
    return R.ok(service.releases(id));
  }

  /** 草稿预览的选项也按业务权限授权，不借用已发布字段目录。 */
  @GetMapping("/{id}/options/{field}")
  @PreAuthorize("@ss.hasPermi('query:definition:preview')")
  public R<?> options(
      @PathVariable Long id,
      @PathVariable String field,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) java.util.List<Long> ids,
      @RequestParam(defaultValue = "1") Integer page) {
    return R.ok(runtime.previewOptions(id, field, keyword, ids, page));
  }
}
