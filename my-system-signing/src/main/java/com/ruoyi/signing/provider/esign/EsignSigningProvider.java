package com.ruoyi.signing.provider.esign;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.PartyRole;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import com.ruoyi.signing.spi.SigningProvider;
import java.util.*;

/** e签宝 V3 实现，平台数字状态和原始 DTO 不进入业务模块。 */
public class EsignSigningProvider implements SigningProvider {
  private final EsignV3Client client;
  private final EsignConfiguration config;
  private final EsignPayloadMapper payloads = new EsignPayloadMapper();

  /** 注入已配置 HTTP 客户端。 */
  public EsignSigningProvider(EsignV3Client client, EsignConfiguration config) {
    this.client = client;
    this.config = config;
  }

  /** {@inheritDoc} */
  public String environment() {
    return "esign:" + config.getEnvironment() + ":" + config.getAppId();
  }

  /** {@inheritDoc} */
  public String upload(String name, byte[] bytes) {
    String md5 = EsignRequestSigner.contentMd5(bytes);
    Map<String, Object> data =
        client.call(
            "POST",
            "/v3/files/file-upload-url",
            map(
                "fileName",
                name,
                "fileSize",
                bytes.length,
                "contentMd5",
                md5,
                "contentType",
                "application/octet-stream",
                "convertToPDF",
                !name.toLowerCase(Locale.ROOT).endsWith(".pdf")),
            false);
    client.uploadFile(text(data, "fileUploadUrl"), bytes, md5);
    return required(text(data, "fileId"), 100, "平台文件 ID");
  }

  /** {@inheritDoc} */
  public Map<String, Object> file(String id) {
    return client.call("GET", "/v3/files/" + EsignV3Client.encode(id), null, false);
  }

  /** {@inheritDoc} */
  public String fill(String id, List<Map<String, Object>> components) {
    Map<String, Object> result =
        client.call(
            "POST",
            "/v3/files/create-by-doc-template",
            map(
                "docTemplateId",
                id,
                "fileName",
                "采购合同.pdf",
                "requiredCheck",
                true,
                "components",
                components),
            false);
    return required(text(result, "fileId"), 100, "模板生成文件 ID");
  }

  /** {@inheritDoc} */
  public String createFlow(SigningTask task, String fileId, List<SigningParty> parties) {
    Map<String, Object> result =
        client.call(
            "POST",
            "/v3/sign-flow/create-by-file",
            payloads.create(task, fileId, parties, config),
            true);
    String id = text(result, "signFlowId");
    if (id.isEmpty()) {
      throw new SigningException("平台未返回流程 ID，请人工核对", true);
    }
    return id;
  }

  /** {@inheritDoc} */
  public void start(String id) {
    client.call("POST", "/v3/sign-flow/" + EsignV3Client.encode(id) + "/start", null, false);
  }

  /** {@inheritDoc} */
  public com.ruoyi.signing.api.result.SigningFlowResult detail(String id) {
    Map<String, Object> data =
        client.call("GET", "/v3/sign-flow/" + EsignV3Client.encode(id) + "/detail", null, false);
    com.ruoyi.signing.api.result.SigningFlowResult result =
        new com.ruoyi.signing.api.result.SigningFlowResult();
    result.setStatus(new EsignStatusMapper().flow(data.get("signFlowStatus")));
    for (Object value : array(data.get("docs"))) {
      result.getFileIds().add(text(object(value), "fileId"));
    }
    for (Object value : array(data.get("signers"))) {
      Map<String, Object> signer = object(value);
      for (Object field : array(signer.get("signFields"))) {
        String role = text(object(field), "customBizNum");
        if (!role.isEmpty()) {
          result
              .getPartyStatuses()
              .put(role, new EsignStatusMapper().party(signer.get("signStatus")));
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public void revoke(String id, String reason) {
    client.call(
        "POST",
        "/v3/sign-flow/" + EsignV3Client.encode(id) + "/revoke",
        map("revokeReason", reason),
        false);
  }

  /** {@inheritDoc} */
  public byte[] signedFile(String id, String fileId) {
    Map<String, Object> result =
        client.call(
            "POST",
            "/v3/sign-flow/" + EsignV3Client.encode(id) + "/file-download-url",
            map("urlAvailableDate", 300),
            false);
    for (Object file : array(result.get("files"))) {
      Map<String, Object> item = object(file);
      if (fileId.equals(text(item, "fileId"))) {
        return download(text(item, "downloadUrl"));
      }
    }
    throw new SigningException("平台未返回对应已签文件");
  }

  /** {@inheritDoc} */
  public byte[] download(String url) {
    return client.fileRequest("GET", url, new byte[0], Collections.<String, String>emptyMap());
  }

  /** {@inheritDoc} */
  public Map<String, Object> templates(int page) {
    if (page < 1) {
      throw new SigningException("页码不合法");
    }
    return client.call("GET", "/v3/doc-templates?pageNum=" + page + "&pageSize=20", null, false);
  }

  /** {@inheritDoc} */
  public Map<String, Object> template(String id) {
    return client.call("GET", "/v3/doc-templates/" + EsignV3Client.encode(id), null, false);
  }

  /** {@inheritDoc} */
  public String templateEditUrl(String id) {
    return text(
        client.call(
            "POST",
            "/v3/doc-templates/" + EsignV3Client.encode(id) + "/doc-template-edit-url",
            map(),
            false),
        "docTemplateEditUrl");
  }

  /** {@inheritDoc} */
  public String positionUrl(String fileId, String session) {
    return text(
        client.call(
            "POST",
            "/v3/files/get-seal-position-url",
            map(
                "fileId",
                fileId,
                "customBizNum",
                session,
                "signerRoles",
                Arrays.asList(PartyRole.OUR.name(), PartyRole.COUNTERPARTY.name()),
                "signFieldType",
                Collections.singletonList(1),
                "clientType",
                "PC"),
            false),
        "getSealPositionUrl");
  }
}
