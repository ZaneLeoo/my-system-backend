package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 私有签署文件元信息。 */
@Getter
@Setter
public class SigningDocument {
  /** 平台确认的 PDF 页数。 */
  private int pageCount;

  /** 本地文档 ID。 */
  private String id;

  /** 所属任务。 */
  private String taskId;

  /** SOURCE、PREPARED 或 SIGNED。 */
  private String kind;

  /** 原始显示文件名。 */
  private String fileName;

  /** 私有存储标识，禁止暴露为公开路径。 */
  private String storageKey;

  /** 文件内容摘要。 */
  private String sha256;

  /** 字节数。 */
  private long size;

  /** 平台文件 ID。 */
  private String providerFileId;
}
