package com.ruoyi.signing;

import static org.junit.Assert.*;

import com.ruoyi.signing.api.*;
import com.ruoyi.signing.application.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.provider.esign.*;
import com.ruoyi.signing.provider.esign.config.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;

/** 此套测试同时在当前 JDK 和真实 Java 8 运行。 */
public class SigningCoreTest {
  private static final byte[] BODY =
      "{\"name\":\"合同\",\"zero\":0,\"enabled\":false}".getBytes(StandardCharsets.UTF_8);

  /** SOCKS 请求必须传递域名，代理拒绝时仍保留创建结果不确定标记。 */
  @Test(timeout = 10000)
  public void socksProxyResolvesRemoteHostname() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
      server.setSoTimeout(3000);
      Future<String> destination = executor.submit(() -> socksDestination(server));
      EsignConfiguration config = config();
      config.setSocksProxyHost("127.0.0.1");
      config.setSocksProxyPort(server.getLocalPort());
      config.setConnectTimeout(3000);
      config.setReadTimeout(3000);
      final com.google.gson.Gson gson = new com.google.gson.Gson();
      com.ruoyi.signing.spi.SigningJsonCodec codec =
          new com.ruoyi.signing.spi.SigningJsonCodec() {
            public String write(Object value) {
              return gson.toJson(value);
            }

            public <T> T read(String value, Class<T> type) {
              return gson.fromJson(value, type);
            }
          };
      try {
        new EsignV3Client(config, codec)
            .call("POST", "/v3/sign-flow/create-by-file", Collections.emptyMap(), true);
        fail("代理已拒绝连接");
      } catch (SigningException expected) {
        assertTrue(expected.isUncertain());
      }
      assertEquals("smlopenapi.esign.cn:443", destination.get(4, TimeUnit.SECONDS));
    } finally {
      executor.shutdownNow();
    }
  }

  /** 接收本地 SOCKS5 测试请求，仅返回拒绝连接，不访问外网。 */
  private String socksDestination(ServerSocket server) throws IOException {
    try (Socket socket = server.accept()) {
      socket.setSoTimeout(3000);
      DataInputStream input = new DataInputStream(socket.getInputStream());
      OutputStream output = socket.getOutputStream();
      assertEquals(5, input.readUnsignedByte());
      byte[] methods = new byte[input.readUnsignedByte()];
      input.readFully(methods);
      output.write(new byte[] {5, 0});
      output.flush();
      assertEquals(5, input.readUnsignedByte());
      assertEquals(1, input.readUnsignedByte());
      assertEquals(0, input.readUnsignedByte());
      assertEquals(3, input.readUnsignedByte());
      byte[] hostname = new byte[input.readUnsignedByte()];
      input.readFully(hostname);
      String result =
          new String(hostname, StandardCharsets.US_ASCII) + ":" + input.readUnsignedShort();
      output.write(new byte[] {5, 5, 0, 1, 0, 0, 0, 0, 0, 0});
      output.flush();
      return result;
    }
  }

  /** 出站签名使用独立 Python 算出的固定向量，包含中文与零值。 */
  @Test
  public void outboundGoldenVector() {
    Map<String, String> headers =
        new EsignRequestSigner()
            .headers(
                "POST",
                "/v3/files/file-upload-url",
                BODY,
                "test-app",
                "test-secret",
                1729489875363L);
    assertEquals(
        "ototSunC1cS1E9GwGoi80fIDcsbxAupB/AaGka4rpeg=", headers.get("X-Tsign-Open-Ca-Signature"));
    assertEquals("Q+fNAKw3TZ5fi2dS31uCwQ==", headers.get("Content-MD5"));
  }

  /** 空 GET 请求不能使用空字节 MD5。 */
  @Test
  public void querySigningUsesDecodedSortedValues() {
    EsignRequestSigner signer = new EsignRequestSigner();
    String path =
        "/v3/seals/org-own-seal-list?pageSize=8&sealBizTypes=PUBLIC%2CCONTRACT"
            + "&orgId=org&pageNum=1";
    assertEquals(
        "ULhe5+Ux8+fmWFoMP+y4oo/Qx9RywBwkFAE3aMe8Xzw=",
        signer
            .headers("GET", path, new byte[0], "app", "test-secret", 1)
            .get("X-Tsign-Open-Ca-Signature"));
    assertEquals(
        signer.headers("GET", "/x?a&z=one", new byte[0], "app", "secret", 1),
        signer.headers("GET", "/x?z=one&a=&z=two", new byte[0], "app", "secret", 1));
    assertEquals(
        signer.headers("GET", "/x?name=合同", new byte[0], "app", "secret", 1),
        signer.headers("GET", "/x?name=%E5%90%88%E5%90%8C", new byte[0], "app", "secret", 1));
  }

  /** HTTP 拒绝响应仍显示平台的 code 和原始 message。 */
  @Test
  public void httpRejectionPreservesPlatformMessage() {
    final com.google.gson.Gson gson = new com.google.gson.Gson();
    com.ruoyi.signing.spi.SigningJsonCodec codec =
        new com.ruoyi.signing.spi.SigningJsonCodec() {
          public String write(Object value) {
            return gson.toJson(value);
          }

          public <T> T read(String value, Class<T> type) {
            return gson.fromJson(value, type);
          }
        };
    EsignV3Client client =
        new EsignV3Client(config(), codec) {
          @Override
          protected byte[] request(
              String method,
              String url,
              byte[] body,
              Map<String, String> headers,
              int limit,
              boolean uncertain) {
            throw platformHttpFailure(
                401,
                "{\"code\":401,\"message\":\"INVALID_SIGNATURE\"}".getBytes(StandardCharsets.UTF_8),
                uncertain);
          }
        };
    SigningException error =
        assertThrows(SigningException.class, () -> client.call("GET", "/v3/test", null, false));
    assertEquals("e签宝平台返回错误（401）： INVALID_SIGNATURE", error.getMessage());
    assertFalse(error.isUncertain());
  }

  /** 空 GET 请求不能使用空字节 MD5。 */
  @Test
  public void emptyBodyHasEmptyMd5() {
    assertEquals(
        "",
        new EsignRequestSigner()
            .headers("GET", "/v3/files/id", new byte[0], "app", "secret", 1)
            .get("Content-MD5"));
  }

  /** 验签排序查询参数，合法旧事件也能重试。 */
  @Test
  public void callbackGoldenVector() {
    EsignConfiguration config = config();
    Map<String, String> query = new LinkedHashMap<String, String>();
    query.put("z", "z-value");
    query.put("a", "a-value");
    assertTrue(
        new EsignCallbackVerifier(config)
            .verify(
                "test-app",
                "hmac-sha256",
                "1729489875363",
                "71146d231f1d142d362f6ab532bdff5a47e22cbf4816496c6e10df9e128efaae",
                query,
                BODY));
    assertFalse(
        new EsignCallbackVerifier(config)
            .verify(
                "wrong-app",
                "hmac-sha256",
                "1729489875363",
                "71146d231f1d142d362f6ab532bdff5a47e22cbf4816496c6e10df9e128efaae",
                query,
                BODY));
  }

  /** 原始字节被改写后签名必须失败。 */
  @Test
  public void alteredBodyRejected() {
    Map<String, String> query = new HashMap<String, String>();
    query.put("a", "a-value");
    query.put("z", "z-value");
    assertFalse(
        new EsignCallbackVerifier(config())
            .verify(
                "test-app",
                "hmac-sha256",
                "1729489875363",
                "71146d231f1d142d362f6ab532bdff5a47e22cbf4816496c6e10df9e128efaae",
                query,
                "{}".getBytes(StandardCharsets.UTF_8)));
  }

  /** 未启用不接受通知。 */
  @Test
  public void disabledCallbacksRejected() {
    EsignConfiguration config = config();
    config.setEnabled(false);
    assertFalse(
        new EsignCallbackVerifier(config)
            .verify(
                "test-app",
                "hmac-sha256",
                "1729489875363",
                "71146d231f1d142d362f6ab532bdff5a47e22cbf4816496c6e10df9e128efaae",
                Collections.<String, String>emptyMap(),
                BODY));
  }

  /** 过期版本不能修改。 */
  @Test
  public void versionConflict() {
    SigningTask task = new SigningTask();
    task.setVersion(2);
    try {
      new SigningStateMachine().version(task, 1);
      fail();
    } catch (SigningException expected) {
      assertTrue(expected.getMessage().contains("刷新"));
    }
  }

  /** 结束状态不能被迟到事件回退。 */
  @Test
  public void terminalCannotRegress() {
    SigningTask task = new SigningTask();
    task.setStatus(SigningStatus.COMPLETED);
    try {
      new SigningStateMachine().transition(task, SigningStatus.SIGNING);
      fail();
    } catch (SigningException expected) {
      assertEquals(SigningStatus.COMPLETED, task.getStatus());
    }
  }

  /** 未知创建结果不能再次编辑或创建。 */
  @Test
  public void uncertainTaskIsFrozen() {
    SigningTask task = new SigningTask();
    task.setOperationStatus(SigningOperationStatus.UNKNOWN);
    try {
      new SigningStateMachine().editable(task);
      fail();
    } catch (SigningException expected) {
      assertTrue(expected.getMessage().contains("不允许修改"));
    }
  }

  /** 平台所有公开终态可映射，未知状态不能猜测。 */
  @Test
  public void vendorStatuses() {
    EsignStatusMapper mapper = new EsignStatusMapper();
    assertEquals(SigningStatus.COMPLETED, mapper.flow(2));
    assertEquals(SigningStatus.REVOKED, mapper.flow(3));
    assertEquals(SigningStatus.EXPIRED, mapper.flow(5));
    assertEquals(SigningStatus.REJECTED, mapper.flow(7));
    try {
      mapper.flow(99);
      fail();
    } catch (SigningException expected) {
      assertNotNull(expected);
    }
  }

  /** 发起人不隐式签署，双方签署人、自动启动和完结均显式设置。 */
  @Test
  public void explicitSignersAndAutoStart() {
    EsignConfiguration config = config();
    config.setCallbackUrl("https://example.org/callback");
    config.setOrganizationName("我方");
    SigningTask task = new SigningTask();
    task.setTitle("采购合同");
    SigningParty ours = party("OUR", "我方", true);
    ours.setOrganizationId("our-org-id");
    ours.setSealId("our-contract-seal");
    SigningParty counterparty = party("COUNTERPARTY", "供应商", false);
    counterparty.setOrganizationId("counterparty-org-id");
    Map<String, Object> request =
        new EsignPayloadMapper().create(task, "file", Arrays.asList(ours, counterparty), config);
    Map<String, Object> flow = SigningSupport.object(request.get("signFlowConfig"));
    assertEquals(false, flow.get("autoStart"));
    assertEquals(true, flow.get("autoFinish"));
    assertEquals(2, SigningSupport.array(request.get("signers")).size());
    Map<String, Object> signer =
        SigningSupport.object(SigningSupport.array(request.get("signers")).get(0));
    Map<String, Object> counterpartySigner =
        SigningSupport.object(SigningSupport.array(request.get("signers")).get(1));
    Map<String, Object> counterpartyOrganization =
        SigningSupport.object(counterpartySigner.get("orgSignerInfo"));
    Map<String, Object> counterpartyTransactor =
        SigningSupport.object(counterpartyOrganization.get("transactorInfo"));
    assertEquals("供应商", counterpartyOrganization.get("orgName"));
    assertFalse(counterpartyOrganization.containsKey("orgId"));
    assertEquals("13800000000", counterpartyTransactor.get("psnAccount"));
    assertEquals(
        "测试经办人", SigningSupport.object(counterpartyTransactor.get("psnInfo")).get("psnName"));
    Map<String, Object> field =
        SigningSupport.object(SigningSupport.array(signer.get("signFields")).get(0));
    assertEquals(
        "our-contract-seal",
        SigningSupport.object(field.get("normalSignFieldConfig")).get("assignedSealId"));
  }

  /** 手动签署按平台要求成对传入机构名称和经办人账号。 */
  @Test
  public void manualOrganizationSignerUsesNameAndAccountPair() {
    EsignConfiguration config = config();
    config.setCallbackUrl("https://example.org/callback");
    config.setOrganizationName("我方");
    SigningTask task = new SigningTask();
    task.setTitle("采购合同");
    SigningParty ours = party("OUR", "我方", false);
    ours.setOrganizationId("our-org-id");
    SigningParty counterparty = party("COUNTERPARTY", "供应商", false);
    counterparty.setOrganizationId("counterparty-org-id");

    Map<String, Object> request =
        new EsignPayloadMapper().create(task, "file", Arrays.asList(ours, counterparty), config);
    for (Object signerValue : SigningSupport.array(request.get("signers"))) {
      Map<String, Object> signer = SigningSupport.object(signerValue);
      Map<String, Object> organization = SigningSupport.object(signer.get("orgSignerInfo"));
      Map<String, Object> transactor = SigningSupport.object(organization.get("transactorInfo"));
      assertTrue(organization.containsKey("orgName"));
      assertFalse(organization.containsKey("orgId"));
      assertEquals("13800000000", transactor.get("psnAccount"));
    }
  }

  /** 个人签署对方使用个人账号参数，不携带供应商机构签署信息。 */
  @Test
  public void personalCounterpartyUsesPersonalSignerInfo() {
    EsignConfiguration config = config();
    config.setCallbackUrl("https://example.org/callback");
    config.setOrganizationName("我方");
    SigningTask task = new SigningTask();
    task.setTitle("采购合同");
    SigningParty ours = party("OUR", "我方", false);
    SigningParty counterparty = party("COUNTERPARTY", "供应商", false);
    counterparty.setPartyType(SigningPartyType.PERSON);
    counterparty.setOrganizationName(null);

    Map<String, Object> request =
        new EsignPayloadMapper().create(task, "file", Arrays.asList(ours, counterparty), config);
    Map<String, Object> signer =
        SigningSupport.object(SigningSupport.array(request.get("signers")).get(1));
    Map<String, Object> personalInfo = SigningSupport.object(signer.get("psnSignerInfo"));
    Map<String, Object> signerInfo = SigningSupport.object(personalInfo.get("psnInfo"));

    assertEquals(0, signer.get("signerType"));
    assertEquals("13800000000", personalInfo.get("psnAccount"));
    assertEquals("测试经办人", signerInfo.get("psnName"));
    assertFalse(signer.containsKey("orgSignerInfo"));
    assertEquals("1", SigningSupport.object(signer.get("noticeConfig")).get("noticeTypes"));
  }

  /** 不能替对方静默签署。 */
  @Test
  public void manualSealIsRestrictedAndAutoSealNeverFallsBack() {
    EsignConfiguration config = config();
    config.setCallbackUrl("https://example.org/callback");
    config.setOrganizationName("我方");
    SigningTask task = new SigningTask();
    task.setTitle("采购合同");
    SigningParty ours = party("OUR", "我方", false);
    SigningParty counterparty = party("COUNTERPARTY", "供应商", false);
    ours.setSealId("manual-seal");
    Map<String, Object> payload =
        new EsignPayloadMapper().create(task, "file", Arrays.asList(ours, counterparty), config);
    Map<String, Object> signer =
        SigningSupport.object(SigningSupport.array(payload.get("signers")).get(0));
    Map<String, Object> field =
        SigningSupport.object(SigningSupport.array(signer.get("signFields")).get(0));
    Map<String, Object> normal = SigningSupport.object(field.get("normalSignFieldConfig"));
    assertEquals(Collections.singletonList("manual-seal"), normal.get("availableSealIds"));
    assertFalse(normal.containsKey("assignedSealId"));
    ours.setAutoSign(true);
    ours.setSealId(null);
    assertThrows(
        SigningException.class,
        () ->
            new EsignPayloadMapper()
                .create(task, "file", Arrays.asList(ours, counterparty), config));
  }

  /** 不能替对方静默签署。 */
  @Test
  public void counterpartyAutoSignDenied() {
    EsignConfiguration config = config();
    config.setCallbackUrl("https://example.org/callback");
    try {
      new EsignPayloadMapper()
          .create(
              new SigningTask(),
              "file",
              Collections.singletonList(party("COUNTERPARTY", "供应商", true)),
              config);
      fail();
    } catch (SigningException expected) {
      assertNotNull(expected);
    }
  }

  /** 字段目录不丢弃零和 false。 */
  @Test
  public void mapKeepsFalseAndZero() {
    Map<String, Object> map = SigningSupport.map("zero", 0, "enabled", false);
    assertEquals(0, map.get("zero"));
    assertEquals(false, map.get("enabled"));
  }

  /** Java 8 常用 JSON 库可能把响应码解为 Double，SDK 仍应识别成功与数字枚举。 */
  @Test
  public void jsonLibraryNumericRepresentationsArePortable() {
    com.google.gson.Gson gson = new com.google.gson.Gson();
    com.ruoyi.signing.spi.SigningJsonCodec codec =
        new com.ruoyi.signing.spi.SigningJsonCodec() {
          public String write(Object value) {
            return gson.toJson(value);
          }

          public <T> T read(String value, Class<T> type) {
            return gson.fromJson(value, type);
          }
        };
    EsignV3Client client =
        new EsignV3Client(config(), codec) {
          @Override
          protected byte[] request(
              String method,
              String url,
              byte[] body,
              Map<String, String> headers,
              int limit,
              boolean uncertain) {
            return "{\"code\":0,\"data\":{\"fileId\":\"file-1\"}}".getBytes(StandardCharsets.UTF_8);
          }

          @Override
          public byte[] fileRequest(
              String method, String url, byte[] body, Map<String, String> headers) {
            return "{\"errCode\":0}".getBytes(StandardCharsets.UTF_8);
          }
        };
    assertEquals("file-1", client.call("GET", "/v3/files/file-1", null, false).get("fileId"));
    client.uploadFile("https://example.org/unused", new byte[] {1}, "test");
    assertEquals(SigningStatus.COMPLETED, new EsignStatusMapper().flow(2D));
    assertEquals("SIGNED", new EsignStatusMapper().party(2D));
    assertEquals("3", SigningSupport.text(SigningSupport.map("page", 3D), "page"));
  }

  /** 平台实名错误应说明来源、错误含义及处理方向。 */
  @Test
  public void esignRealNameErrorIsActionable() {
    com.google.gson.Gson gson = new com.google.gson.Gson();
    com.ruoyi.signing.spi.SigningJsonCodec codec =
        new com.ruoyi.signing.spi.SigningJsonCodec() {
          public String write(Object value) {
            return gson.toJson(value);
          }

          public <T> T read(String value, Class<T> type) {
            return gson.fromJson(value, type);
          }
        };
    EsignV3Client client =
        new EsignV3Client(config(), codec) {
          @Override
          protected byte[] request(
              String method,
              String url,
              byte[] body,
              Map<String, String> headers,
              int limit,
              boolean uncertain) {
            return "{\"code\":1437328,\"message\":\"实名信息不一致\"}".getBytes(StandardCharsets.UTF_8);
          }
        };
    try {
      client.call("POST", "/v3/sign-flow/create-by-file", Collections.emptyMap(), true);
      fail("应拒绝不匹配的实名信息");
    } catch (SigningException expected) {
      assertEquals("e签宝平台返回错误（1437328）： 实名信息不一致", expected.getMessage());
    }
  }

  /** 固定配置夹具，不含真实凭证。 */
  private static EsignConfiguration config() {
    EsignConfiguration config = new EsignConfiguration();
    config.setEnabled(true);
    config.setAppId("test-app");
    config.setAppSecret("test-secret");
    return config;
  }

  /** 构建有显式位置的签署方。 */
  private static SigningParty party(String role, String name, boolean auto) {
    SigningParty party = new SigningParty();
    party.setRole(role);
    party.setOrganizationName(name);
    party.setAutoSign(auto);
    party.setTransactorAccount("13800000000");
    party.setTransactorName("测试经办人");
    SigningPosition position = new SigningPosition();
    position.setPage(1);
    position.setX(120);
    position.setY(200);
    party.getPositions().add(position);
    return party;
  }
}
