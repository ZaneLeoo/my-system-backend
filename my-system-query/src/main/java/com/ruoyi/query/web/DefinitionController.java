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

  /**
   * 查询已接入业务对象。
   *
   * @return 已由开发者登记的查询对象列表
   */
  @GetMapping
  @PreAuthorize("@ss.hasPermi('query:definition:list')")
  public R<?> list() {
    return R.ok(service.list());
  }

  /**
   * 读取对象信息、字段草稿、来源和已注册提供器。
   *
   * @param id 查询配置 ID
   * @return 查询对象、完整字段草稿、可选来源及已注册提供器
   */
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

  /**
   * 按草稿编辑版本保存查询名称、启停状态及完整字段集合。
   *
   * <p>持锁检查并发版本，保留稳定字段标识；已有字段不能换绑来源，未继续提交的旧字段停用，已发布配置不受本次保存影响。
   *
   * @param id 查询配置 ID
   * @param request 查询名称、启停状态、完整字段草稿及当前编辑版本
   * @return 保存后的查询对象及最新草稿版本
   * @throws com.ruoyi.common.exception.ServiceException 草稿版本过期、字段标识重复或试图改绑已有字段来源时
   */
  @PutMapping("/{id}")
  @PreAuthorize("@ss.hasPermi('query:definition:edit')")
  @Log(title = "查询配置草稿", businessType = BusinessType.UPDATE)
  public R<?> save(@PathVariable Long id, @RequestBody SaveDefinitionRequest request) {
    return R.ok(service.save(id, request));
  }

  /**
   * 使用已保存草稿执行只读查询预览。
   *
   * <p>同时要求查询配置预览权限和对应业务列表权限；预览复用正式查询的条件编译与业务投影，不生成发布版本。
   *
   * @param id 查询配置 ID
   * @param request 预览的分页、排序及条件树，字段按当前已保存草稿校验
   * @return 草稿对应的界面元数据和业务查询结果
   * @throws com.ruoyi.common.exception.ServiceException 草稿配置或查询条件不合法时
   */
  @PostMapping("/{id}/preview")
  @PreAuthorize("@ss.hasPermi('query:definition:preview')")
  public R<?> preview(@PathVariable Long id, @RequestBody SearchRequest request) {
    return R.ok(runtime.preview(id, request));
  }

  /**
   * 按草稿版本校验并发布不可变查询快照。
   *
   * <p>复核字段、控件、运算符及真实数据库结构，在同一事务内保存新快照并切换当前发布版本；结构变化需先同步数据目录。
   *
   * @param id 查询配置 ID
   * @param version 当前草稿编辑版本，用于拒绝并发修改后的过期操作
   * @return 本次生成的不可变发布版本
   * @throws com.ruoyi.common.exception.ServiceException 草稿版本过期、配置不合法或数据库结构尚未同步时
   */
  @PostMapping("/{id}/publish")
  @PreAuthorize("@ss.hasPermi('query:definition:publish')")
  @Log(title = "查询配置发布", businessType = BusinessType.UPDATE)
  public R<?> publish(@PathVariable Long id, @RequestParam Integer version) {
    return R.ok(service.publish(id, version));
  }

  /**
   * 查看不可变历史版本，仅管理权限可见内部配置。
   *
   * @param id 查询配置 ID
   * @return 按发布版本倒序排列的历史快照
   */
  @GetMapping("/{id}/releases")
  @PreAuthorize("@ss.hasPermi('query:definition:list')")
  public R<?> releases(@PathVariable Long id) {
    return R.ok(service.releases(id));
  }

  /**
   * 草稿预览的选项也按业务权限授权，不借用已发布字段目录。
   *
   * @param id 查询配置 ID
   * @param field 当前查询配置中的稳定字段标识
   * @param keyword 编码或名称搜索关键字，可为空
   * @param ids 待回显的 1～100 个不同正整数 ID；为 null 时按关键字搜索
   * @param page 从 1 开始的选项页码
   * @return 草稿字段对应的查询选项和总数
   */
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
