package com.ruoyi.signing.spi;

import com.ruoyi.signing.model.SigningDocument;

/** 私有文件存储，不依赖宿主公开上传目录。 */
public interface SigningFileStore {
  /**
   * 私有保存文件。
   *
   * @param name 显示名称
   * @param bytes 文件内容
   * @return 包含存储键和摘要的元信息
   */
  SigningDocument put(String name, byte[] bytes);

  /**
   * 读取私有文件，调用前必须校验业务权限。
   *
   * @param key 受信存储标识
   * @return 文件字节
   */
  byte[] read(String key);
}
