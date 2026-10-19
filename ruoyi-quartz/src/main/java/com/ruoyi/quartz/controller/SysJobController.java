package com.ruoyi.quartz.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.job.TaskException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;
import com.ruoyi.quartz.util.CronUtils;
import com.ruoyi.quartz.util.ScheduleUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调度任务信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController {
  @Autowired private ISysJobService jobService;

  /**
   * 查询定时任务列表
   *
   * @param sysJob 定时任务筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysJob sysJob) {
    startPage();
    List<SysJob> list = jobService.selectJobList(sysJob);
    return getDataTable(list);
  }

  /**
   * 导出定时任务列表
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param sysJob 定时任务筛选条件
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:export')")
  @Log(title = "定时任务", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysJob sysJob) {
    List<SysJob> list = jobService.selectJobList(sysJob);
    ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
    util.exportExcel(response, list, "定时任务");
  }

  /**
   * 获取定时任务详细信息
   *
   * @param jobId 定时任务 ID
   * @return 匹配的记录详情
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:query')")
  @GetMapping(value = "/{jobId}")
  public AjaxResult getInfo(@PathVariable("jobId") Long jobId) {
    return success(jobService.selectJobById(jobId));
  }

  /**
   * 新增定时任务
   *
   * @param job 待新增的定时任务信息
   * @return 操作结果，失败时包含具体错误说明
   * @throws SchedulerException 调度器执行任务操作失败时
   * @throws TaskException 任务配置不满足调度约束时
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:add')")
  @Log(title = "定时任务", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException {
    if (!CronUtils.isValid(job.getCronExpression())) {
      return error("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
    } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
    } else if (StringUtils.containsAnyIgnoreCase(
        job.getInvokeTarget(), new String[] {Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(
        job.getInvokeTarget(), new String[] {Constants.HTTP, Constants.HTTPS})) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
    } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
    }
    job.setCreateBy(getUsername());
    return toAjax(jobService.insertJob(job));
  }

  /**
   * 修改定时任务
   *
   * @param job 待修改的定时任务信息及其标识
   * @return 操作结果，失败时包含具体错误说明
   * @throws SchedulerException 调度器执行任务操作失败时
   * @throws TaskException 任务配置不满足调度约束时
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:edit')")
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
    if (!CronUtils.isValid(job.getCronExpression())) {
      return error("修改任务'" + job.getJobName() + "'失败，Cron表达式不正确");
    } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
    } else if (StringUtils.containsAnyIgnoreCase(
        job.getInvokeTarget(), new String[] {Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(
        job.getInvokeTarget(), new String[] {Constants.HTTP, Constants.HTTPS})) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串存在违规");
    } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
    }
    job.setUpdateBy(getUsername());
    return toAjax(jobService.updateJob(job));
  }

  /**
   * 定时任务状态修改
   *
   * @param job 待操作的定时任务信息
   * @return 操作结果，失败时包含具体错误说明
   * @throws SchedulerException 调度器执行任务操作失败时
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @PutMapping("/changeStatus")
  public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
    SysJob newJob = jobService.selectJobById(job.getJobId());
    newJob.setStatus(job.getStatus());
    return toAjax(jobService.changeStatus(newJob));
  }

  /**
   * 定时任务立即执行一次
   *
   * @param job 待操作的定时任务信息
   * @return 操作结果，失败时包含具体错误说明
   * @throws SchedulerException 调度器执行任务操作失败时
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @PutMapping("/run")
  public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
    boolean result = jobService.run(job);
    return result ? success() : error("任务不存在或已过期！");
  }

  /**
   * 删除定时任务
   *
   * @param jobIds 定时任务 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   * @throws SchedulerException 调度器执行任务操作失败时
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @Log(title = "定时任务", businessType = BusinessType.DELETE)
  @DeleteMapping("/{jobIds}")
  public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException {
    jobService.deleteJobByIds(jobIds);
    return success();
  }
}
