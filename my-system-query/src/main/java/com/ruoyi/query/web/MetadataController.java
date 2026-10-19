package com.ruoyi.query.web;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.query.definition.DefinitionService;
import com.ruoyi.query.metadata.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 数据目录管理，只开放已登记应用业务表。 */
@RestController
@RequestMapping("/query/metadata")
public class MetadataController {
  private final MetadataService service;
  private final DefinitionService definitions;

  /** 注入目录服务。 */
  public MetadataController(MetadataService service, DefinitionService definitions) {
    this.service = service;
    this.definitions = definitions;
  }

  /** 按中文名称或数据库表名筛选已导入目录并分页。 */
  @GetMapping("/tables")
  @PreAuthorize("@ss.hasPermi('query:metadata:list')")
  public R<PageResult<MetadataTableView>> list(@Valid MetadataTableQuery query) {
    return R.ok(service.page(query));
  }

  /** 返回目录表对应的已登记查询配置，仅供同时具备两项管理权限的用户查看。 */
  @GetMapping("/definition-links")
  @PreAuthorize("@ss.hasPermi('query:metadata:list') and @ss.hasPermi('query:definition:list')")
  public R<?> definitionLinks(@RequestParam(required = false) List<Long> tableIds) {
    return R.ok(definitions.tableLinks(tableIds));
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
