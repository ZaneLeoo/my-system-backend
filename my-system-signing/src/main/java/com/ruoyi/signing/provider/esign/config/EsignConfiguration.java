package com.ruoyi.signing.provider.esign.config;

import lombok.Getter;
import lombok.Setter;

/** 应用配置，密钥只由宿主环境注入，禁止返回前端。 */
@Getter
@Setter
public class EsignConfiguration {
  /** 未配置时默认关闭。 */
  private boolean enabled;

  /** SANDBOX 或 PRODUCTION。 */
  private String environment = "SANDBOX";

  /** e签宝应用 ID。 */
  private String appId = "";

  /** e签宝应用密钥。 */
  private String appSecret = "";

  /** 公网 HTTPS 签署通知地址。 */
  private String callbackUrl = "";

  /** 我方主体名称。 */
  private String organizationName = "";

  /** 我方平台机构 ID。 */
  private String organizationId = "";

  /** 建连超时毫秒。 */
  private int connectTimeout = 10000;

  /** 读取超时毫秒。 */
  private int readTimeout = 30000;

  /** 可选 SOCKS 代理地址；留空时直连 e签宝。 */
  private String socksProxyHost = "";

  /** SOCKS 代理端口。 */
  private int socksProxyPort = 1080;
}
