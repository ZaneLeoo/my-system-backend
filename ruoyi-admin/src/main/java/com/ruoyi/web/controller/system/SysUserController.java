package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
  @Autowired private ISysUserService userService;

  @Autowired private ISysRoleService roleService;

  @Autowired private ISysDeptService deptService;

  @Autowired private ISysPostService postService;

  /**
   * 获取用户列表
   *
   * @param user 用户筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('system:user:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysUser user) {
    startPage();
    List<SysUser> list = userService.selectUserList(user);
    return getDataTable(list);
  }

  /**
   * 按筛选条件导出用户。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param user 用户筛选条件
   */
  @Log(title = "用户管理", businessType = BusinessType.EXPORT)
  @PreAuthorize("@ss.hasPermi('system:user:export')")
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysUser user) {
    List<SysUser> list = userService.selectUserList(user);
    ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
    util.exportExcel(response, list, "用户数据");
  }

  /**
   * 导入用户数据，并按请求决定是否更新已有用户。
   *
   * @param file 客户端上传的文件
   * @param updateSupport 是否允许更新已存在的用户
   * @return 导入处理结果及成功、失败信息
   * @throws Exception 执行接口操作失败时
   */
  @Log(title = "用户管理", businessType = BusinessType.IMPORT)
  @PreAuthorize("@ss.hasPermi('system:user:import')")
  @PostMapping("/importData")
  public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
    ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
    List<SysUser> userList = util.importExcel(file.getInputStream());
    String operName = getUsername();
    String message = userService.importUser(userList, updateSupport, operName);
    return success(message);
  }

  /**
   * 下载用户数据导入模板。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   */
  @PostMapping("/importTemplate")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
    util.importTemplateExcel(response, "用户数据");
  }

  /**
   * 根据用户编号获取详细信息
   *
   * @param userId 用户 ID
   * @return 匹配的记录详情
   */
  @PreAuthorize("@ss.hasPermi('system:user:query')")
  @GetMapping(value = {"/", "/{userId}"})
  public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
    AjaxResult ajax = AjaxResult.success();
    if (StringUtils.isNotNull(userId)) {
      userService.checkUserDataScope(userId);
      SysUser sysUser = userService.selectUserById(userId);
      ajax.put(AjaxResult.DATA_TAG, sysUser);
      ajax.put("postIds", postService.selectPostListByUserId(userId));
      ajax.put(
          "roleIds",
          sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
    }
    List<SysRole> roles = roleService.selectRoleAll();
    ajax.put(
        "roles",
        SecurityUtils.isAdmin(userId)
            ? roles
            : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    ajax.put("posts", postService.selectPostAll());
    return ajax;
  }

  /**
   * 新增用户
   *
   * @param user 待新增的用户信息
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:add')")
  @Log(title = "用户管理", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysUser user) {
    deptService.checkDeptDataScope(user.getDeptId());
    roleService.checkRoleDataScope(user.getRoleIds());
    if (!userService.checkUserNameUnique(user)) {
      return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
    } else if (StringUtils.isNotEmpty(user.getPhonenumber())
        && !userService.checkPhoneUnique(user)) {
      return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
    } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
      return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
    }
    user.setCreateBy(getUsername());
    user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
    return toAjax(userService.insertUser(user));
  }

  /**
   * 修改用户
   *
   * @param user 待修改的用户信息及其标识
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:edit')")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysUser user) {
    userService.checkUserAllowed(user);
    userService.checkUserDataScope(user.getUserId());
    deptService.checkDeptDataScope(user.getDeptId());
    roleService.checkRoleDataScope(user.getRoleIds());
    if (!userService.checkUserNameUnique(user)) {
      return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
    } else if (StringUtils.isNotEmpty(user.getPhonenumber())
        && !userService.checkPhoneUnique(user)) {
      return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
    } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
      return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
    }
    user.setUpdateBy(getUsername());
    return toAjax(userService.updateUser(user));
  }

  /**
   * 删除用户
   *
   * @param userIds 用户 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:remove')")
  @Log(title = "用户管理", businessType = BusinessType.DELETE)
  @DeleteMapping("/{userIds}")
  public AjaxResult remove(@PathVariable Long[] userIds) {
    if (ArrayUtils.contains(userIds, getUserId())) {
      return error("当前用户不能删除");
    }
    return toAjax(userService.deleteUserByIds(userIds));
  }

  /**
   * 重置密码
   *
   * @param user 用户 ID 及待重置的新密码
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @PutMapping("/resetPwd")
  public AjaxResult resetPwd(@RequestBody SysUser user) {
    userService.checkUserAllowed(user);
    userService.checkUserDataScope(user.getUserId());
    user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
    user.setUpdateBy(getUsername());
    return toAjax(userService.resetPwd(user));
  }

  /**
   * 状态修改
   *
   * @param user 用户 ID 及目标启停状态
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:edit')")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @PutMapping("/changeStatus")
  public AjaxResult changeStatus(@RequestBody SysUser user) {
    userService.checkUserAllowed(user);
    userService.checkUserDataScope(user.getUserId());
    user.setUpdateBy(getUsername());
    return toAjax(userService.updateUserStatus(user));
  }

  /**
   * 根据用户编号获取授权角色
   *
   * @param userId 用户 ID
   * @return 用户信息及已分配角色列表
   */
  @PreAuthorize("@ss.hasPermi('system:user:query')")
  @GetMapping("/authRole/{userId}")
  public AjaxResult authRole(@PathVariable("userId") Long userId) {
    AjaxResult ajax = AjaxResult.success();
    SysUser user = userService.selectUserById(userId);
    List<SysRole> roles = roleService.selectRolesByUserId(userId);
    ajax.put("user", user);
    ajax.put(
        "roles",
        SecurityUtils.isAdmin(userId)
            ? roles
            : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    return ajax;
  }

  /**
   * 用户授权角色
   *
   * @param userId 用户 ID
   * @param roleIds 角色 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:user:edit')")
  @Log(title = "用户管理", businessType = BusinessType.GRANT)
  @PutMapping("/authRole")
  public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
    userService.checkUserDataScope(userId);
    roleService.checkRoleDataScope(roleIds);
    userService.insertUserAuth(userId, roleIds);
    return success();
  }

  /**
   * 获取部门树列表
   *
   * @param dept 部门筛选条件
   * @return 当前用户权限范围内的部门树
   */
  @PreAuthorize("@ss.hasPermi('system:user:list')")
  @GetMapping("/deptTree")
  public AjaxResult deptTree(SysDept dept) {
    return success(deptService.selectDeptTreeList(dept));
  }
}
