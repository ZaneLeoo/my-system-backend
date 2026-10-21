package com.ruoyi.signing.provider.esign;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.application.SigningSupport;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import com.ruoyi.signing.spi.SigningJsonCodec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

/** Java 8 HTTPS 客户端，不自动重试外部写操作，不输出凭证和临时文件 URL。 */
public class EsignV3Client {
  protected final EsignConfiguration config;
  private final SigningJsonCodec json;
  private final EsignRequestSigner signer = new EsignRequestSigner();

  /** 注入宿主 JSON 编解码及服务端配置。 */
  public EsignV3Client(EsignConfiguration config, SigningJsonCodec json) {
    this.config = config;
    this.json = json;
  }

  /** 校验开关及配置，禁用状态下不会发出网络请求。 */
  public void checkConfigured() {
    if (!config.isEnabled()
        || config.getAppId().trim().isEmpty()
        || config.getAppSecret().trim().isEmpty()) {
      throw new SigningException("电子签署尚未启用或未配置应用凭证");
    }
    if (!"SANDBOX".equals(config.getEnvironment())
        && !"PRODUCTION".equals(config.getEnvironment())) {
      throw new SigningException("电子签署环境不合法");
    }
  }

  /**
   * 调用受控 V3 路径并解析业务码。
   *
   * @param method HTTP 方法
   * @param path 受控路径及排序查询串
   * @param body 请求对象或 null
   * @param uncertainOnFailure 是否为不可盲目重试的创建操作
   * @return data 对象
   */
  public Map<String, Object> call(
      String method, String path, Object body, boolean uncertainOnFailure) {
    checkConfigured();
    if (!path.startsWith("/v3/") || path.contains("\r") || path.contains("\n")) {
      throw new SigningException("非法平台路径");
    }
    byte[] bytes = body == null ? new byte[0] : json.write(body).getBytes(StandardCharsets.UTF_8);
    String host =
        "PRODUCTION".equals(config.getEnvironment())
            ? "https://openapi.esign.cn"
            : "https://smlopenapi.esign.cn";
    byte[] response =
        request(
            method,
            host + path,
            bytes,
            signer.headers(
                method,
                path,
                bytes,
                config.getAppId(),
                config.getAppSecret(),
                System.currentTimeMillis()),
            2 * 1024 * 1024,
            uncertainOnFailure);
    try {
      Map<String, Object> result =
          SigningSupport.object(json.read(new String(response, StandardCharsets.UTF_8), Map.class));
      if (!result.containsKey("code")) {
        throw new SigningException("平台响应缺少结果码，请核对操作结果", uncertainOnFailure);
      }
      if (!"0".equals(SigningSupport.text(result, "code"))) {
        String errorCode = SigningSupport.text(result, "code");
        String providerMessage = SigningSupport.text(result, "message");
        if (providerMessage.trim().isEmpty()) {
          providerMessage = SigningSupport.text(result, "msg");
        }
        throw new SigningException(externalErrorMessage(errorCode, providerMessage));
      }
      Object data = result.get("data");
      return data instanceof Map ? SigningSupport.object(data) : SigningSupport.map("items", data);
    } catch (SigningException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new SigningException("平台响应格式异常，请核对操作结果", uncertainOnFailure);
    }
  }

  /** 按 e签宝平台的错误码和原始消息生成用户提示。 */
  private String externalErrorMessage(String errorCode, String providerMessage) {
    String message = providerMessage == null ? "" : providerMessage;
    if (!message.trim().isEmpty()) {
      return "e签宝平台返回错误（" + errorCode + "）： " + message;
    }
    return "e签宝平台返回错误（" + errorCode + "）： 平台未返回错误说明";
  }

