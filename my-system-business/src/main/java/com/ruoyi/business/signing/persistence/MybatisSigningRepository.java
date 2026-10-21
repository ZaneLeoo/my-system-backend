package com.ruoyi.business.signing.persistence;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.business.signing.persistence.entity.SigningJobRow;
import com.ruoyi.business.signing.persistence.mapper.SigningMapper;
import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.spi.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** 基于 MP 会话工厂中的 MyBatis Mapper 实现，JSON 仅保存受控快照。 */
@Repository
public class MybatisSigningRepository implements SigningRepository {
  /** {@inheritDoc} */
  public void attachEvent(String eventId, String taskId) {
    mapper.attachEvent(eventId, taskId);
  }

  /** {@inheritDoc} */
  public boolean beginEvent(String id) {
    return mapper.beginEvent(id) == 1;
  }

  private final SigningMapper mapper;
  private final SigningJsonCodec json;
  private final TransactionTemplate transactions;

  /** 注入专用 Mapper 和宿主事务。 */
  public MybatisSigningRepository(
      SigningMapper mapper,
      SigningJsonCodec json,
      org.springframework.transaction.PlatformTransactionManager manager) {
    this.mapper = mapper;
    this.json = json;
    this.transactions = new TransactionTemplate(manager);
  }

  /** 解码可空单值。 */
  private <T> T read(String value, Class<T> type) {
    return value == null ? null : json.read(value, type);
  }

  /** 解码预先声明的普通类，禁止动态类型名。 */
  private <T> List<T> list(List<String> rows, Class<T> type) {
    List<T> result = new ArrayList<T>();
    for (String row : rows) {
      result.add(read(row, type));
    }
    return result;
  }

  /** {@inheritDoc} */
  public SigningTask findTask(String id) {
    return read(mapper.task(id), SigningTask.class);
  }

  /** {@inheritDoc} */
  public SigningTask findActiveTask(String type, String id) {
    return read(mapper.activeTask(type, id), SigningTask.class);
  }

  /** {@inheritDoc} */
  public void insertTask(SigningTask task) {
    try {
      mapper.insertTask(task, json.write(task));
    } catch (DuplicateKeyException e) {
      throw new SigningException("该单据已有有效签署任务，请查看签署记录");
    }
  }

  /** {@inheritDoc} */
  public void updateTask(SigningTask task, long expectedVersion) {
    Integer active =
        Arrays.asList(SigningStatus.REVOKED, SigningStatus.EXPIRED, SigningStatus.REJECTED)
                .contains(task.getStatus())
            ? null
            : 1;
    if (mapper.updateTask(task, json.write(task), expectedVersion, active) != 1) {
      throw new SigningException("签署任务已变化，请刷新后重试");
    }
  }

  /** {@inheritDoc} */
  public List<SigningTask> history(String type, String id) {
    return list(mapper.history(type, id), SigningTask.class);
  }

  /** {@inheritDoc} */
  public SigningTask findByFlow(String environment, String flowId) {
    return read(mapper.byFlow(environment, flowId), SigningTask.class);
  }

  /** {@inheritDoc} */
  public SigningTask findByPositionSession(String session) {
    return read(mapper.bySession(session), SigningTask.class);
  }

  /** {@inheritDoc} */
  public void saveDocument(SigningDocument document) {
    mapper.document(document, json.write(document));
  }

  /** {@inheritDoc} */
  public List<SigningDocument> documents(String id) {
    return list(mapper.documents(id), SigningDocument.class);
  }

  /** {@inheritDoc} */
  public void saveParties(String id, List<SigningParty> parties) {
    mapper.clearParties(id);
    for (SigningParty party : parties) {
      mapper.party(party, json.write(party));
    }
  }

  /** {@inheritDoc} */
  public List<SigningParty> parties(String id) {
    return list(mapper.parties(id), SigningParty.class);
  }

