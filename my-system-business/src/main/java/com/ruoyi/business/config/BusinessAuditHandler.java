package com.ruoyi.business.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import com.ruoyi.common.utils.SecurityUtils;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/** 仅为新业务实体填充审计信息，不改变若依已有实体的写入方式。 */
@Component
public class BusinessAuditHandler implements MetaObjectHandler {

  /** 新增时从当前认证用户填充创建和修改信息。 */
  @Override
  public void insertFill(MetaObject metaObject) {
    if (metaObject.getOriginalObject() instanceof ItemCategory) {
      String username = SecurityUtils.getUsername();
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
    if (metaObject.getOriginalObject() instanceof ItemCategory) {
      setFieldValByName("updateBy", SecurityUtils.getUsername(), metaObject);
      setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
  }
}
