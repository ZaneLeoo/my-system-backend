package com.ruoyi.signing.application;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;

/** 集中保护状态和乐观锁，终态不被迟到通知覆盖。 */
public final class SigningStateMachine {
  /** 是否已结束，完成状态仍占用业务唯一槽位。 */
  public boolean terminal(SigningStatus status) {
    return status != SigningStatus.DRAFT && status != SigningStatus.SIGNING;
  }

  /** 校验用户读取的任务版本。 */
  public void version(SigningTask task, long version) {
    if (task == null) {
      throw new SigningException("签署任务不存在");
    }
    if (task.getVersion() != version) {
      throw new SigningException("签署任务已变化，请刷新后重试");
    }
  }

  /** 确认任务允许修改。 */
  public void editable(SigningTask task) {
    if (task.getStatus() != SigningStatus.DRAFT
        || task.getFlowId() != null
        || task.getOperationStatus() == SigningOperationStatus.QUEUED
        || task.getOperationStatus() == SigningOperationStatus.RUNNING
        || task.getOperationStatus() == SigningOperationStatus.UNKNOWN) {
      throw new SigningException("当前签署任务不允许修改");
    }
  }

  /** 接受权威查询状态；结束后不能回退到其他状态。 */
  public void transition(SigningTask task, SigningStatus next) {
    if (terminal(task.getStatus()) && next != task.getStatus()) {
      throw new SigningException("平台状态与本地终态冲突，需人工核对");
    }
    if (task.getStatus() == SigningStatus.SIGNING && next == SigningStatus.DRAFT) {
      throw new SigningException("拒绝签署状态回退");
    }
    task.setStatus(next);
  }
}
