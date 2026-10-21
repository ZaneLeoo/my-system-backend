package com.ruoyi.business.signing.infrastructure;

import com.ruoyi.business.signing.config.SigningProperties;
import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.application.SigningSupport;
import com.ruoyi.signing.model.SigningDocument;
import com.ruoyi.signing.spi.SigningFileStore;
import java.io.IOException;
import java.nio.file.*;
import org.springframework.stereotype.Component;

/** 私有目录不登记为静态资源，下载只允许使用仓库查出的存储键。 */
@Component
public class LocalSigningFileStore implements SigningFileStore {
  private final Path root;

  /** 解析固定私有根目录，首次使用才创建。 */
  public LocalSigningFileStore(SigningProperties config) {
    root = Paths.get(config.getPrivateDirectory()).toAbsolutePath().normalize();
  }

  /** {@inheritDoc} */
  public SigningDocument put(String name, byte[] bytes) {
    if (bytes == null || bytes.length == 0 || bytes.length > 50 * 1024 * 1024) {
      throw new SigningException("合同文件为空或超过 50MB");
    }
    try {
      Files.createDirectories(root);
      String key = SigningSupport.id();
      Path destination = root.resolve(key);
      Files.write(destination, bytes, StandardOpenOption.CREATE_NEW);
      SigningDocument document = new SigningDocument();
      document.setFileName(name);
      document.setStorageKey(key);
      document.setSize(bytes.length);
      document.setSha256(SigningSupport.hash(bytes));
      return document;
    } catch (IOException e) {
      throw new SigningException("合同私有存储写入失败");
    }
  }

  /** {@inheritDoc} */
  public byte[] read(String key) {
    if (key == null || !key.matches("[a-f0-9-]{36}")) {
      throw new SigningException("合同存储标识不合法");
    }
    Path file = root.resolve(key).normalize();
    if (!file.startsWith(root) || Files.isSymbolicLink(file)) {
      throw new SigningException("合同路径不合法");
    }
    try {
      if (Files.size(file) > 50 * 1024 * 1024) {
        throw new SigningException("合同大小超过限制");
      }
      return Files.readAllBytes(file);
    } catch (IOException e) {
      throw new SigningException("合同文件不存在或无法读取");
    }
  }
}
