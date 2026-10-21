package com.ruoyi.signing.api;

/** 可迁移的签署异常，不暴露平台原始响应或密钥。 */
public class SigningException extends RuntimeException {
  /** 是否无法确定外部操作结果，调用方不得直接重试创建。 */
  private final boolean uncertain;

  /** 创建业务错误。 */
  public SigningException(String message) {
    this(message, false);
  }

  /** 创建包含结果不确定性的错误。 */
  public SigningException(String message, boolean uncertain) {
    super(message);
    this.uncertain = uncertain;
  }

  /** 是否必须先核对平台结果。 */
  public boolean isUncertain() {
    return uncertain;
  }
}
