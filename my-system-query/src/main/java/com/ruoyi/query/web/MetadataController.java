package com.ruoyi.query.web;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.query.metadata.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 数据目录管理，只开放已登记应用业务表。 */
@RestController
@RequestMapping("/query/metadata")
public class MetadataController {
  private final MetadataService service;

  /** 注入目录服务。 */
  public MetadataController(MetadataService service) {
    this.service = service;
  }

  /** 查看已导入目录。 */
  @GetMapping("/tables")
  @PreAuthorize("@ss.hasPermi('query:metadata:list')")
  public R<?> list() {
    return R.ok(service.list());
  }

  /** 查看可导入实际表。 */
  @GetMapping("/candidates")
  @PreAuthorize("@ss.hasPermi('query:metadata:import')")
  public R<?> candidates() {
    return R.ok(service.candidates());
  }

  /** 幂等导入指定表及字段。 */
  @PostMapping("/tables/import")
  @PreAuthorize("@ss.hasPermi('query:metadata:import')")
  @Log(title = "查询目录导入", businessType = BusinessType.INSERT)
  public R<?> importTable(@RequestParam String tableName) {
    return R.ok(service.importTable(tableName));
  }

  /** 查看字段目录。 */
  @GetMapping("/tables/{id}/columns")
  @PreAuthorize("@ss.hasPermi('query:metadata:list')")
  public R<?> columns(@PathVariable Long id) {
    return R.ok(service.columns(id));
  }

  /** 预览结构变化和影响对象。 */
  @GetMapping("/tables/{id}/sync-preview")
  @PreAuthorize("@ss.hasPermi('query:metadata:sync')")
  public R<?> preview(@PathVariable Long id) {
    return R.ok(service.preview(id));
  }

  /** 对已经确认的结构差异执行目录同步。 */
  @PostMapping("/tables/{id}/sync")
  @PreAuthorize("@ss.hasPermi('query:metadata:sync')")
  @Log(title = "查询目录同步", businessType = BusinessType.UPDATE)
  public R<?> sync(
      @PathVariable Long id, @RequestParam Integer version, @RequestParam String signature) {
    return R.ok(service.synchronize(id, version, signature));
  }

  /** 人工名称独立于物理数据库注释。 */
  @PutMapping("/tables/{id}/label")
  @PreAuthorize("@ss.hasPermi('query:metadata:edit')")
  @Log(title = "查询目录名称", businessType = BusinessType.UPDATE)
  public R<Void> label(
      @PathVariable Long id,
      @RequestParam Integer version,
      @RequestParam String label,
      @RequestParam(required = false) Long columnId) {
    service.rename(id, version, label, columnId);
    return R.ok();
  }
}
