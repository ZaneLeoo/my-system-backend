package com.ruoyi.signing;

import com.google.gson.Gson;
import com.ruoyi.signing.model.SigningTask;
import com.ruoyi.signing.spi.SigningJsonCodec;
import java.io.*;
import java.net.*;
import java.util.*;

/** 使用测试专用 Java 8 JSON 库验证扩展边界，不给核心引入运行时依赖。 */
public final class SigningJava8Smoke {
  private SigningJava8Smoke() {}

  /**
   * 验证普通实体及泛型快照在真实 Java 8 上往返，另测文件流和公开站点 TLS。
   *
   * @param args --https 启用公开文档站握手，不调用签署业务
   * @throws Exception 兼容性检查失败
   */
  public static void main(String[] args) throws Exception {
    if (!"1.8".equals(System.getProperty("java.specification.version"))) {
      throw new IllegalStateException("This check must run on Java 8");
    }
    Gson gson = new Gson();
    SigningJsonCodec codec =
        new SigningJsonCodec() {
          public String write(Object value) {
            return gson.toJson(value);
          }

          public <T> T read(String json, Class<T> type) {
            return gson.fromJson(json, type);
          }
        };
    SigningTask original = new SigningTask();
    original.setId("java8-check");
    original.getSnapshot().put("name", "合同");
    original.getSnapshot().put("zero", 0);
    original.getSnapshot().put("enabled", false);
    SigningTask restored = codec.read(codec.write(original), SigningTask.class);
    if (!original.getId().equals(restored.getId())
        || !Boolean.FALSE.equals(restored.getSnapshot().get("enabled"))
        || !"合同".equals(restored.getSnapshot().get("name"))
        || ((Number) restored.getSnapshot().get("zero")).intValue() != 0) {
      throw new IllegalStateException("JSON adapter contract failed");
    }
    File file = File.createTempFile("signing-java8-", ".pdf");
    try {
      try (OutputStream out = new FileOutputStream(file)) {
        out.write(new byte[] {37, 80, 68, 70, 45});
      }
      try (InputStream in = new FileInputStream(file)) {
        if (in.read() != 37) {
          throw new IllegalStateException("File stream failed");
        }
      }
    } finally {
      if (!file.delete()) {
        file.deleteOnExit();
      }
    }
    if (args.length > 0 && "--https".equals(args[0])) {
      HttpURLConnection connection =
          (HttpURLConnection) new URL("https://open.esign.cn/doc/opendoc").openConnection();
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      try {
        if (connection.getResponseCode() != 200) {
          throw new IllegalStateException("TLS document-site request failed");
        }
      } finally {
        connection.disconnect();
      }
    }
    System.out.println(
        "Java 8 JSON / stream / compatibility smoke passed"
            + (args.length > 0 ? " with HTTPS" : ""));
  }
}
