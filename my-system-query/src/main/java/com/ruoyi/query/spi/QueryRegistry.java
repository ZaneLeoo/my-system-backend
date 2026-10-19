package com.ruoyi.query.spi;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

/** 登记唯一业务能力，并对运行时和预览统一检查业务权限。 */
@Component
public class QueryRegistry {
  private final Map<String, QueryAdapter> adapters = new LinkedHashMap<>();

  /** 拒绝重复登记，避免查询对象被意外替换。 */
  public QueryRegistry(List<QueryAdapter> values) {
    for (QueryAdapter a : values) {
      if (adapters.putIfAbsent(a.key(), a) != null) {
        throw new IllegalStateException("重复查询对象");
      }
    }
  }

  /** 返回登记的适配器。 */
  public QueryAdapter require(String key) {
    QueryAdapter a = adapters.get(key);
    if (a == null) {
      throw new ServiceException("查询对象未接入");
    }
    return a;
  }

  /** 返回查询适配器已接入的来源表，不作为数据目录导入白名单。 */
  public Set<String> sourceTables() {
    Set<String> names = new TreeSet<>();
    adapters.values().forEach(a -> a.sources().values().forEach(s -> names.add(s.getTable())));
    return names;
  }

  /** 运行时始终按当前登录身份核验业务权限。 */
  public QueryAdapter authorized(String key) {
    QueryAdapter a = require(key);
    Set<String> permissions = SecurityUtils.getLoginUser().getPermissions();
    if (permissions == null
        || permissions.stream()
            .noneMatch(
                p -> p.equals("*:*:*") || PatternMatchUtils.simpleMatch(p, a.permission()))) {
      throw new AccessDeniedException("没有业务列表权限");
    }
    return a;
  }
}
