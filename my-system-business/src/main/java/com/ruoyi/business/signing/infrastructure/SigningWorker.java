package com.ruoyi.business.signing.infrastructure;

import com.ruoyi.signing.application.*;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** 开启集成后运行；数据库租约允许多实例部署，禁用时不创建调度器。 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "signing", name = "enabled", havingValue = "true")
public class SigningWorker {
  private static final Logger LOG = LoggerFactory.getLogger(SigningWorker.class);
  private final SigningJobProcessor jobs;
  private final SigningEventProcessor events;

  /** 注入平台无关处理器。 */
  public SigningWorker(SigningJobProcessor jobs, SigningEventProcessor events) {
    this.jobs = jobs;
    this.events = events;
  }

  /** 每轮有界消费，不把原始异常或签署数据写入日志。 */
  @Scheduled(fixedDelay = 3000, initialDelay = 5000)
  public void tick() {
    try {
      events.runOnce();
      for (int count = 0; count < 5; count++) {
        if (!jobs.runOnce()) {
          break;
        }
      }
    } catch (RuntimeException error) {
      LOG.warn("签署后台任务暂未完成，将按持久化状态恢复；异常类型：{}", error.getClass().getSimpleName());
    }
  }
}
