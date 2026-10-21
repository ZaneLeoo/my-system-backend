package com.ruoyi.signing.api;

import com.ruoyi.signing.api.command.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.*;
import java.util.List;

/** 可迁移的签署应用入口；宿主调用前完成身份和权限校验。 */
public interface SigningService {
  /**
   * 创建并占用唯一有效任务。
   *
   * @param command 受控业务来源
   * @param actor 当前操作人
   * @return 新任务
   */
  SigningTask create(CreateSigningTaskCommand command, String actor);

  /**
   * 准备上传文件或模板，异步转换。
   *
   * @param id 任务 ID
   * @param version 当前版本
   * @param name 上传名称；模板方式为空
   * @param bytes 上传内容；模板方式为空
   * @param actor 当前操作人
   * @return 已排队任务
   */
  SigningTask prepare(String id, long version, String name, byte[] bytes, String actor);

  /**
   * 保存双方信息及最终文件位置确认。
   *
   * @param id 任务 ID
   * @param version 当前版本
   * @param parties 经核验双方信息
   * @param confirmPositions 是否确认当前文件位置
   * @param actor 当前操作人
   * @return 新版本任务
   */
  SigningTask participants(
      String id, long version, List<SigningParty> parties, boolean confirmPositions, String actor);

  /**
   * 发起双阶段外部创建和启动。
   *
   * @param id 任务
   * @param version 当前版本
   * @param actor 操作人
   * @return 已排队任务
   */
  SigningTask start(String id, long version, String actor);

  /**
   * 按页面明确选择的我方签署方式发起流程，选章需实时核验。
   *
   * @param id 任务 ID
   * @param version 当前版本
   * @param autoSign 是否我方自动落章
   * @param sealId 我方印章 ID，自动落章时必填
   * @param actor 当前操作人
   * @return 已原子保存用章快照并排队的任务
   */
  SigningTask start(String id, long version, boolean autoSign, String sealId, String actor);

  /**
   * 请求撤销或同步，不允许替换未知创建结果。
   *
   * @param id 任务
   * @param version 当前版本
   * @param action sync 或 revoke
   * @param reason 撤销原因
   * @param actor 操作人
   * @return 新版本任务
   */
  SigningTask action(String id, long version, String action, String reason, String actor);

  /**
   * 获取需要由宿主脱敏的详情。
   *
   * @param id 任务
   * @return 最新任务及关联元信息
   */
  SigningDetail detail(String id);

  /**
   * 获取业务签署历史。
   *
   * @param type 业务类型
   * @param businessId 业务 ID
   * @return 签署任务
   */
  List<SigningTask> history(String type, String businessId);
}
