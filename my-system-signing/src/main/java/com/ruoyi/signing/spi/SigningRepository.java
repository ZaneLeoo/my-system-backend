package com.ruoyi.signing.spi;

import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.SigningJobType;
import java.util.List;

/** 宿主持久化契约，所有 SQL 值必须绑定参数。 */
public interface SigningRepository {
  /**
   * 消费事务内将已验证事件关联至受信本地任务。
   *
   * @param eventId 事件
   * @param taskId 已核对流程或定位会话的任务
   */
  void attachEvent(String eventId, String taskId);

  /**
   * 在本地事务内独占事件处理权，事务回滚后恢复待处理状态。
   *
   * @param id 事件 ID
   * @return 首次领取为 true
   */
  boolean beginEvent(String id);

  /**
   * 校验领取令牌及未到期租约。
   *
   * @param job 已领取任务
   * @return 是否仍拥有执行权
   */
  boolean owns(SigningJob job);

  /**
   * 读取待处理的回调事件。
   *
   * @return 最多二十个已验证事件
   */
  List<SigningEvent> pendingEvents();

  /**
   * 标记事件消费结果。
   *
   * @param id 事件
   * @param result DONE、IGNORED 或 PENDING
   */
  void completeEvent(String id, String result);

  /**
   * 根据定位会话查找任务。
   *
   * @param session 定位会话
   * @return 任务或 null
   */
  SigningTask findByPositionSession(String session);

  /**
   * 读取任务。
   *
   * @param id 本地主键
   * @return 任务或 null
   */
  SigningTask findTask(String id);

  /**
   * 查询业务当前占用的签署任务。
   *
   * @param businessType 业务类型
   * @param businessId 业务 ID
   * @return 当前有效任务或 null
   */
  SigningTask findActiveTask(String businessType, String businessId);

  /**
   * 原子插入任务并取得该业务的唯一有效任务占用。
   *
   * @param task 新任务
   */
  void insertTask(SigningTask task);

  /**
   * 按版本更新，失败必须抛出冲突异常。
   *
   * @param task 新值
   * @param expectedVersion 读取版本
   */
  void updateTask(SigningTask task, long expectedVersion);

  /**
   * 查询同一业务的历史。
   *
   * @param businessType 业务类型
   * @param businessId 业务 ID
   * @return 最新记录在前
   */
  List<SigningTask> history(String businessType, String businessId);

  /**
   * 根据平台流程查找任务。
   *
   * @param environment 应用环境
   * @param flowId 平台流程
   * @return 任务或 null
   */
  SigningTask findByFlow(String environment, String flowId);

  /**
   * 插入或更新文档元信息。
   *
   * @param document 文档
   */
  void saveDocument(SigningDocument document);

  /**
   * 读取任务所有文档。
   *
   * @param taskId 任务
   * @return 文档集合
   */
  List<SigningDocument> documents(String taskId);

  /**
   * 保存当前签署方，必须在任务版本保护的事务中执行。
   *
   * @param taskId 任务
   * @param parties 双方信息
   */
  void saveParties(String taskId, List<SigningParty> parties);

  /**
   * 读取签署方。
   *
   * @param taskId 任务
   * @return 签署方
   */
  List<SigningParty> parties(String taskId);

  /**
   * 列出业务模板绑定。
   *
   * @param businessType 业务类型
   * @return 模板绑定
   */
  List<SigningTemplateBinding> bindings(String businessType);

  /**
   * 根据 ID 读取模板绑定。
   *
   * @param id 绑定 ID
   * @return 绑定或 null
   */
  SigningTemplateBinding binding(String id);

  /**
   * 按版本保存模板绑定。
   *
   * @param binding 模板配置
   * @param expectedVersion 原版本，新建为 -1
   */
  void saveBinding(SigningTemplateBinding binding, long expectedVersion);

  /**
   * 原子登记持久化操作。
   *
   * @param taskId 任务
   * @param type 操作
   */
  void enqueue(String taskId, SigningJobType type);

  /** 立即安排一次平台状态同步，不等待周期轮询间隔。 */
  void enqueueNow(String taskId, SigningJobType type);

  /**
   * 领取一个任务，过期的非幂等创建须标记 UNKNOWN。
   *
   * @return 带租约令牌的任务或 null
   */
  SigningJob claim();

  /**
   * 结束当前租约；必要时保留可重试任务。
   *
   * @param job 已领取任务
   * @param state DONE、FAILED、UNKNOWN 或 PENDING
   * @param message 脱敏提示
   */
  void finish(SigningJob job, String state, String message);

  /**
   * 幂等写入已验证事件并安排处理。
   *
   * @param event 事件
   * @return 首次写入为 true
   */
  boolean insertEvent(SigningEvent event);

  /**
   * 读取任务时间线。
   *
   * @param taskId 任务
   * @return 不包含原始回调内容的事件
   */
  List<SigningEvent> events(String taskId);
}
