package com.ruoyi.signing.provider.esign;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** e签宝出站请求签名，严格使用最终发送的原始字节。 */
public final class EsignRequestSigner {
  /** HMAC-SHA256 二进制结果。 */
  public static byte[] hmac(String secret, byte[] data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return mac.doFinal(data);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("签名组件不可用", e);
    }
  }

  /** 文件或 JSON 的二进制 MD5 再 Base64。 */
  public static String contentMd5(byte[] bytes) {
    try {
      return Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * 生成请求头，URL 查询串由客户端预先按字段名排序。
   *
   * @param method HTTP 方法
   * @param path 实际请求的编码路径和查询串，签名时解码查询值并按键排序
   * @param body 实际发送内容
   * @param appId 应用
   * @param secret 密钥
   * @param timestamp 当前毫秒
   * @return 鉴权头，不可写入日志
   */
  public Map<String, String> headers(
      String method, String path, byte[] body, String appId, String secret, long timestamp) {
    String md5 = body.length == 0 ? "" : contentMd5(body);
    String contentType = "application/json; charset=UTF-8";
    String canonical = method + "\n*/*\n" + md5 + "\n" + contentType + "\n\n" + canonicalUrl(path);
    Map<String, String> result = new LinkedHashMap<String, String>();
    result.put("Accept", "*/*");
    result.put("Content-Type", contentType);
    result.put("Content-MD5", md5);
    result.put("X-Tsign-Open-App-Id", appId);
    result.put("X-Tsign-Open-Auth-Mode", "Signature");
    result.put("X-Tsign-Open-Ca-Timestamp", String.valueOf(timestamp));
    result.put(
        "X-Tsign-Open-Ca-Signature",
        Base64.getEncoder()
            .encodeToString(hmac(secret, canonical.getBytes(StandardCharsets.UTF_8))));
    return result;
  }

  /** 网关按解码后的首个查询值签名；网络 URL 仍保持编码形式。 */
  private String canonicalUrl(String path) {
    int split = path.indexOf('?');
    if (split < 0) {
      return path;
    }
    SortedMap<String, String> values = new TreeMap<String, String>();
    try {
      for (String pair : path.substring(split + 1).split("&")) {
        if (!pair.isEmpty()) {
          int equals = pair.indexOf('=');
          String key = URLDecoder.decode(equals < 0 ? pair : pair.substring(0, equals), "UTF-8");
          String value = equals < 0 ? "" : URLDecoder.decode(pair.substring(equals + 1), "UTF-8");
          if (!values.containsKey(key)) {
            values.put(key, value);
          }
        }
      }
    } catch (java.io.UnsupportedEncodingException error) {
      throw new IllegalStateException(error);
    }
    StringBuilder result = new StringBuilder(path.substring(0, split));
    for (Map.Entry<String, String> entry : values.entrySet()) {
      result.append(result.indexOf("?") < 0 ? '?' : '&').append(entry.getKey());
      if (!entry.getValue().isEmpty()) {
        result.append('=').append(entry.getValue());
      }
    }
    return result.toString();
  }
}
