package com.ruoyi.quartz.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.quartz.domain.SysJobLog;
import com.ruoyi.quartz.service.ISysJobLogService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调度日志操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {
  @Autowired private ISysJobLogService jobLogService;

  /**
   * 查询定时任务调度日志列表
   *
   * @param sysJobLog 任务执行日志筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysJobLog sysJobLog) {
    startPage();
    List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
    return getDataTable(list);
  }

  /**
   * 导出定时任务调度日志列表
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param sysJobLog 任务执行日志筛选条件
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:export')")
  @Log(title = "任务调度日志", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysJobLog sysJobLog) {
    List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
    ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
    util.exportExcel(response, list, "调度日志");
  }

  /**
   * 根据调度编号获取详细信息
   *
   * @param jobLogId 任务执行日志 ID
   * @return 匹配的记录详情
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:query')")
  @GetMapping(value = "/{jobLogId}")
  public AjaxResult getInfo(@PathVariable Long jobLogId) {
    return success(jobLogService.selectJobLogById(jobLogId));
  }

  /**
   * 删除定时任务调度日志
   *
   * @param jobLogIds 任务执行日志 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @Log(title = "定时任务调度日志", businessType = BusinessType.DELETE)
  @DeleteMapping("/{jobLogIds}")
  public AjaxResult remove(@PathVariable Long[] jobLogIds) {
    return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
  }

  /**
   * 清空定时任务调度日志
   *
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @Log(title = "调度日志", businessType = BusinessType.CLEAN)
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    jobLogService.cleanJobLog();
    return success();
  }
}
