package com.ruoyi.signing.api;

import com.ruoyi.signing.model.*;
import java.util.List;
import java.util.Map;

/** 本地字段映射及官方模板能力，操作前由宿主鉴权。 */
public interface SigningTemplateService {
  /**
   * 读取允许映射的中文字段目录。
   *
   * @param type 登记业务场景
   * @return 字段列表
   */
  List<SigningField> fields(String type);

  /**
   * 保存经过平台控件及业务字段校验的绑定。
   *
   * @param binding 本地绑定
   * @param actor 当前维护人，由宿主身份提供
   * @return 保存后的配置
   */
  SigningTemplateBinding save(SigningTemplateBinding binding, String actor);

  /**
   * 生成受控填充值，不能执行自定义表达式或 SQL。
   *
   * @param binding 模板配置
   * @param snapshot 业务快照
   * @return 平台控件填充值
   */
  List<Map<String, Object>> values(SigningTemplateBinding binding, Map<String, Object> snapshot);
}
