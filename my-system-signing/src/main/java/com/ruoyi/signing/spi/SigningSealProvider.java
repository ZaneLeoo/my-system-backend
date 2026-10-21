package com.ruoyi.signing.spi;

import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.SigningSeal;
import com.ruoyi.signing.model.enums.SigningSealType;

/** 当前我方企业的印章只读能力，企业归属由宿主配置而非客户端指定。 */
public interface SigningSealProvider {
  /**
   * 分页读取当前企业的内部机构印章。
   *
   * @param pageNum 页码
   * @param pageSize 每页数量，上限二十
   * @param type 机构印章类型，空表示全部支持类型
   * @return 平台无关分页结果
   */
  SigningSealPage list(int pageNum, int pageSize, SigningSealType type);

  /**
   * 读取当前企业的指定印章，不得返回其他企业或外部授权印章。
   *
   * @param sealId 印章编号
   * @return 最新印章信息
   */
  SigningSeal detail(String sealId);

  /**
   * 读取当前企业印章的带水印预览图片。
   *
   * @param sealId 印章编号
   * @return 已验证类型和大小的预览图片
   */
  SigningSealImage preview(String sealId);
}
