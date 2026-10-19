package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.service.ISysPostService;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {
  @Autowired private ISysPostService postService;

  /**
   * 获取岗位列表
   *
   * @param post 岗位筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('system:post:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysPost post) {
    startPage();
    List<SysPost> list = postService.selectPostList(post);
    return getDataTable(list);
  }

  /**
   * 按筛选条件导出岗位。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param post 岗位筛选条件
   */
  @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
  @PreAuthorize("@ss.hasPermi('system:post:export')")
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysPost post) {
    List<SysPost> list = postService.selectPostList(post);
    ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
    util.exportExcel(response, list, "岗位数据");
  }

  /**
   * 根据岗位编号获取详细信息
   *
   * @param postId 岗位 ID
   * @return 匹配的记录详情
   */
  @PreAuthorize("@ss.hasPermi('system:post:query')")
  @GetMapping(value = "/{postId}")
  public AjaxResult getInfo(@PathVariable Long postId) {
    return success(postService.selectPostById(postId));
  }

  /**
   * 新增岗位
   *
   * @param post 待新增的岗位信息
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:post:add')")
  @Log(title = "岗位管理", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysPost post) {
    if (!postService.checkPostNameUnique(post)) {
      return error("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
    } else if (!postService.checkPostCodeUnique(post)) {
      return error("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
    }
    post.setCreateBy(getUsername());
    return toAjax(postService.insertPost(post));
  }

  /**
   * 修改岗位
   *
   * @param post 待修改的岗位信息及其标识
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:post:edit')")
  @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysPost post) {
    if (!postService.checkPostNameUnique(post)) {
      return error("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
    } else if (!postService.checkPostCodeUnique(post)) {
      return error("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
    }
    post.setUpdateBy(getUsername());
    return toAjax(postService.updatePost(post));
  }

  /**
   * 删除岗位
   *
   * @param postIds 岗位 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:post:remove')")
  @Log(title = "岗位管理", businessType = BusinessType.DELETE)
  @DeleteMapping("/{postIds}")
  public AjaxResult remove(@PathVariable Long[] postIds) {
    return toAjax(postService.deletePostByIds(postIds));
  }

  /**
   * 获取岗位选择框列表
   *
   * @return 操作结果或选项列表
   */
  @GetMapping("/optionselect")
  public AjaxResult optionselect() {
    List<SysPost> posts = postService.selectPostAll();
    return success(posts);
  }
}
