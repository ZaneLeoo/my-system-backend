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

  /**
   * 按中文名称或数据库表名筛选已导入目录并分页。
   *
   * @param query 中文名称或物理表名关键字及分页参数，每页 1～100 条
   * @return 当前页记录及全部匹配记录的总数
   */
  @GetMapping("/tables")
  @PreAuthorize("@ss.hasPermi('query:metadata:list')")
  public R<PageResult<MetadataTableView>> list(@Valid MetadataTableQuery query) {
    return R.ok(service.page(query));
  }

  /**
   * 返回目录表对应的已登记查询配置，仅供同时具备两项管理权限的用户查看。
   *
   * @param tableIds 当前目录页的表 ID，每次最多 100 个；为空时返回全部关联
   * @return 指定目录表与查询配置的关联，包含主表及预设明细来源
   */
  @GetMapping("/definition-links")
  @PreAuthorize("@ss.hasPermi('query:metadata:list') and @ss.hasPermi('query:definition:list')")
  public R<?> definitionLinks(@RequestParam(required = false) List<Long> tableIds) {
    return R.ok(definitions.tableLinks(tableIds));
  }

  /**
   * 查看可导入实际表。
   *
   * @return 已登记且实际存在的业务表，包含中文名称和已导入标识
   */
  @GetMapping("/candidates")
  @PreAuthorize("@ss.hasPermi('query:metadata:import')")
  public R<?> candidates() {
    return R.ok(service.candidates());
  }

  /**
   * 幂等导入已登记业务表及其真实字段结构。
   *
   * <p>只读取当前应用数据库，首次导入创建目录；已导入时返回原目录，不覆盖人工名称、稳定 ID 或已有查询配置。
   *
   * @param tableName 数据库物理表名
   * @return 新建或已存在的表目录；重复导入不覆盖已有配置
   * @throws com.ruoyi.common.exception.ServiceException 物理表未登记或在当前数据库中不存在时
   */
  @PostMapping("/tables/import")
  @PreAuthorize("@ss.hasPermi('query:metadata:import')")
  @Log(title = "查询目录导入", businessType = BusinessType.INSERT)
  public R<?> importTable(@RequestParam String tableName) {
    return R.ok(service.importTable(tableName));
  }

  /**
   * 查看字段目录。
   *
   * @param id 数据目录 ID
   * @return 字段目录列表，包含失效字段，便于检查配置引用
   */
  @GetMapping("/tables/{id}/columns")
  @PreAuthorize("@ss.hasPermi('query:metadata:list')")
  public R<?> columns(@PathVariable Long id) {
    return R.ok(service.columns(id));
  }

  /**
   * 预览结构变化和影响对象。
   *
   * @param id 数据目录 ID
   * @return 新增、变更、移除字段、受影响查询及确认所需的版本和签名
   */
  @GetMapping("/tables/{id}/sync-preview")
  @PreAuthorize("@ss.hasPermi('query:metadata:sync')")
  public R<?> preview(@PathVariable Long id) {
    return R.ok(service.preview(id));
  }

  /**
   * 根据已确认的结构差异同步表和字段目录。
   *
   * <p>在事务锁内复核目录版本和预览签名，保留已有字段 ID 及人工名称；物理删除字段仅标记失效，不执行业务表 DDL。
   *
   * @param id 数据目录 ID
   * @param version 预览或读取时的目录版本，用于并发检查
   * @param signature 预览返回的结构签名，用于拒绝过期预览
   * @return 同步后的表目录及最新版本
   * @throws com.ruoyi.common.exception.ServiceException 目录版本过期或数据库结构与预览签名不一致时
   */
  @PostMapping("/tables/{id}/sync")
  @PreAuthorize("@ss.hasPermi('query:metadata:sync')")
  @Log(title = "查询目录同步", businessType = BusinessType.UPDATE)
  public R<?> sync(
      @PathVariable Long id, @RequestParam Integer version, @RequestParam String signature) {
    return R.ok(service.synchronize(id, version, signature));
  }

  /**
   * 人工名称独立于物理数据库注释。
   *
   * @param id 数据目录 ID
   * @param version 预览或读取时的目录版本，用于并发检查
   * @param label 去除首尾空白后的中文显示名称
   * @param columnId 字段目录 ID，为空时修改表目录名称
   * @return 操作成功响应，业务数据为空
   */
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
