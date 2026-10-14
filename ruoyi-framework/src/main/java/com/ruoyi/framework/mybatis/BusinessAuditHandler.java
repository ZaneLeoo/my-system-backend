package com.ruoyi.framework.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AuditEntity;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/** 为所有 AuditEntity 子类统一填充当前登录用户的审计信息。 */
@Component
public class BusinessAuditHandler implements MetaObjectHandler {

  /** 新增时从当前认证用户填充创建和修改信息。 */
  @Override
  public void insertFill(MetaObject metaObject) {
    if (metaObject.getOriginalObject() instanceof AuditEntity) {
      String username = currentUsername();
      LocalDateTime now = LocalDateTime.now();
      setFieldValByName("createBy", username, metaObject);
      setFieldValByName("createTime", now, metaObject);
      setFieldValByName("updateBy", username, metaObject);
      setFieldValByName("updateTime", now, metaObject);
    }
  }

  /** 更新及逻辑删除时覆盖旧审计值，记录本次真实操作者。 */
  @Override
  public void updateFill(MetaObject metaObject) {
    if (metaObject.getOriginalObject() instanceof AuditEntity) {
      setFieldValByName("updateBy", currentUsername(), metaObject);
      setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
  }

  /** 要求明确有效的当前操作者，不使用匿名账号或 system 兜底。 */
  private String currentUsername() {
    String username = SecurityUtils.getUsername();
    if (username == null || username.isBlank()) {
      throw new ServiceException("缺少有效的审计操作者", HttpStatus.UNAUTHORIZED);
    }
    return username;
  }
}
