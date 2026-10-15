package com.ruoyi.framework.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ruoyi.common.core.domain.AuditEntity;
import com.ruoyi.common.core.domain.BaseEntity;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

/** 验证实际 Boot 序列化配置及旧式日期、请求解析的兼容性。 */
@ResourceLock("java.util.TimeZone.default")
class JacksonConfigTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
          .withUserConfiguration(JacksonConfig.class)
          .withPropertyValues(
              "spring.jackson.time-zone=GMT+8", "spring.jackson.date-format=yyyy-MM-dd HH:mm:ss");

  /** 继承的审计时间与普通 LocalDateTime 均输出到秒，空值保持 null。 */
  @Test
  void localDateTimeUsesSharedFormatWithoutShiftingOrFraction() {
    runner.run(
        context -> {
          assertNull(context.getStartupFailure());
          JsonMapper mapper = context.getBean(JsonMapper.class);
          AuditProbe probe = new AuditProbe();
          LocalDateTime time = LocalDateTime.of(2026, 2, 2, 12, 0, 0, 123456789);
          probe.setCreateTime(time);
          Map<?, ?> result = mapper.readValue(mapper.writeValueAsString(probe), Map.class);
          assertEquals("2026-02-02 12:00:00", result.get("createTime"));
          assertTrue(result.containsKey("updateTime"));
          assertNull(result.get("updateTime"));
          assertEquals("\"2026-02-02 12:00:00\"", mapper.writeValueAsString(time));
        });
  }

  /** 即使 JVM 默认 UTC，旧基类的 Date 仍按已有 GMT+8 配置输出。 */
  @Test
  void legacyDateKeepsBeijingFormatWithUtcJvm() {
    TimeZone previous = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      runner.run(
          context -> {
            assertNull(context.getStartupFailure());
            JsonMapper mapper = context.getBean(JsonMapper.class);
            BaseEntity entity = new BaseEntity();
            Date time = Date.from(Instant.parse("2026-02-02T04:00:00Z"));
            entity.setCreateTime(time);
            entity.setUpdateTime(time);
            Map<?, ?> result = mapper.readValue(mapper.writeValueAsString(entity), Map.class);
            assertEquals("2026-02-02 12:00:00", result.get("createTime"));
            assertEquals("2026-02-02 12:00:00", result.get("updateTime"));
            assertEquals("\"2026-02-02 12:00:00\"", mapper.writeValueAsString(time));
          });
    } finally {
      TimeZone.setDefault(previous);
    }
  }

  /** 输出格式变更不影响原有 ISO LocalDateTime 请求解析。 */
  @Test
  void existingIsoInputRemainsSupported() {
    runner.run(
        context -> {
          JsonMapper mapper = context.getBean(JsonMapper.class);
          assertEquals(
              LocalDateTime.of(2026, 2, 2, 12, 0),
              mapper.readValue("\"2026-02-02T12:00:00\"", LocalDateTime.class));
        });
  }

  /** 验证公共审计字段的序列化，不依赖具体业务实体。 */
  public static class AuditProbe extends AuditEntity {}
}
