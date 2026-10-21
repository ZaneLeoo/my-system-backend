package com.ruoyi.business.signing.persistence.mapper;

import com.ruoyi.business.signing.dto.SigningTaskQuery;
import com.ruoyi.signing.model.*;
import java.util.List;
import org.apache.ibatis.annotations.*;

/** 签署专用持久化；后台显式审计，不触发登录身份填充。 */
@Mapper
public interface SigningMapper {
  /**
   * 分页读取已授权业务的任务；来源类型始终与权限白名单取交集。
   *
   * @param query 状态与场景筛选
   * @param types 非空授权场景白名单
   * @return 当前页任务 JSON，分页由调用方管理
   */
  @Select({
    "<script>SELECT payload FROM sign_task WHERE business_type IN",
    "<foreach collection='types' item='type' open='(' separator=',' close=')'>#{type}</foreach>",
    "<if test='query.businessType != null and query.businessType != &quot;&quot;'>AND business_type=#{query.businessType}</if>",
    "<if test='query.status != null'>AND status=#{query.status}</if>",
    "ORDER BY create_time DESC,task_id DESC</script>"
  })
  List<String> taskList(@Param("query") SigningTaskQuery query, @Param("types") List<String> types);

  /** 只在事件消费事务内关联已核验的任务。 */
  @Update(
      "UPDATE sign_event SET task_id=#{taskId} WHERE event_id=#{eventId} AND status='PROCESSING'")
  void attachEvent(@Param("eventId") String eventId, @Param("taskId") String taskId);

  /** 在消费事务内领取事件，失败回滚而不丢失通知。 */
  @Update("UPDATE sign_event SET status='PROCESSING' WHERE event_id=#{id} AND status='PENDING'")
  int beginEvent(String id);

  /** 读取任务 JSON，普通读取不持锁。 */
  @Select("SELECT payload FROM sign_task WHERE task_id=#{id}")
  String task(String id);

  /** 查询业务当前占用的唯一签署任务。 */
  @Select(
      "SELECT payload FROM sign_task WHERE business_type=#{type} AND business_id=#{businessId} AND active_flag=1 LIMIT 1")
  String activeTask(@Param("type") String type, @Param("businessId") String businessId);

  /** 任务历史只读取当前业务，最多一百条。 */
  @Select(
      "SELECT payload FROM sign_task WHERE business_type=#{type} AND business_id=#{businessId} ORDER BY create_time DESC,task_id DESC LIMIT 100")
  List<String> history(@Param("type") String type, @Param("businessId") String businessId);

  /** 由平台流程确定本地关联，防止回调伪造业务主键。 */
  @Select(
      "SELECT payload FROM sign_task WHERE provider_environment=#{environment} AND flow_id=#{flowId}")
  String byFlow(@Param("environment") String environment, @Param("flowId") String flowId);

  /** 定位回调必须匹配当前会话。 */
  @Select("SELECT payload FROM sign_task WHERE position_session=#{session}")
  String bySession(String session);

  /** 插入时依靠唯一索引抢占业务签署槽位。 */
  @Insert(
      "INSERT INTO sign_task(task_id,business_type,business_id,provider_environment,flow_id,status,active_flag,position_session,version,payload,create_by,create_time,update_by,update_time) VALUES(#{task.id},#{task.businessType},#{task.businessId},#{task.providerEnvironment},#{task.flowId},#{task.status},1,#{task.positionSession},#{task.version},#{payload},#{task.createBy},#{task.createTime},#{task.updateBy},#{task.updateTime})")
  void insertTask(@Param("task") SigningTask task, @Param("payload") String payload);

  /** 原子比较版本，释放的终态槽位使用 NULL 保留全部历史。 */
  @Update(
      "UPDATE sign_task SET flow_id=#{task.flowId},status=#{task.status},active_flag=#{active},position_session=#{task.positionSession},version=#{task.version},payload=#{payload},update_by=#{task.updateBy},update_time=#{task.updateTime} WHERE task_id=#{task.id} AND version=#{version}")
  int updateTask(
      @Param("task") SigningTask task,
      @Param("payload") String payload,
      @Param("version") long version,
      @Param("active") Integer active);

  /** 保存文档，历史原件和已签文件不覆盖。 */
  @Insert(
      "INSERT INTO sign_document(document_id,task_id,kind,payload,create_time) VALUES(#{document.id},#{document.taskId},#{document.kind},#{payload},NOW()) ON DUPLICATE KEY UPDATE payload=VALUES(payload)")
  void document(@Param("document") SigningDocument document, @Param("payload") String payload);

  /** 读取文档元信息。 */
  @Select("SELECT payload FROM sign_document WHERE task_id=#{id} ORDER BY create_time,document_id")
  List<String> documents(String id);

  /** 在主任务版本保护下替换当前参与方。 */
  @Delete("DELETE FROM sign_party WHERE task_id=#{id}")
  void clearParties(String id);

  /** 保存当前参与方，历史变化由事件留痕。 */
  @Insert(
      "INSERT INTO sign_party(party_id,task_id,payload) VALUES(#{party.id},#{party.taskId},#{payload})")
  void party(@Param("party") SigningParty party, @Param("payload") String payload);

  /** 返回双方配置。 */
  @Select("SELECT payload FROM sign_party WHERE task_id=#{id} ORDER BY party_id")
  List<String> parties(String id);

  /** 读取模板绑定。 */
  @Select("SELECT payload FROM sign_template_binding WHERE binding_id=#{id}")
  String binding(String id);

