package com.ruoyi.signing.application;

import com.ruoyi.signing.api.SigningException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 公共参数、摘要与时钟工具，全部兼容 Java 8。 */
public final class SigningSupport {
  private SigningSupport() {}

  /** 创建保留字段顺序的 JSON 对象。 */
  public static Map<String, Object> map(Object... entries) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    for (int i = 0; i < entries.length; i += 2) {
      result.put((String) entries[i], entries[i + 1]);
    }
    return result;
  }

  /** 返回新主键。 */
  public static String id() {
    return UUID.randomUUID().toString();
  }

  /** 获取北京时间。 */
  public static String now() {
    return LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  /** 校验非空文本与最大长度。 */
  public static String required(String value, int max, String label) {
    if (value == null || value.trim().isEmpty() || value.trim().length() > max) {
      throw new SigningException(label + "不能为空且不能超过" + max + "个字符");
    }
    return value.trim();
  }

  /** 计算 SHA-256 十六进制摘要。 */
  public static String hash(byte[] bytes) {
    try {
      return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** 计算文本摘要。 */
  public static String hash(String value) {
    return hash(value.getBytes(StandardCharsets.UTF_8));
  }

  /** 转换二进制为小写十六进制。 */
  public static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte value : bytes) {
      result.append(String.format("%02x", value & 255));
    }
    return result.toString();
  }

  /** 读取嵌套对象，非对象返回空集合。 */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> object(Object value) {
    return value instanceof Map
        ? (Map<String, Object>) value
        : Collections.<String, Object>emptyMap();
  }

  /** 读取数组，非数组返回空集合。 */
  @SuppressWarnings("unchecked")
  public static List<Object> array(Object value) {
    return value instanceof List ? (List<Object>) value : Collections.emptyList();
  }

  /** 读取必需平台字段。 */
  public static String text(Map<String, Object> value, String key) {
    Object result = value.get(key);
    return numberText(result);
  }

  /** 兼容不同 JSON 库将整数解码为 Integer、Long 或 Double，避免把 0.0 误判为错误码。 */
  public static String numberText(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof Number) {
      return new java.math.BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
    }
    return String.valueOf(value);
  }
}
