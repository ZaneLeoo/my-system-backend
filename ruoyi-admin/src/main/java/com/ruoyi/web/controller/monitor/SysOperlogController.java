package com.ruoyi.web.controller.monitor;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysOperLogService;
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
 * 操作日志记录
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {
  @Autowired private ISysOperLogService operLogService;

  /**
   * 按筛选条件分页查询操作日志。
   *
   * @param operLog 操作日志筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysOperLog operLog) {
    startPage();
    List<SysOperLog> list = operLogService.selectOperLogList(operLog);
    return getDataTable(list);
  }

  /**
   * 按筛选条件导出操作日志。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param operLog 操作日志筛选条件
   */
  @Log(title = "操作日志", businessType = BusinessType.EXPORT)
  @PreAuthorize("@ss.hasPermi('monitor:operlog:export')")
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysOperLog operLog) {
    List<SysOperLog> list = operLogService.selectOperLogList(operLog);
    ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
    util.exportExcel(response, list, "操作日志");
  }

  /**
   * 批量删除指定操作日志。
   *
   * @param operIds 操作日志 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @Log(title = "操作日志", businessType = BusinessType.DELETE)
  @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
  @DeleteMapping("/{operIds}")
  public AjaxResult remove(@PathVariable Long[] operIds) {
    return toAjax(operLogService.deleteOperLogByIds(operIds));
  }

  /**
   * 清空操作日志。
   *
   * @return 操作结果，失败时包含具体错误说明
   */
  @Log(title = "操作日志", businessType = BusinessType.CLEAN)
  @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    operLogService.cleanOperLog();
    return success();
  }
}
