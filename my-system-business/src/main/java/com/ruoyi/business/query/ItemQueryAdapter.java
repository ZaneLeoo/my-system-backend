package com.ruoyi.business.query;

import com.ruoyi.business.masterdata.item.service.support.ItemReadSupport;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.query.engine.CompiledQuery;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** 料品列表适配器，与已有参照保持独立协议。 */
@Component
public class ItemQueryAdapter implements QueryAdapter {
  private final ItemReadSupport read;
  private final QueryOptionsSupport options;

  /** 注入现有 VO 查询与专用选项。 */
  public ItemQueryAdapter(ItemReadSupport read, QueryOptionsSupport options) {
    this.read = read;
    this.options = options;
  }

  /** 业务编码。 */
  public String key() {
    return "masterdata.item";
  }

  /** 原列表权限。 */
  public String permission() {
    return "masterdata:item:list";
  }

  /** 仅开放料品主表。 */
  public Map<String, QuerySource> sources() {
    return Map.of("main", new QuerySource("md_item", "i", "料品档案", null));
  }

  /** 稳定主键。 */
  public String primaryKey() {
    return "item_id";
  }

  /** 默认编码排序。 */
  public String defaultSort() {
    return "itemCode";
  }

  /** 默认升序。 */
  public String defaultDirection() {
    return "asc";
  }

  /** 原有名称关联和分类路径组装。 */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public PageResult<?> search(CompiledQuery query, SearchRequest request) {
    return read.search(query, request);
  }

  /** 只开放业务支持的选项提供器。 */
  public Set<String> optionProviders() {
    return Set.of("boolean", "categories", "units", "enableStatus", "purchaseEnabled");
  }

  /** 校验状态和档案 ID。 */
  public void validateValue(FieldSpec f, Object value) {
    options.validate(f, value);
  }

  /** 分类后代匹配。 */
  public List<?> descendants(Object id) {
    return options.descendants(id);
  }

  /** 查询选项不额外要求参照权限。 */
  public Object options(String provider, String keyword, List<Long> ids, int page) {
    return options.options(provider, keyword, ids, page);
  }

  /** 根据业务字段提供固定的引用或枚举语义。 */
  public String fieldOptionProvider(String sourceKey, String columnName) {
    return options.provider(key(), sourceKey, columnName);
  }
}
