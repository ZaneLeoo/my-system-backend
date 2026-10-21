package com.ruoyi.business.signing.controller;

import com.ruoyi.business.signing.dto.*;
import com.ruoyi.business.signing.service.SigningWorkbenchService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 工作台和业务登记入口，控制器不依赖任何具体业务单据。 */
@RestController
@RequestMapping("/signing")
public class SigningWorkbenchController {
  private final SigningAccess access;
  private final SigningWorkbenchService workbench;

  /** 注入业务访问登记和摘要查询。 */
  public SigningWorkbenchController(SigningAccess access, SigningWorkbenchService workbench) {
    this.access = access;
    this.workbench = workbench;
  }

  /**
   * 获取已登记业务及当前用户来源访问能力。
   *
   * @return 场景目录，不包含业务数据
   */
  @GetMapping("/businesses")
  @PreAuthorize("@ss.hasAnyPermi('signing:task:query,signing:task:start,signing:template:edit')")
  public R<List<SigningScenarioView>> businesses() {
    return R.ok(access.scenarios());
  }

  /**
   * 分页选择满足发起资格的业务单据，权限仍由来源适配器校验。
   *
   * @param businessType 已登记业务类型
   * @param query 单据号关键词及分页
   * @return 当前用户可选来源及总数
   */
  @GetMapping("/businesses/{businessType}/sources")
  @PreAuthorize("@ss.hasAnyPermi('signing:task:start,signing:template:edit')")
  public R<PageResult<SigningSourceView>> sources(
      @PathVariable String businessType, @Valid SigningSourceQuery query) {
    return R.ok(access.support(businessType).sources(query));
  }

  /**
   * 分页查看所有有权读取的场景任务，不因签署权限放宽来源权限。
   *
   * @param query 场景、状态及分页
   * @return 安全任务摘要及总数
   */
  @GetMapping("/tasks/list")
  @PreAuthorize("@ss.hasPermi('signing:task:query')")
  public R<PageResult<SigningTaskSummary>> list(@Valid SigningTaskQuery query) {
    return R.ok(workbench.list(query));
  }
}
