package com.ruoyi.business.purchase.receipt.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import jakarta.validation.constraints.*;

/** 新单经办人及部门默认值 */
public class ReceiptDefaults {
  /** 当前经办人 ID。 */
  private Long receiverId;

  /** 当前经办人姓名。 */
  private String receiverName;

  /** 当前部门 ID。 */
  private Long deptId;

  /** 当前部门名称。 */
  private String deptName;

  /** 返回当前经办人 ID。 */
  public Long getReceiverId() {
    return receiverId;
  }

  /** 设置当前经办人 ID。 */
  public void setReceiverId(Long receiverId) {
    this.receiverId = receiverId;
  }

  /** 返回当前经办人姓名。 */
  public String getReceiverName() {
    return receiverName;
  }

  /** 设置当前经办人姓名。 */
  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  /** 返回当前部门 ID。 */
  public Long getDeptId() {
    return deptId;
  }

  /** 设置当前部门 ID。 */
  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  /** 返回当前部门名称。 */
  public String getDeptName() {
    return deptName;
  }

  /** 设置当前部门名称。 */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }
}
