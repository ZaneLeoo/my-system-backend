package com.ruoyi.query.metadata;

import com.ruoyi.query.spi.MetadataTableProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** 独立的数据目录白名单，表结构收录不依赖是否已实现查询适配器。 */
@Component
public class MetadataRegistry {
  private static final Pattern TABLE_NAME = Pattern.compile("[a-z][a-z0-9_]{0,63}");
  private final Map<String, String> names;

  /** 合并业务模块的显式登记，拒绝非法表名及冲突的中文名称。 */
  public MetadataRegistry(List<MetadataTableProvider> providers) {
    Map<String, String> registered = new TreeMap<>();
    for (MetadataTableProvider provider : providers) {
      for (Entry<String, String> entry : provider.tables().entrySet()) {
        String table = entry.getKey();
        String label = entry.getValue();
        if (table == null
            || !TABLE_NAME.matcher(table).matches()
            || label == null
            || label.isBlank()
            || label.strip().length() > 100) {
          throw new IllegalStateException("数据目录登记的表名或中文名称无效");
        }
        String previous = registered.putIfAbsent(table, label.strip());
        if (previous != null && !previous.equals(label.strip())) {
          throw new IllegalStateException("数据目录重复登记的名称不一致：" + table);
        }
      }
    }
    names = Collections.unmodifiableMap(registered);
  }

  /** 返回允许读取结构的精确表名集合，不按前缀自动开放表。 */
  public Set<String> tables() {
    return names.keySet();
  }

  /** 返回首次导入时的中文名称；后续同步保留目录人工名称。 */
  public String displayName(String table) {
    return names.get(table);
  }
}
