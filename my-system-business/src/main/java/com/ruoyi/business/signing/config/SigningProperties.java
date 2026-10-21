package com.ruoyi.business.signing.config;

import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 宿主私有存储及环境配置，不作为接口返回对象。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "signing")
public class SigningProperties extends EsignConfiguration {
  /** 与公开上传目录隔离的文件目录。 */
  private String privateDirectory = "./data/signing-private";
}
