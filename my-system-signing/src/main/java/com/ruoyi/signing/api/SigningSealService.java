package com.ruoyi.signing.api;

import static com.ruoyi.signing.application.SigningSupport.required;

import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.SigningSeal;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.spi.SigningSealProvider;

/** 可迁移的我方印章读取及选择校验，不持有数据库事务。 */
public final class SigningSealService {
  private final SigningSealProvider provider;

  /** 注入当前平台的只读印章能力。 */
  public SigningSealService(SigningSealProvider provider) {
    this.provider = provider;
  }

  /**
   * 分页读取支持的机构印章。
   *
   * @param pageNum 页码，从一开始
   * @param pageSize 每页一至二十枚
   * @param type 类型标识，空表示全部支持类型
   * @return 分页印章及可选状态
   */
  public SigningSealPage list(int pageNum, int pageSize, String type) {
    if (pageNum < 1 || pageSize < 1 || pageSize > 20) {
      throw new SigningException("印章分页参数不合法，每页允许 1～20 枚");
    }
    SigningSealType selected = null;
    if (type != null && !type.trim().isEmpty()) {
      try {
        selected = SigningSealType.valueOf(type);
      } catch (IllegalArgumentException error) {
        throw new SigningException("印章类型不支持");
      }
      if (selected == SigningSealType.UNSUPPORTED) {
        throw new SigningException("印章类型不支持");
      }
    }
    return provider.list(pageNum, pageSize, selected);
  }

  /**
   * 在发起签署或创建流程前核实选章。
   *
   * @param sealId 当前选择的印章编号
   * @return 已确认归属且启用的机构印章
   * @throws SigningException 印章不属于我方、类型不支持或不可用时
   */
  public SigningSeal requireSelectable(String sealId) {
    String checked = required(sealId, 100, "印章编号");
    SigningSeal seal = provider.detail(checked);
    if (seal == null
        || !checked.equals(seal.getId())
        || !seal.isSelectable()
        || seal.getStatus() != SigningSealStatus.ENABLED
        || seal.getType() == null
        || seal.getType() == SigningSealType.UNSUPPORTED) {
      throw new SigningException("所选印章不可用，请刷新印章列表后重新选择");
    }
    return seal;
  }

  /**
   * 读取不暴露平台临时链接的图片。
   *
   * @param sealId 印章编号
   * @return 带水印的预览图片
   */
  public SigningSealImage preview(String sealId) {
    return provider.preview(required(sealId, 100, "印章编号"));
  }
}
