package com.ruoyi.business.signing.config;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.application.*;
import com.ruoyi.signing.provider.esign.*;
import com.ruoyi.signing.spi.*;
import java.util.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/** 显式装配可迁移模块，核心类无需 Spring 注解。 */
@Configuration
@EnableConfigurationProperties(SigningProperties.class)
public class SigningConfiguration {
  /** 装配固定官方端点 HTTP 客户端。 */
  @Bean
  public EsignV3Client esignClient(SigningProperties properties, SigningJsonCodec json) {
    return new EsignV3Client(properties, json);
  }

  /** 装配平台能力。 */
  @Bean
  public SigningProvider signingProvider(EsignV3Client client, SigningProperties properties) {
    return new EsignSigningProvider(client, properties);
  }

  /** 装配与签署写操作隔离的我方印章读取能力。 */
  @Bean
  public SigningSealService signingSealService(EsignV3Client client, SigningProperties properties) {
    return new SigningSealService(new EsignSealProvider(client, properties));
  }

  /** 登记开发者实现的业务场景。 */
  private Map<String, SigningBusinessAdapter> adapters(List<SigningBusinessAdapter> adapters) {
    Map<String, SigningBusinessAdapter> result =
        new LinkedHashMap<String, SigningBusinessAdapter>();
    for (SigningBusinessAdapter adapter : adapters) {
      if (result.put(adapter.businessType(), adapter) != null) {
        throw new IllegalStateException("重复签署场景");
      }
    }
    return result;
  }

  /** 装配模板映射服务。 */
  @Bean
  public DefaultSigningTemplateService signingTemplateService(
      SigningRepository repository,
      SigningProvider provider,
      SigningJsonCodec json,
      List<SigningBusinessAdapter> adapters) {
    return new DefaultSigningTemplateService(repository, provider, json, adapters(adapters));
  }

  /** 装配业务签署服务。 */
  @Bean
  public DefaultSigningService signingService(
      SigningRepository repository,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      SigningFileStore files,
      SigningJsonCodec json,
      List<SigningBusinessAdapter> adapters,
      SigningSealService seals) {
    return new DefaultSigningService(
        repository, provider, transactions, files, json, adapters(adapters), seals);
  }

  /** 装配可恢复任务处理器。 */
  @Bean
  public SigningJobProcessor signingJobProcessor(
      SigningRepository repository,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      SigningFileStore files,
      SigningTemplateService templates,
      DefaultSigningService service) {
    return new SigningJobProcessor(repository, provider, transactions, files, templates, service);
  }

  /** 装配收件箱处理器。 */
  @Bean
  public SigningEventProcessor signingEventProcessor(
      SigningRepository repository,
      SigningJsonCodec json,
      SigningProvider provider,
      SigningUnitOfWork transactions,
      DefaultSigningService service) {
    return new SigningEventProcessor(repository, json, provider, transactions, service);
  }
}
