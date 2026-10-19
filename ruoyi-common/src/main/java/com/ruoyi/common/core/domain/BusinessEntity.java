package com.ruoyi.common.core.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.ruoyi.common.constant.LogicDeleteConstants;
import lombok.Getter;
import lombok.Setter;

/** 需要逻辑删除的业务持久化基类；是否允许删除由具体业务服务校验。 */
@Getter
@Setter
public abstract class BusinessEntity extends AuditEntity {
  /** 逻辑删除标记。 */
  @TableLogic(value = LogicDeleteConstants.NOT_DELETED, delval = LogicDeleteConstants.DELETED)
  private String delFlag = LogicDeleteConstants.NOT_DELETED;
}
