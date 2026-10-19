package com.ruoyi.business.purchase.arrival.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** 新单经办人及部门默认值 */
@Getter
@Setter
public class ArrivalDefaults {
  /** 当前经办人 ID。 */
  private Long receiverId;

  /** 当前经办人姓名。 */
  private String receiverName;

  /** 当前部门 ID。 */
  private Long deptId;

  /** 当前部门名称。 */
  private String deptName;
}
