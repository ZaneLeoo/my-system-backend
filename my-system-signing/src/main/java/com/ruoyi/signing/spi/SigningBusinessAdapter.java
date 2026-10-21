package com.ruoyi.signing.spi;

import com.ruoyi.signing.model.*;
import java.util.List;
import java.util.Map;

/** 业务只负责资格及快照，不直接依赖平台。 */
public interface SigningBusinessAdapter {
  /**
   * 返回登记的业务类型。
   *
   * @return 稳定业务编码
   */
  String businessType();

  /**
   * 校验业务可发起并读取允许签署的内容；不能调用平台。
   *
   * @param businessId 来源主键
   * @return 业务内容快照
   */
  BusinessSnapshot snapshot(String businessId);

  /**
   * 返回实施人员可选择的业务字段。
   *
   * @return 中文字段目录
   */
  List<SigningField> fields();

  /**
   * 从任务保存的业务快照生成对方签署默认值，已有签署人信息不得被默认值覆盖。
   *
   * @param values 创建任务时保存的业务快照
   * @return 待用户确认的默认签署方；没有可用默认值时返回 null
   */
  default SigningParty defaultCounterparty(Map<String, Object> values) {
    return null;
  }
}
