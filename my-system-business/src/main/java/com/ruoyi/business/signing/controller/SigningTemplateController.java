package com.ruoyi.business.signing.controller;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.signing.api.*;
import com.ruoyi.signing.application.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.spi.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 官方模板编辑与本地业务映射，不接收任意 URL 或 SQL。 */
@RestController
@RequestMapping("/signing/templates")
public class SigningTemplateController {
  private final SigningProvider provider;
  private final SigningTemplateService templates;
  private final SigningRepository repository;
  private final List<SigningBusinessAdapter> adapters;
  private final SigningAccess access;

  /** 注入平台和开发者登记业务。 */
  public SigningTemplateController(
      SigningProvider provider,
      SigningTemplateService templates,
      SigningRepository repository,
      List<SigningBusinessAdapter> adapters,
      SigningAccess access) {
    this.provider = provider;
    this.templates = templates;
    this.repository = repository;
    this.adapters = adapters;
    this.access = access;
  }

  /**
   * 列出当前应用可访问的平台模板。
   *
   * @param page 页码，每页二十项
   * @return 平台模板的展示字段
   */
  @GetMapping("/platform")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public R<Map<String, Object>> platform(@RequestParam(defaultValue = "1") int page) {
    return R.ok(provider.templates(page));
  }

  /**
   * 查询本地模板绑定。
   *
   * @param businessType 登记场景
   * @return 当前环境的业务模板；普通发起用户只获得启用项
   */
  @GetMapping("/bindings")
  @PreAuthorize("@ss.hasAnyPermi('signing:template:edit,signing:task:start')")
  public R<List<SigningTemplateBinding>> bindings(@RequestParam String businessType) {
    templates.fields(businessType);
    List<SigningTemplateBinding> result = new ArrayList<SigningTemplateBinding>();
    for (SigningTemplateBinding binding : repository.bindings(businessType)) {
      if (provider.environment().equals(binding.getProviderEnvironment())
          && (binding.isEnabled()
              || com.ruoyi.common.utils.SecurityUtils.hasPermi("signing:template:edit"))) {
        result.add(binding);
      }
    }
    return R.ok(result);
  }

  /**
   * 保存映射并检查平台控件变化。
   *
   * @param binding 包含当前版本的业务映射
   * @return 保存后的绑定
   */
  @com.ruoyi.common.annotation.Log(
      title = "合同模板映射",
      businessType = com.ruoyi.common.enums.BusinessType.UPDATE,
      isSaveRequestData = false,
      isSaveResponseData = false)
  @PutMapping("/bindings")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public R<SigningTemplateBinding> save(@RequestBody SigningTemplateBinding binding) {
    return R.ok(templates.save(binding, com.ruoyi.common.utils.SecurityUtils.getUsername()));
  }

  /**
   * 返回中文业务字段目录。
   *
   * @param businessType 已登记业务
   * @return 受控字段，不包含数据库结构
   */
  @GetMapping("/fields")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public R<List<SigningField>> fields(@RequestParam String businessType) {
    return R.ok(templates.fields(businessType));
  }

  /**
   * 读取官方控件用于下拉映射。
   *
   * @param templateId 当前应用有权访问的模板 ID
   * @return 归一化控件
   */
  @GetMapping("/{templateId}/controls")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public R<List<Map<String, Object>>> controls(@PathVariable String templateId) {
    return R.ok(DefaultSigningTemplateService.controls(provider.template(templateId)));
  }

  /**
   * 打开 e签宝编辑器，不嵌入自制文件编辑器。
   *
   * @param templateId 平台模板
   * @return 临时官方链接
   */
  @PostMapping("/{templateId}/edit-url")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public R<String> edit(@PathVariable String templateId) {
    return R.ok(provider.templateEditUrl(templateId));
  }

  /**
   * 使用实际业务快照试填，配置权限不替代业务查看权限。
   *
   * @param bindingId 已保存模板映射
   * @param businessId 有权读取的来源单据
   * @return 临时试填 PDF，不创建签署流程
   */
  @PostMapping("/bindings/{bindingId}/preview")
  @PreAuthorize("@ss.hasPermi('signing:template:edit')")
  public ResponseEntity<byte[]> preview(
      @PathVariable String bindingId, @RequestParam String businessId) {
    SigningTemplateBinding binding = repository.binding(bindingId);
    if (binding == null) {
      throw new SigningException("模板配置不存在");
    }
    access.business(binding.getBusinessType(), businessId);
    SigningBusinessAdapter adapter =
        adapters.stream()
            .filter(item -> item.businessType().equals(binding.getBusinessType()))
            .findFirst()
            .orElseThrow(() -> new SigningException("未登记业务"));
    String file =
        provider.fill(
            binding.getTemplateId(),
            templates.values(binding, adapter.snapshot(businessId).getValues()));
    Map<String, Object> detail = provider.file(file);
    if (!Arrays.asList("2", "5").contains(text(detail, "fileStatus"))) {
      throw new SigningException("平台仍在生成试填文件，请稍后重试");
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .contentType(MediaType.APPLICATION_PDF)
        .body(provider.download(text(detail, "fileDownloadUrl")));
  }

  /**
   * 统一映射业务错误。
   *
   * @param error 已脱敏错误
   * @return R 失败响应
   */
  @ExceptionHandler(SigningException.class)
  public R<Void> error(SigningException error) {
    return R.fail(error.getMessage());
  }
}
