package com.ruoyi.signing.spi;

import com.ruoyi.signing.model.*;
import java.util.*;

/** 第三方能力边界，仅适配器内部使用平台字段。 */
public interface SigningProvider {
  /**
   * 返回应用环境稳定标识。
   *
   * @return 环境和应用 ID 组合
   */
  String environment();

  /**
   * 上传并触发转换，不等待文件就绪。
   *
   * @param name 文件名
   * @param bytes 原文件
   * @return 平台文件 ID
   */
  String upload(String name, byte[] bytes);

  /**
   * 查询文件状态及 PDF 结果。
   *
   * @param fileId 平台 ID
   * @return 平台文件信息
   */
  Map<String, Object> file(String fileId);

  /**
   * 模板填充生成最终文件。
   *
   * @param templateId 模板
   * @param components 经受控映射的控件
   * @return 平台文件 ID
   */
  String fill(String templateId, List<Map<String, Object>> components);

  /**
   * 创建但不启动流程；超时必须标记结果不确定。
   *
   * @param task 任务
   * @param fileId PDF 文件
   * @param parties 已校验签署方
   * @return 平台流程 ID
   */
  String createFlow(SigningTask task, String fileId, List<SigningParty> parties);

  /**
   * 启动已保存 ID 的流程。
   *
   * @param flowId 平台流程
   */
  void start(String flowId);

  /**
   * 读取权威流程状态及签署方。
   *
   * @param flowId 平台流程
   * @return 平台详情
   */
  com.ruoyi.signing.api.result.SigningFlowResult detail(String flowId);

  /**
   * 撤销尚未结束流程。
   *
   * @param flowId 流程
   * @param reason 原因
   */
  void revoke(String flowId, String reason);

  /**
   * 下载最终已签文件，不能使用带水印的签署中预览。
   *
   * @param flowId 流程
   * @param fileId 原文件
   * @return 完整已签 PDF
   */
  byte[] signedFile(String flowId, String fileId);

  /**
   * 受信平台文件下载。
   *
   * @param url 平台临时 URL
   * @return 文件字节
   */
  byte[] download(String url);

  /**
   * 分页获取平台模板。
   *
   * @param page 页码
   * @return 模板列表响应
   */
  Map<String, Object> templates(int page);

  /**
   * 获取模板控件详情。
   *
   * @param templateId 模板
   * @return 控件详情
   */
  Map<String, Object> template(String templateId);

  /**
   * 获取官方模板编辑入口。
   *
   * @param templateId 模板
   * @return 编辑 URL
   */
  String templateEditUrl(String templateId);

  /**
   * 获取官方定位入口。
   *
   * @param fileId 待签文件
   * @param session 唯一定位会话
   * @return 官方 URL
   */
  String positionUrl(String fileId, String session);
}