  /** 列出指定场景模板。 */
  @Select(
      "SELECT payload FROM sign_template_binding WHERE business_type=#{type} ORDER BY name,binding_id")
  List<String> bindings(String type);

  /** 新建模板绑定。 */
  @Insert(
      "INSERT INTO sign_template_binding(binding_id,business_type,name,version,payload,create_time,update_time) VALUES(#{binding.id},#{binding.businessType},#{binding.name},#{binding.version},#{payload},NOW(),NOW())")
  void insertBinding(
      @Param("binding") SigningTemplateBinding binding, @Param("payload") String payload);

  /** 按版本更新模板。 */
  @Update(
      "UPDATE sign_template_binding SET name=#{binding.name},version=#{binding.version},payload=#{payload},update_time=NOW() WHERE binding_id=#{binding.id} AND business_type=#{binding.businessType} AND version=#{version}")
  int updateBinding(
      @Param("binding") SigningTemplateBinding binding,
      @Param("payload") String payload,
      @Param("version") long version);

  /** 去重写入事件收件箱。 */
  @Insert(
      "INSERT IGNORE INTO sign_event(event_id,task_id,deduplication_key,kind,payload,actor,status,create_time,next_run_time) VALUES(#{event.id},#{event.taskId},#{event.deduplicationKey},#{event.kind},#{event.payload},#{event.actor},#{status},NOW(),NOW())")
  int event(@Param("event") SigningEvent event, @Param("status") String status);

  /** 时间线不读取原始回调。 */
  @Select(
      "SELECT event_id AS id,task_id,kind,actor,create_time FROM sign_event WHERE task_id=#{id} ORDER BY create_time DESC,event_id DESC LIMIT 100")
  List<SigningEvent> events(String id);

  /** 延迟消费，避免未知流程通知高速循环。 */
  @Select(
      "SELECT event_id AS id,task_id,kind,payload,actor FROM sign_event WHERE status='PENDING' AND next_run_time<=NOW() ORDER BY create_time LIMIT 20")
  List<SigningEvent> pendingEvents();

  /** 成功消费或延后重试。 */
  @Update(
      "UPDATE sign_event SET status=#{status},next_run_time=#{nextTime} WHERE event_id=#{id} AND status NOT IN ('DONE','IGNORED')")
  void completeEvent(
      @Param("id") String id, @Param("status") String status, @Param("nextTime") String nextTime);

  /** 新动作可替换尚未执行的周期同步。 */
  @Update(
      "UPDATE sign_job SET status='DONE' WHERE task_id=#{id} AND type='SYNC' AND status='PENDING'")
  void cancelPendingSync(String id);

  /** 将已有的待执行平台状态同步提前到当前时间。 */
  @Update(
      "UPDATE sign_job SET next_run_time=#{nextTime} WHERE task_id=#{id} AND type='SYNC' AND status='PENDING'")
  int expeditePendingSync(@Param("id") String id, @Param("nextTime") String nextTime);

  /** 查询当前是否存在待执行的平台状态同步。 */
  @Select("SELECT COUNT(1) FROM sign_job WHERE task_id=#{id} AND type='SYNC' AND status='PENDING'")
  int pendingSyncCount(String id);

  /** 活动任务唯一索引保证单任务不会并行执行两个操作。 */
  @Insert(
      "INSERT IGNORE INTO sign_job(job_id,task_id,type,status,attempt,next_run_time,create_time) VALUES(#{id},#{taskId},#{type},'PENDING',0,#{nextTime},NOW())")
  int enqueue(
      @Param("id") String id,
      @Param("taskId") String taskId,
      @Param("type") String type,
      @Param("nextTime") String nextTime);

  /** 事务内领取到期操作。 */
  @Select(
      "SELECT job_id AS id,task_id,type,token,attempt,status FROM sign_job WHERE (status='PENDING' AND next_run_time<=NOW()) OR (status='RUNNING' AND lease_until<=NOW()) ORDER BY next_run_time,job_id LIMIT 1 FOR UPDATE")
  com.ruoyi.business.signing.persistence.entity.SigningJobRow candidate();

  /** 写入新的领取令牌。 */
  @Update(
      "UPDATE sign_job SET status='RUNNING',token=#{job.token},attempt=attempt+1,lease_until=#{lease} WHERE job_id=#{job.id}")
  void claim(@Param("job") SigningJob job, @Param("lease") String lease);

  /** 判断旧执行者是否仍有效。 */
  @Select(
      "SELECT job_id FROM sign_job WHERE job_id=#{job.id} AND token=#{job.token} AND status='RUNNING' AND lease_until>NOW() FOR UPDATE")
  String owns(@Param("job") SigningJob job);

  /** 只允许当前租约结束任务。 */
  @Update(
      "UPDATE sign_job SET status=#{state},last_error=#{message},next_run_time=#{nextTime},lease_until=NULL WHERE job_id=#{job.id} AND token=#{job.token} AND status='RUNNING'")
  int finish(
      @Param("job") SigningJob job,
      @Param("state") String state,
      @Param("message") String message,
      @Param("nextTime") String nextTime);

  /** 创建阶段超时不得重新执行。 */
  @Update(
      "UPDATE sign_job SET status='UNKNOWN',last_error='创建进程中断，必须核对平台结果' WHERE job_id=#{id} AND status='RUNNING'")
  void unknown(String id);
}
