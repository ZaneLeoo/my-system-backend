package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeReadService;
import com.ruoyi.system.service.ISysNoticeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
  @Autowired private ISysNoticeService noticeService;

  @Autowired private ISysNoticeReadService noticeReadService;

  /**
   * 获取通知公告列表
   *
   * @param notice 通知公告筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('system:notice:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysNotice notice) {
    startPage();
    List<SysNotice> list = noticeService.selectNoticeList(notice);
    return getDataTable(list);
  }

  /**
   * 根据通知公告编号获取详细信息
   *
   * @param noticeId 通知公告 ID
   * @return 匹配的记录详情
   */
  @GetMapping(value = "/{noticeId}")
  public AjaxResult getInfo(@PathVariable Long noticeId) {
    return success(noticeService.selectNoticeById(noticeId));
  }

  /**
   * 新增通知公告
   *
   * @param notice 待新增的通知公告信息
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:notice:add')")
  @Log(title = "通知公告", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysNotice notice) {
    notice.setCreateBy(getUsername());
    return toAjax(noticeService.insertNotice(notice));
  }

  /**
   * 修改通知公告
   *
   * @param notice 待修改的通知公告信息及其标识
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:notice:edit')")
  @Log(title = "通知公告", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
    notice.setUpdateBy(getUsername());
    return toAjax(noticeService.updateNotice(notice));
  }

  /**
   * 首页顶部公告列表（返回全部正常公告，带当前用户已读标记，最多5条）
   *
   * @return 操作结果，失败时包含具体错误说明
   */
  @GetMapping("/listTop")
  @ResponseBody
  public AjaxResult listTop() {
    Long userId = getUserId();
    List<SysNotice> list = noticeReadService.selectNoticeListWithReadStatus(userId, 5);
    long unreadCount = list.stream().filter(n -> !n.getIsRead()).count();
    AjaxResult result = AjaxResult.success(list);
    result.put("unreadCount", unreadCount);
    return result;
  }

  /**
   * 标记公告已读
   *
   * @param noticeId 通知公告 ID
   * @return 操作结果，失败时包含具体错误说明
   */
  @PostMapping("/markRead")
  @ResponseBody
  public AjaxResult markRead(Long noticeId) {
    Long userId = getUserId();
    noticeReadService.markRead(noticeId, userId);
    return success();
  }

  /**
   * 批量标记已读
   *
   * @param ids 以逗号分隔的公告 ID
   * @return 操作结果，失败时包含具体错误说明
   */
  @PostMapping("/markReadAll")
  @ResponseBody
  public AjaxResult markReadAll(String ids) {
    Long userId = getUserId();
    Long[] noticeIds = Convert.toLongArray(ids);
    noticeReadService.markReadBatch(userId, noticeIds);
    return success();
  }

  /**
   * 已读用户列表数据
   *
   * @param noticeId 通知公告 ID
   * @param searchValue 用户搜索关键字，可为空
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('system:notice:list')")
  @GetMapping("/readUsers/list")
  @ResponseBody
  public TableDataInfo readUsersList(Long noticeId, String searchValue) {
    startPage();
    List<?> list = noticeReadService.selectReadUsersByNoticeId(noticeId, searchValue);
    return getDataTable(list);
  }

  /**
   * 删除通知公告
   *
   * @param noticeIds 公告 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:notice:remove')")
  @Log(title = "通知公告", businessType = BusinessType.DELETE)
  @DeleteMapping("/{noticeIds}")
  public AjaxResult remove(@PathVariable Long[] noticeIds) {
    noticeReadService.deleteByNoticeIds(noticeIds);
    return toAjax(noticeService.deleteNoticeByIds(noticeIds));
  }
}
