package com.ruoyi.signing;

import static com.ruoyi.signing.application.SigningSupport.*;
import static org.junit.Assert.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.provider.esign.*;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import java.util.*;
import org.junit.Test;

/** Java 8 下验证官方响应转换、归属范围和图片内容。 */
public class SigningSealTest {
  /** 可记录接口调用的假客户端，不发外部请求。 */
  private static final class Client extends EsignV3Client {
    private String path;
    private Map<String, Object> response;
    private Map<String, Object> identity;
    private int identityCalls;
    private byte[] image = new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

    /** 注入不包含密钥的测试配置。 */
    private Client(EsignConfiguration config) {
      super(config, null);
    }

    /** 返回固定官方结构。 */
    @Override
    public Map<String, Object> call(String method, String path, Object body, boolean uncertain) {
      this.path = path;
      if (path.startsWith("/v3/organizations/identity-info")) {
        identityCalls++;
        return identity;
      }
      return response;
    }

    /** 返回测试图片，不保存临时 URL。 */
    @Override
    public byte[] fileRequest(String method, String url, byte[] body, Map<String, String> headers) {
      assertTrue(headers.isEmpty());
      return image;
    }
  }

  /** 构造统一的官方印章结构。 */
  private Map<String, Object> seal(String id, String type, int status) {
    return map(
        "sealId",
        id,
        "sealName",
        "测试合同章",
        "sealBizType",
        type,
        "sealStatus",
        status,
        "defaultSealFlag",
        true,
        "sealWidth",
        40.0,
        "sealHeight",
        40,
        "sealImageDownloadUrl",
        "https://esignoss.esign.cn/test.png");
  }

  /** 所有请求均固定我方机构，类型和状态显式映射且保留平台总数。 */
  @Test
  public void listIsScopedAndUnknownStatesCannotBeSelected() {
    EsignConfiguration config = new EsignConfiguration();
    config.setOrganizationId("our-org");
    Client client = new Client(config);
    client.response =
        map(
            "total",
            18.0,
            "seals",
            Arrays.asList(
                seal("one", "CONTRACT", 1), seal("two", "PUBLIC", 4),
                seal("three", "LEGAL_PERSON", 1), seal("four", "COMMON", 99)));
    SigningSealService service = new SigningSealService(new EsignSealProvider(client, config));
    SigningSealPage page = service.list(2, 8, "");
    assertEquals(18, page.getTotal());
    assertTrue(client.path.contains("orgId=our-org"));
    assertTrue(client.path.contains("pageNum=2&pageSize=8"));
    assertFalse(client.path.contains("LEGAL_PERSON"));
    assertEquals("our-org", page.getRows().get(0).getOrganizationId());
    assertTrue(page.getRows().get(0).isSelectable());
    assertEquals(Integer.valueOf(40), page.getRows().get(0).getWidth());
    for (int index = 1; index < 4; index++) {
      assertFalse(page.getRows().get(index).isSelectable());
    }
  }

  /** 平台必须返回所请求的印章；停用或法人印章不可发起。 */
  @Test
  public void configuredOrganizationNameResolvesOnlyMatchingVerifiedIdentity() {
    EsignConfiguration config = new EsignConfiguration();
    config.setOrganizationName("我方测试企业");
    Client client = new Client(config);
    client.identity = map("orgId", "resolved-org", "orgName", "我方测试企业", "realnameStatus", 1);
    client.response = map("total", 0, "seals", Collections.emptyList());
    SigningSealService service = new SigningSealService(new EsignSealProvider(client, config));
    service.list(1, 8, null);
    service.list(2, 8, null);
    assertEquals(1, client.identityCalls);
    assertTrue(client.path.contains("orgId=resolved-org"));
    client.identity = map("orgId", "other-org", "orgName", "其他企业", "realnameStatus", 1);
    SigningSealService mismatch = new SigningSealService(new EsignSealProvider(client, config));
    assertThrows(SigningException.class, () -> mismatch.list(1, 8, null));
  }

  /** 平台必须返回所请求的印章；停用或法人印章不可发起。 */
  @Test
  public void selectionRevalidatesIdTypeAndState() {
    EsignConfiguration config = new EsignConfiguration();
    config.setOrganizationId("our-org");
    Client client = new Client(config);
    SigningSealService service = new SigningSealService(new EsignSealProvider(client, config));
    client.response = seal("seal", "PUBLIC", 1);
    assertEquals("seal", service.requireSelectable("seal").getId());
    client.response = seal("other", "PUBLIC", 1);
    assertThrows(SigningException.class, () -> service.requireSelectable("seal"));
    client.response = seal("seal", "PUBLIC", 2);
    assertThrows(SigningException.class, () -> service.requireSelectable("seal"));
    client.response = seal("seal", "LEGAL_PERSON", 1);
    assertThrows(SigningException.class, () -> service.requireSelectable("seal"));
    assertThrows(SigningException.class, () -> service.list(0, 8, null));
    assertThrows(SigningException.class, () -> service.list(1, 21, null));
    assertThrows(SigningException.class, () -> service.list(1, 8, "UNSUPPORTED"));
  }

  /** 预览拒绝 HTML、超大内容；官方业务错误不重写原始说明。 */
  @Test
  public void previewChecksContentAndPreservesPlatformErrors() {
    EsignConfiguration config = new EsignConfiguration();
    config.setOrganizationId("our-org");
    Client client = new Client(config);
    client.response = seal("seal", "PUBLIC", 1);
    SigningSealService service = new SigningSealService(new EsignSealProvider(client, config));
    assertEquals("image/png", service.preview("seal").getContentType());
    client.image = "<html>access denied</html>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    assertThrows(SigningException.class, () -> service.preview("seal"));
    client.image = new byte[2 * 1024 * 1024 + 1];
    assertThrows(SigningException.class, () -> service.preview("seal"));
    SigningSealService denied =
        new SigningSealService(
            new com.ruoyi.signing.spi.SigningSealProvider() {
              public SigningSealPage list(int page, int size, SigningSealType type) {
                throw new SigningException("e签宝平台返回错误（1499000）： 用户未授权 manage_org_seal");
              }

              public SigningSeal detail(String id) {
                throw new SigningException("e签宝平台返回错误（31400109）： 印章不属于该账号");
              }

              public SigningSealImage preview(String id) {
                throw new SigningException("拒绝访问");
              }
            });
    assertTrue(
        assertThrows(SigningException.class, () -> denied.list(1, 8, null))
            .getMessage()
            .contains("1499000"));
    assertTrue(
        assertThrows(SigningException.class, () -> denied.requireSelectable("seal"))
            .getMessage()
            .contains("31400109"));
  }
}