  /** {@inheritDoc} */
  public List<SigningTemplateBinding> bindings(String type) {
    return list(mapper.bindings(type), SigningTemplateBinding.class);
  }

  /** {@inheritDoc} */
  public SigningTemplateBinding binding(String id) {
    return read(mapper.binding(id), SigningTemplateBinding.class);
  }

  /** {@inheritDoc} */
  public void saveBinding(SigningTemplateBinding binding, long version) {
    if (version < 0) {
      mapper.insertBinding(binding, json.write(binding));
    } else if (mapper.updateBinding(binding, json.write(binding), version) != 1) {
      throw new SigningException("模板配置已变化，请刷新后重试");
    }
  }

  /** 将北京时间延后指定秒数。 */
  private String later(long seconds) {
    return LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        .plusSeconds(seconds)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  /** {@inheritDoc} */
  public void enqueue(String taskId, SigningJobType type) {
    if (type != SigningJobType.SYNC) {
      mapper.cancelPendingSync(taskId);
    }
    int inserted =
        mapper.enqueue(id(), taskId, type.name(), later(type == SigningJobType.SYNC ? 30 : 0));
    if (inserted != 1 && type != SigningJobType.SYNC) {
      throw new SigningException("已有后台操作正在执行，请稍后重试");
    }
  }

  /** {@inheritDoc} */
  public void enqueueNow(String taskId, SigningJobType type) {
    if (type != SigningJobType.SYNC) {
      throw new IllegalArgumentException("仅平台状态同步支持立即入队");
    }
    String nextTime = later(0);
    mapper.expeditePendingSync(taskId, nextTime);
    int inserted = mapper.enqueue(id(), taskId, type.name(), nextTime);
    if (inserted != 1 && mapper.pendingSyncCount(taskId) == 0) {
      throw new SigningException("已有后台操作正在执行，请稍后重试");
    }
  }

  /** {@inheritDoc} */
  public SigningJob claim() {
    return transactions.execute(
        status -> {
          SigningJobRow job = mapper.candidate();
          if (job == null) {
            return null;
          }
          if ("RUNNING".equals(job.getStatus()) && job.getType() == SigningJobType.CREATE_FLOW) {
            mapper.unknown(job.getId());
            SigningTask task = findTask(job.getTaskId());
            long version = task.getVersion();
            task.setVersion(Math.addExact(version, 1));
            task.setOperationStatus(SigningOperationStatus.UNKNOWN);
            task.setLastError("创建进程中断，请核对平台结果后关联流程 ID");
            task.setUpdateBy("signing:worker");
            task.setUpdateTime(now());
            updateTask(task, version);
            return null;
          }
          job.setToken(id());
          mapper.claim(job, later(300));
          job.setAttempt(job.getAttempt() + 1);
          return job;
        });
  }

  /** {@inheritDoc} */
  public boolean owns(SigningJob job) {
    return job.getId().equals(mapper.owns(job));
  }

  /** {@inheritDoc} */
  public void finish(SigningJob job, String state, String message) {
    if (mapper.finish(
            job,
            state,
            message,
            later(
                job.getType() == SigningJobType.PREPARE
                    ? 3
                    : Math.min(300, 10L * (1L << Math.min(job.getAttempt(), 5)))))
        != 1) {
      throw new SigningException("后台执行租约已变化");
    }
  }

  /** {@inheritDoc} */
  public boolean insertEvent(SigningEvent event) {
    return mapper.event(event, "esign:callback".equals(event.getActor()) ? "PENDING" : "DONE") == 1;
  }

  /** {@inheritDoc} */
  public List<SigningEvent> events(String id) {
    return mapper.events(id);
  }

  /** {@inheritDoc} */
  public List<SigningEvent> pendingEvents() {
    return mapper.pendingEvents();
  }

  /** {@inheritDoc} */
  public void completeEvent(String id, String result) {
    mapper.completeEvent(id, result, later(30));
  }
}