  /** 上传二进制后同时校验 HTTP 状态及文件接口 errCode。 */
  public void uploadFile(String url, byte[] bytes, String md5) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-MD5", md5);
    headers.put("Content-Type", "application/octet-stream");
    byte[] response = fileRequest("PUT", url, bytes, headers);
    Map<String, Object> result =
        SigningSupport.object(json.read(new String(response, StandardCharsets.UTF_8), Map.class));
    if (!"0".equals(SigningSupport.text(result, "errCode"))) {
      throw new SigningException("平台文件上传未成功，错误码：" + SigningSupport.text(result, "errCode"));
    }
  }

  /** 编码单个路径或查询值。 */
  public static String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * 下载或上传平台返回的 HTTPS 文件地址；不携带应用鉴权头。
   *
   * @param method GET 或 PUT
   * @param url 平台响应返回的临时地址
   * @param body 文件内容
   * @param headers 文件请求头
   * @return 响应内容
   */
  public byte[] fileRequest(String method, String url, byte[] body, Map<String, String> headers) {
    checkConfigured();
    try {
      URI target = new URI(url);
      String host = target.getHost();
      if (!"https".equals(target.getScheme())
          || target.getUserInfo() != null
          || host == null
          || !(host.endsWith(".esign.cn")
              || host.endsWith(".tsign.cn")
              || host.endsWith(".aliyuncs.com"))) {
        throw new SigningException("平台返回了未允许的文件地址");
      }
      for (InetAddress address : InetAddress.getAllByName(host)) {
        if (address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()) {
          throw new SigningException("拒绝内网文件地址");
        }
      }
      return request(method, url, body, headers, 50 * 1024 * 1024, false);
    } catch (URISyntaxException | UnknownHostException e) {
      throw new SigningException("平台文件地址无法解析");
    }
  }

  /**
   * 实际网络请求，固定长度发送可避免缓冲请求的隐式重放。
   *
   * @param method 方法
   * @param url 已验证地址
   * @param bytes 原始内容
   * @param headers 头部
   * @param limit 响应上限
   * @param uncertain 传输失败是否需要人工核对
   * @return 原始响应
   */
  protected byte[] request(
      String method,
      String url,
      byte[] bytes,
      Map<String, String> headers,
      int limit,
      boolean uncertain) {
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) openConnection(url);
      connection.setRequestMethod(method);
      connection.setConnectTimeout(config.getConnectTimeout());
      connection.setReadTimeout(config.getReadTimeout());
      connection.setInstanceFollowRedirects(false);
      connection.setUseCaches(false);
      for (Map.Entry<String, String> header : headers.entrySet()) {
        connection.setRequestProperty(header.getKey(), header.getValue());
      }
      if (!"GET".equals(method)) {
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream out = connection.getOutputStream()) {
          out.write(bytes);
        }
      }
      int status = connection.getResponseCode();
      if (status < 200 || status >= 300) {
        if (status >= 400
            && status < 500
            && (url.startsWith("https://smlopenapi.esign.cn/v3/")
                || url.startsWith("https://openapi.esign.cn/v3/"))) {
          try (InputStream in = connection.getErrorStream()) {
            throw platformHttpFailure(status, readLimited(in, 2 * 1024 * 1024), uncertain);
          }
        }
        throw new SigningException(
            "平台 HTTP 请求失败：" + status, uncertain && (status < 400 || status >= 500));
      }
      try (InputStream in = connection.getInputStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
          if (out.size() + count > limit) {
            throw new SigningException("平台响应超过大小限制", uncertain);
          }
          out.write(buffer, 0, count);
        }
        return out.toByteArray();
      }
    } catch (IOException e) {
      throw new SigningException("平台网络请求失败，请稍后核对状态", uncertain);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /** 读取有界响应，避免错误页或异常图片耗尽内存。 */
  private byte[] readLimited(InputStream in, int limit) throws IOException {
    if (in == null) {
      return new byte[0];
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int count;
      while ((count = in.read(buffer)) != -1) {
        if (out.size() + count > limit) {
          throw new SigningException("平台响应超过大小限制");
        }
        out.write(buffer, 0, count);
      }
      return out.toByteArray();
    }
  }

  /** HTTP 明确拒绝时仍保留平台原始业务码和 message，非结构化错误使用 HTTP 状态。 */
  protected SigningException platformHttpFailure(int status, byte[] body, boolean uncertain) {
    if (status >= 400 && status < 500 && body.length > 0) {
      try {
        Map<String, Object> data =
            SigningSupport.object(json.read(new String(body, StandardCharsets.UTF_8), Map.class));
        String code = SigningSupport.text(data, "code");
        if (!code.isEmpty() && !"0".equals(code)) {
          String message = SigningSupport.text(data, "message");
          if (message.trim().isEmpty()) {
            message = SigningSupport.text(data, "msg");
          }
          return new SigningException(externalErrorMessage(code, message));
        }
      } catch (RuntimeException error) {
        // HTML、空响应等不能当作平台明确的业务错误。
      }
    }
    return new SigningException(
        "平台 HTTP 请求失败：" + status, uncertain && (status < 400 || status >= 500));
  }

  /** 根据配置直连或通过 SOCKS 代理建立 HTTPS 连接。 */
  private URLConnection openConnection(String url) throws IOException {
    URL target = new URL(url);
    String proxyHost = config.getSocksProxyHost();
    if (proxyHost == null || proxyHost.trim().isEmpty()) {
      return target.openConnection();
    }
    int proxyPort = config.getSocksProxyPort();
    if (proxyPort < 1 || proxyPort > 65535) {
      throw new SigningException("电子签署 SOCKS 代理端口不合法");
    }
    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost.trim(), proxyPort));
    HttpsURLConnection connection = (HttpsURLConnection) target.openConnection(Proxy.NO_PROXY);
    connection.setSSLSocketFactory(
        new SocksRemoteDnsSocketFactory(
            proxy, target, config.getConnectTimeout(), config.getReadTimeout()));
    return connection;
  }
}
