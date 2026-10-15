package com.ruoyi.framework.config;

import com.ruoyi.common.constant.DateTimeConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

/** 补充 Java 时间类型的统一 JSON 输出约定，保留 Spring Boot 原有配置。 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {
  /** LocalDateTime 按北京时间的本地钟面值输出到秒，不重复进行时区偏移。 */
  @Bean
  public JsonMapperBuilderCustomizer localDateTimeCustomizer() {
    SimpleModule module = new SimpleModule("business-date-time");
    module.addSerializer(
        LocalDateTime.class,
        new LocalDateTimeSerializer(
            DateTimeFormatter.ofPattern(DateTimeConstants.DATE_TIME_PATTERN)));
    return builder -> builder.addModule(module);
  }
}
