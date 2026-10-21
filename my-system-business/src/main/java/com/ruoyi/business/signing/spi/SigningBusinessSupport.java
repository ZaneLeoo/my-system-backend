package com.ruoyi.business.signing.spi;

import com.ruoyi.business.signing.dto.SigningSourceQuery;
import com.ruoyi.business.signing.dto.SigningSourceView;
import com.ruoyi.common.core.page.PageResult;

/** 宿主的业务访问及来源选择契约，不进入可迁移签署核心。 */
public interface SigningBusinessSupport {
  /**
   * 返回与核心适配器一致的编码。
   *
   * @return 稳定业务编码
   */
  String businessType();

  /**
   * 返回场景名称。
   *
   * @return 中文业务名称
   */
  String businessName();

  /**
   * 判断用户是否可查询该场景的全部签署任务；有行级隔离的业务应另行约束列表范围。
   *
   * @return 是否具备业务查询权限
   */
  boolean canAccess();

  /**
   * 校验单据存在与读取权限，历史单据不要求仍可发起签署。
   *
   * @param businessId 来源主键
   * @throws com.ruoyi.common.exception.ServiceException 无权限或主键无效时
   */
  void authorize(String businessId);

  /**
   * 查询用户有权读取且满足发起资格的来源。
   *
   * @param query 已校验的关键词与分页参数
   * @return 可发起来源及总数
   * @throws com.ruoyi.common.exception.ServiceException 缺少来源查询权限时
   */
  PageResult<SigningSourceView> sources(SigningSourceQuery query);
}
