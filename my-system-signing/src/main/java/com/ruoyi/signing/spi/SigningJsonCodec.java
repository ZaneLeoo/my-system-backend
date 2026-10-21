package com.ruoyi.signing.spi;

/** 隔离宿主 JSON 库，公共接口不出现 JsonNode。 */
public interface SigningJsonCodec {
  /**
   * 将对象编码为 JSON；不启用不可信多态类型解析。
   *
   * @param value 普通对象或集合
   * @return JSON 字符串
   */
  String write(Object value);

  /**
   * 解码为普通 Java 类。
   *
   * @param json JSON 字符串
   * @param type 预先声明的目标类型
   * @param <T> 结果类型
   * @return 解码结果
   */
  <T> T read(String json, Class<T> type);
}
