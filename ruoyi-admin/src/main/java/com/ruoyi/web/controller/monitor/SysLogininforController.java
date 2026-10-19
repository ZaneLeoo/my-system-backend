package com.ruoyi.web.controller.monitor;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.service.SysPasswordService;
import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.service.ISysLogininforService;
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
 * 系统访问记录
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
  @Autowired private ISysLogininforService logininforService;

  @Autowired private SysPasswordService passwordService;

  /**
   * 按筛选条件分页查询登录日志。
   *
   * @param logininfor 登录日志筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysLogininfor logininfor) {
    startPage();
    List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
    return getDataTable(list);
  }

  /**
   * 按筛选条件导出登录日志。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param logininfor 登录日志筛选条件
   */
  @Log(title = "登录日志", businessType = BusinessType.EXPORT)
  @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysLogininfor logininfor) {
    List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
    ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
    util.exportExcel(response, list, "登录日志");
  }

  /**
   * 批量删除指定登录日志。
   *
   * @param infoIds 登录日志 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
  @Log(title = "登录日志", businessType = BusinessType.DELETE)
  @DeleteMapping("/{infoIds}")
  public AjaxResult remove(@PathVariable Long[] infoIds) {
    return toAjax(logininforService.deleteLogininforByIds(infoIds));
  }

  /**
   * 清空登录日志。
   *
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
  @Log(title = "登录日志", businessType = BusinessType.CLEAN)
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    logininforService.cleanLogininfor();
    return success();
  }

  /**
   * 解除指定账号的密码重试锁定。
   *
   * @param userName 用户登录账号
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('monitor:logininfor:unlock')")
  @Log(title = "账户解锁", businessType = BusinessType.OTHER)
  @GetMapping("/unlock/{userName}")
  public AjaxResult unlock(@PathVariable("userName") String userName) {
    passwordService.clearLoginRecordCache(userName);
    return success();
  }
}
