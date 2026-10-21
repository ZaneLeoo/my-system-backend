package com.ruoyi.signing.application;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.spi.*;
import java.util.*;

/** 官方控件与开发者字段目录之间的显式映射。 */
public class DefaultSigningTemplateService implements SigningTemplateService {
  private final SigningRepository repository;
  private final SigningProvider provider;
  private final SigningJsonCodec json;
  private final Map<String, SigningBusinessAdapter> adapters;

  /** 注入平台和受控业务来源。 */
  public DefaultSigningTemplateService(
      SigningRepository repository,
      SigningProvider provider,
      SigningJsonCodec json,
      Map<String, SigningBusinessAdapter> adapters) {
    this.repository = repository;
    this.provider = provider;
    this.json = json;
    this.adapters = adapters;
  }

  /** {@inheritDoc} */
  public List<SigningField> fields(String type) {
    SigningBusinessAdapter adapter = adapters.get(type);
    if (adapter == null) {
      throw new SigningException("未登记签署业务场景");
    }
    return adapter.fields();
  }

  /** 归一化官方模板嵌套控件。 */
  public static List<Map<String, Object>> controls(Object value) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    collect(value, result);
    return result;
  }

  /** 递归寻找控件，保留平台类型用于后续比较。 */
  private static void collect(Object value, List<Map<String, Object>> result) {
    if (value instanceof Map) {
      Map<String, Object> item = object(value);
      if (item.containsKey("componentId")
          && (item.containsKey("componentType") || item.containsKey("type"))) {
        result.add(item);
      } else {
        for (Object child : item.values()) {
          collect(child, result);
        }
      }
    } else if (value instanceof List) {
      for (Object child : array(value)) {
        collect(child, result);
      }
    }
  }

  /** {@inheritDoc} */
  public SigningTemplateBinding save(SigningTemplateBinding binding, String actor) {
    binding.setBusinessType(required(binding.getBusinessType(), 64, "业务场景"));
    binding.setName(required(binding.getName(), 100, "模板名称"));
    binding.setTemplateId(required(binding.getTemplateId(), 100, "平台模板"));
    if (binding.getMappings() == null || binding.getMappings().size() > 200) {
      throw new SigningException("模板映射最多 200 项");
    }
    validate(binding);
    long expected = binding.getId() == null ? -1 : binding.getVersion();
    binding.setUpdateBy(required(actor, 64, "维护人"));
    binding.setUpdateTime(now());
    if (expected < 0) {
      binding.setCreateBy(actor);
      binding.setCreateTime(binding.getUpdateTime());
      binding.setId(id());
      binding.setVersion(0);
    } else {
      SigningTemplateBinding previous = repository.binding(binding.getId());
      if (previous == null) {
        throw new SigningException("模板绑定不存在");
      }
      binding.setCreateBy(previous.getCreateBy());
      binding.setCreateTime(previous.getCreateTime());
      binding.setVersion(Math.addExact(expected, 1));
    }
    binding.setProviderEnvironment(provider.environment());
    repository.saveBinding(binding, expected);
    return binding;
  }

  /** 校验控件存在、类型未变化且字段来自业务目录。 */
  public void validate(SigningTemplateBinding binding) {
    if (binding.getProviderEnvironment() != null
        && !provider.environment().equals(binding.getProviderEnvironment())) {
      throw new SigningException("模板所属平台环境不匹配");
    }
    Map<String, SigningField> allowed = new LinkedHashMap<String, SigningField>();
    for (SigningField field : fields(binding.getBusinessType())) {
      allowed.put(field.getKey(), field);
    }
    Map<String, Map<String, Object>> controls = new HashMap<String, Map<String, Object>>();
    for (Map<String, Object> control : controls(provider.template(binding.getTemplateId()))) {
      controls.put(text(control, "componentId"), control);
    }
    Set<String> seen = new HashSet<String>();
    for (TemplateMapping mapping : binding.getMappings()) {
      if (mapping == null) {
        throw new SigningException("映射项不能为空");
      }
      Map<String, Object> control = controls.get(mapping.getComponentId());
      SigningField field = allowed.get(mapping.getFieldKey());
      if (control == null || field == null || !seen.add(mapping.getComponentId())) {
        throw new SigningException("控件已失效、重复或业务字段未登记，请重新映射");
      }
      String type = text(control, "componentType");
      if (type.isEmpty()) {
        type = text(control, "type");
      }
      if (!type.equals(mapping.getComponentType())) {
        throw new SigningException("模板控件类型已变化，请重新映射");
      }
      if (!Arrays.asList("1", "2", "3", "8", "18", "19").contains(type)) {
        throw new SigningException("一期仅支持文本、数字、日期、手机号及动态表控件");
      }
      if (("2".equals(type) && !"NUMBER".equals(field.getType()))
          || ("3".equals(type) && !"DATE".equals(field.getType()))) {
        throw new SigningException("控件类型与业务字段类型不匹配");
      }
      if ("TABLE".equals(field.getType())) {
        if (!"18".equals(type) || mapping.getColumns() == null || mapping.getColumns().isEmpty()) {
          throw new SigningException("明细必须映射到动态表控件并配置列");
        }
        Set<String> columns = new HashSet<String>();
        for (SigningField column : field.getColumns()) {
          columns.add(column.getKey());
        }
        Set<String> targetColumns = new HashSet<String>();
        for (Object row :
            array(object(control.get("componentSpecialAttribute")).get("tableContent"))) {
          targetColumns.addAll(object(object(row).get("row")).keySet());
        }
        if (!targetColumns.containsAll(mapping.getColumns().keySet())) {
          throw new SigningException("动态表目标列已变化，请重新映射");
        }
        for (String column : mapping.getColumns().values()) {
          if (!columns.contains(column)) {
            throw new SigningException("明细列未登记");
          }
        }
      } else if ("18".equals(type)) {
        throw new SigningException("标量字段不能填充动态表");
      }
    }
  }

  /** {@inheritDoc} */
  public List<Map<String, Object>> values(
      SigningTemplateBinding binding, Map<String, Object> snapshot) {
    validate(binding);
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    for (TemplateMapping mapping : binding.getMappings()) {
      Object value = snapshot.get(mapping.getFieldKey());
      if (value instanceof List) {
        List<Object> rows = new ArrayList<Object>();
        for (Object row : array(value)) {
          Map<String, Object> cells = new LinkedHashMap<String, Object>();
          for (Map.Entry<String, String> column : mapping.getColumns().entrySet()) {
            cells.put(column.getKey(), object(row).get(column.getValue()));
          }
          rows.add(map("row", cells, "insertRow", "true"));
        }
        value = json.write(rows);
      }
      result.add(map("componentId", mapping.getComponentId(), "componentValue", numberText(value)));
    }
    return result;
  }
}
