package com.ruoyi.signing.provider.esign;

import com.ruoyi.signing.application.SigningSupport;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 签署及文件模板通知的 HMAC 验证，不复用出站签名格式。 */
public final class EsignCallbackVerifier {
  private final EsignConfiguration config;

  /** 注入当前应用，禁止根据回调动态选择任意密钥。 */
  public EsignCallbackVerifier(EsignConfiguration config) {
    this.config = config;
  }

  /**
   * 验证原始字节及按键排序的回调查询值；旧事件重试不因原事件时间而误拒绝。
   *
   * @param appId 通知应用 ID
   * @param algorithm 平台算法名
   * @param timestamp 签名时间戳
   * @param signature 小写或大写十六进制签名
   * @param query 回调地址已有的唯一查询参数
   * @param body 未经重编码的原始请求体
   * @return 是否有效
   */
  public boolean verify(
      String appId,
      String algorithm,
      String timestamp,
      String signature,
      Map<String, String> query,
      byte[] body) {
    if (!config.isEnabled()
        || config.getAppSecret().isEmpty()
        || !config.getAppId().equals(appId)
        || !"hmac-sha256".equalsIgnoreCase(algorithm)
        || timestamp == null
        || !timestamp.matches("[0-9]{10,16}")
        || signature == null
        || !signature.matches("[0-9a-fA-F]{64}")
        || body == null
        || body.length > 1024 * 1024) {
      return false;
    }
    StringBuilder prefix = new StringBuilder(timestamp);
    for (String value : new TreeMap<String, String>(query).values()) {
      prefix.append(value);
    }
    byte[] head = prefix.toString().getBytes(StandardCharsets.UTF_8);
    byte[] data = new byte[head.length + body.length];
    System.arraycopy(head, 0, data, 0, head.length);
    System.arraycopy(body, 0, data, head.length, body.length);
    String expected = SigningSupport.hex(EsignRequestSigner.hmac(config.getAppSecret(), data));
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.US_ASCII),
        signature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
  }
}
