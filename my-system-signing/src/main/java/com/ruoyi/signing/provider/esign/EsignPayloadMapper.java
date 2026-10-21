package com.ruoyi.signing.provider.esign;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.PartyRole;
import com.ruoyi.signing.model.enums.SigningPartyType;
import com.ruoyi.signing.provider.esign.config.EsignConfiguration;
import java.util.*;

/** 将领域快照显式转换为 V3 发起参数。 */
public final class EsignPayloadMapper {
  /** 生成显式创建不启动、自动完结的双方合同请求。 */
  public Map<String, Object> create(
      SigningTask task, String fileId, List<SigningParty> parties, EsignConfiguration config) {
    if (!config.getCallbackUrl().startsWith("https://")) {
      throw new SigningException("请配置公网 HTTPS 回调地址");
    }
    List<Object> signers = new ArrayList<Object>();
    for (SigningParty party : parties) {
      boolean ours = PartyRole.OUR.name().equals(party.getRole());
      SigningPartyType partyType =
          party.getPartyType() == null ? SigningPartyType.ORGANIZATION : party.getPartyType();
      if (ours && partyType != SigningPartyType.ORGANIZATION) {
        throw new SigningException("我方签署主体必须是企业或机构");
      }
      if (ours && !config.getOrganizationName().equals(party.getOrganizationName())) {
        throw new SigningException("我方主体与应用配置不一致");
      }
      if (!ours
          && (party.isAutoSign() || (party.getSealId() != null && !party.getSealId().isEmpty()))) {
        throw new SigningException("不能替对方指定自动落章或机构印章");
      }
      if (party.isAutoSign() && (party.getSealId() == null || party.getSealId().isEmpty())) {
        throw new SigningException("自动落章必须明确选择我方印章");
      }
      Map<String, Object> signer =
          map(
              "signerType",
              partyType == SigningPartyType.PERSON ? 0 : 1,
              "signConfig",
              map("signOrder", party.getSignOrder()),
              "noticeConfig",
              map("noticeTypes", party.isAutoSign() ? "" : "1"));
      if (!party.isAutoSign()) {
        Map<String, Object> signerInfo =
            map(
                "psnAccount",
                party.getTransactorAccount(),
                "psnInfo",
                map("psnName", party.getTransactorName()));
        if (partyType == SigningPartyType.PERSON) {
          signer.put("psnSignerInfo", signerInfo);
        } else {
          signer.put(
              "orgSignerInfo",
              map("orgName", party.getOrganizationName(), "transactorInfo", signerInfo));
        }
      }
      List<Object> fields = new ArrayList<Object>();
      for (SigningPosition position : party.getPositions()) {
        Map<String, Object> normal =
            map(
                "autoSign",
                party.isAutoSign(),
                "freeMode",
                false,
                "movableSignField",
                false,
                "signFieldStyle",
                1,
                "signFieldPosition",
                map(
                    "positionPage",
                    String.valueOf(position.getPage()),
                    "positionX",
                    position.getX(),
                    "positionY",
                    position.getY()));
        if (ours && party.getSealId() != null && !party.getSealId().isEmpty()) {
          if (party.isAutoSign()) {
            normal.put("assignedSealId", party.getSealId());
          } else {
            normal.put("availableSealIds", Collections.singletonList(party.getSealId()));
          }
        }
        fields.add(
            map(
                "fileId",
                fileId,
                "customBizNum",
                party.getRole(),
                "signFieldType",
                0,
                "normalSignFieldConfig",
                normal));
      }
      signer.put("signFields", fields);
      signers.add(signer);
    }
    return map(
        "docs",
        Collections.singletonList(map("fileId", fileId, "fileName", task.getTitle() + ".pdf")),
        "signFlowConfig",
        map(
            "signFlowTitle",
            task.getTitle(),
            "autoStart",
            false,
            "autoFinish",
            true,
            "notifyUrl",
            config.getCallbackUrl(),
            "noticeConfig",
            map("noticeTypes", "1")),
        "signers",
        signers);
  }
}
