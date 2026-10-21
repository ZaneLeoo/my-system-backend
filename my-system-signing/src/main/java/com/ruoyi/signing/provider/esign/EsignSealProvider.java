package com.ruoyi.signing.provider.esign;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.SigningSeal;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import com.ruoyi.signing.spi.SigningSealProvider;
import java.util.*;

/** e签宝 V3 企业内部印章适配，不接入跨企业授权章和法人章。 */
public final class EsignSealProvider implements SigningSealProvider {
  private final EsignV3Client client;
  private final EsignConfiguration config;
  private volatile String resolvedOrganizationId;

  /** 复用现有签名、超时及代理配置。 */
  public EsignSealProvider(EsignV3Client client, EsignConfiguration config) {
    this.client = client;
    this.config = config;
  }

  /** 获取由服务端固定的我方机构，禁止客户端传入归属。 */
  private String organization() {
    if (config.getOrganizationId() != null && !config.getOrganizationId().trim().isEmpty()) {
      return required(config.getOrganizationId(), 100, "我方 e签宝机构 ID");
    }
    if (resolvedOrganizationId == null) {
      synchronized (this) {
        if (resolvedOrganizationId == null) {
          String name = required(config.getOrganizationName(), 200, "我方机构名称");
          Map<String, Object> identity =
              client.call(
                  "GET",
                  "/v3/organizations/identity-info?orgName=" + EsignV3Client.encode(name),
                  null,
                  false);
          if (!"1".equals(text(identity, "realnameStatus"))
              || !name.equals(text(identity, "orgName"))) {
            throw new SigningException("我方机构尚未实名，或平台返回的机构名称与配置不一致");
          }
          resolvedOrganizationId = required(text(identity, "orgId"), 100, "我方 e签宝机构 ID");
        }
      }
    }
    return resolvedOrganizationId;
  }

  /** 转换为官方机构印章类型。 */
  private String providerType(SigningSealType type) {
    if (type == null) {
      return "PUBLIC,CONTRACT,FINANCE,PERSONNEL,COMMON";
    }
    return type == SigningSealType.OTHER ? "COMMON" : type.name();
  }

  /** {@inheritDoc} */
  public SigningSealPage list(int pageNum, int pageSize, SigningSealType type) {
    String orgId = organization();
    Map<String, Object> data =
        client.call(
            "GET",
            "/v3/seals/org-own-seal-list?orgId="
                + EsignV3Client.encode(orgId)
                + "&pageNum="
                + pageNum
                + "&pageSize="
                + pageSize
                + "&sealBizTypes="
                + EsignV3Client.encode(providerType(type)),
            null,
            false);
    SigningSealPage result = new SigningSealPage();
    String total = text(data, "total");
    result.setTotal(total.isEmpty() ? 0 : Long.parseLong(total));
    for (Object value : array(data.get("seals"))) {
      result.getRows().add(convert(object(value), orgId));
    }
    return result;
  }

  /** 查询接口限定当前企业的内部印章，跨企业编号由平台拒绝。 */
  private Map<String, Object> info(String sealId) {
    Map<String, Object> data =
        client.call(
            "GET",
            "/v3/seals/org-seal-info?orgId="
                + EsignV3Client.encode(organization())
                + "&sealId="
                + EsignV3Client.encode(sealId),
            null,
            false);
    if (!sealId.equals(text(data, "sealId"))) {
      throw new SigningException("平台返回的印章编号与请求不一致");
    }
    return data;
  }

  /** {@inheritDoc} */
  public SigningSeal detail(String sealId) {
    return convert(info(sealId), organization());
  }

  /** {@inheritDoc} */
  public SigningSealImage preview(String sealId) {
    String url = required(text(info(sealId), "sealImageDownloadUrl"), 4000, "印章预览地址");
    byte[] bytes =
        client.fileRequest("GET", url, new byte[0], Collections.<String, String>emptyMap());
    if (bytes.length == 0 || bytes.length > 2 * 1024 * 1024) {
      throw new SigningException("印章预览图片大小不合法");
    }
    if (bytes.length >= 8
        && bytes[0] == (byte) 0x89
        && bytes[1] == 'P'
        && bytes[2] == 'N'
        && bytes[3] == 'G'
        && bytes[4] == 13
        && bytes[5] == 10
        && bytes[6] == 26
        && bytes[7] == 10) {
      return new SigningSealImage(bytes, "image/png");
    }
    if (bytes.length >= 3
        && bytes[0] == (byte) 0xff
        && bytes[1] == (byte) 0xd8
        && bytes[2] == (byte) 0xff) {
      return new SigningSealImage(bytes, "image/jpeg");
    }
    throw new SigningException("平台未返回支持的印章预览图片");
  }

  /** 显式转换平台枚举，未知类型或状态一律不可选。 */
  private SigningSeal convert(Map<String, Object> source, String orgId) {
    SigningSeal result = new SigningSeal();
    result.setId(required(text(source, "sealId"), 100, "平台印章编号"));
    result.setName(text(source, "sealName"));
    result.setOrganizationId(orgId);
    result.setOrganizationName(config.getOrganizationName());
    String type = text(source, "sealBizType");
    try {
      result.setType("COMMON".equals(type) ? SigningSealType.OTHER : SigningSealType.valueOf(type));
    } catch (IllegalArgumentException error) {
      result.setType(SigningSealType.UNSUPPORTED);
    }
    String status = text(source, "sealStatus");
    if ("1".equals(status)) {
      result.setStatus(SigningSealStatus.ENABLED);
    } else if ("2".equals(status)) {
      result.setStatus(SigningSealStatus.PENDING);
    } else if ("3".equals(status)) {
      result.setStatus(SigningSealStatus.REJECTED);
    } else if ("4".equals(status)) {
      result.setStatus(SigningSealStatus.DISABLED);
    } else if ("6".equals(status)) {
      result.setStatus(SigningSealStatus.REVOKED);
    } else {
      result.setStatus(SigningSealStatus.UNKNOWN);
    }
    result.setStatusDescription(text(source, "statusDescription"));
    result.setDefaultSeal(Boolean.TRUE.equals(source.get("defaultSealFlag")));
    result.setWidth(integer(source, "sealWidth"));
    result.setHeight(integer(source, "sealHeight"));
    result.setSelectable(
        result.getStatus() == SigningSealStatus.ENABLED
            && result.getType() != SigningSealType.UNSUPPORTED);
    result.setPreviewAvailable(!text(source, "sealImageDownloadUrl").isEmpty());
    return result;
  }

  /** 读取可空的毫米尺寸。 */
  private Integer integer(Map<String, Object> source, String key) {
    String value = text(source, key);
    return value.isEmpty() ? null : Integer.valueOf(value);
  }
}
