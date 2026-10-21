package com.ruoyi.business.signing.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.application.DefaultSigningService;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.PartyRole;
import com.ruoyi.signing.spi.SigningRepository;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 任务范围内的我方印章读取，不暴露平台临时图片链接。 */
@RestController
@RequestMapping("/signing/tasks/{id}/seals")
public class SigningSealController {
  private final DefaultSigningService service;
  private final SigningSealService seals;
  private final SigningAccess access;
  private final SigningRepository repository;

  /** 注入平台无关读取服务和宿主访问边界。 */
  public SigningSealController(
      DefaultSigningService service,
      SigningSealService seals,
      SigningAccess access,
      SigningRepository repository) {
    this.service = service;
    this.seals = seals;
    this.access = access;
    this.repository = repository;
  }

  /** 同时核实来源单据权限和当前平台环境。 */
  private void authorize(String id) {
    SigningTask task = service.task(id);
    access.business(task.getBusinessType(), task.getBusinessId());
    service.verifyEnvironment(task);
  }

  /**
   * 分页读取当前我方企业的内部机构印章。
   *
   * @param id 签署任务编号
   * @param pageNum 页码，从一开始
   * @param pageSize 每页数量，默认八枚，最多二十枚
   * @param sealType 可空的印章业务类型
   * @return 印章信息及总数；不含平台图片 URL
   */
  @GetMapping
  @PreAuthorize("@ss.hasPermi('signing:task:start')")
  public R<SigningSealPage> list(
      @PathVariable String id,
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "8") int pageSize,
      @RequestParam(required = false) String sealType) {
    authorize(id);
    return R.ok(seals.list(pageNum, pageSize, sealType));
  }

  /**
   * 鉴权读取印章的带水印预览图；查询用户仅能查看任务已记录的印章。
   *
   * @param id 签署任务编号
   * @param sealId 我方内部印章编号
   * @return 禁止缓存的 PNG 或 JPEG 图片
   */
  @GetMapping("/{sealId}/preview")
  @PreAuthorize("@ss.hasAnyPermi('signing:task:query,signing:task:start')")
  public ResponseEntity<byte[]> preview(@PathVariable String id, @PathVariable String sealId) {
    authorize(id);
    if (!SecurityUtils.hasPermi("signing:task:start")) {
      boolean selected = false;
      for (SigningParty party : repository.parties(id)) {
        if (PartyRole.OUR.name().equals(party.getRole()) && sealId.equals(party.getSealId())) {
          selected = true;
        }
      }
      if (!selected) {
        throw new SigningException("仅允许查看本任务已记录的我方印章");
      }
    }
    SigningSealImage image = seals.preview(sealId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header("X-Content-Type-Options", "nosniff")
        .contentType(MediaType.parseMediaType(image.getContentType()))
        .body(image.getBytes());
  }
}
