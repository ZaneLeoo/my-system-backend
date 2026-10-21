package com.ruoyi.business.signing.controller;

import com.ruoyi.business.signing.dto.SigningScenarioView;
import com.ruoyi.business.signing.spi.SigningBusinessSupport;
import com.ruoyi.common.exception.ServiceException;
import java.util.*;
import org.springframework.stereotype.Component;

/** 签署权限与来源单据读取权限叠加；配置管理权限不授予业务数据。 */
@Component
public class SigningAccess {
  private final Map<String, SigningBusinessSupport> supports = new LinkedHashMap<>();

  /** 注入当前业务读取服务。 */
  public SigningAccess(List<SigningBusinessSupport> entries) {
    for (SigningBusinessSupport entry : entries) {
      if (supports.put(entry.businessType(), entry) != null) {
        throw new IllegalStateException("重复签署访问适配器：" + entry.businessType());
      }
    }
  }

  /** 校验已登记场景及单据访问，包含签署历史和文件下载。 */
  public void business(String type, String id) {
    support(type).authorize(id);
  }

  /** 返回已登记业务，未知类型拒绝访问。 */
  public SigningBusinessSupport support(String type) {
    SigningBusinessSupport support = supports.get(type);
    if (support == null) {
      throw new ServiceException("未登记的签署业务");
    }
    return support;
  }

  /** 返回业务场景及访问能力，不返回任何业务数据。 */
  public List<SigningScenarioView> scenarios() {
    List<SigningScenarioView> result = new ArrayList<>();
    for (SigningBusinessSupport support : supports.values()) {
      SigningScenarioView view = new SigningScenarioView();
      view.setBusinessType(support.businessType());
      view.setName(support.businessName());
      view.setAccessible(support.canAccess());
      result.add(view);
    }
    return result;
  }

  /** 列表只允许查询具备来源权限的场景。 */
  public List<String> accessibleTypes() {
    List<String> result = new ArrayList<>();
    for (SigningBusinessSupport support : supports.values()) {
      if (support.canAccess()) {
        result.add(support.businessType());
      }
    }
    return result;
  }
}
