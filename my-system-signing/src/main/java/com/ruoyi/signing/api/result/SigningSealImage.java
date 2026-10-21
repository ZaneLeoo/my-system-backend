package com.ruoyi.signing.api.result;

import lombok.Getter;

/** 仅供鉴权预览的印章图片，不作为合同签署原件。 */
@Getter
public final class SigningSealImage {
  /** 已校验的图片内容。 */
  private final byte[] bytes;

  /** 已识别的图片 MIME 类型。 */
  private final String contentType;

  /** 保存已校验的图片及类型。 */
  public SigningSealImage(byte[] bytes, String contentType) {
    this.bytes = bytes;
    this.contentType = contentType;
  }
}
