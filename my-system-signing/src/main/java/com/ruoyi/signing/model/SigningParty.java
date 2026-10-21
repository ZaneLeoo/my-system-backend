package com.ruoyi.signing.model;

import com.ruoyi.signing.model.enums.SigningPartyType;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 签署主体和授权经办人快照。 */
@Getter
@Setter
public class SigningParty {
  /** 本地签署方 ID。 */
  private String id;

  /** 所属任务。 */
  private String taskId;

  /** OUR 或 COUNTERPARTY。 */
  private String role;

  /** 签署主体类型；旧任务未保存此字段时按企业主体兼容。 */
  private SigningPartyType partyType = SigningPartyType.ORGANIZATION;

  /** 平台机构 ID，可空。 */
  private String organizationId;

  /** 签署机构名称；个人签署时为空。 */
  private String organizationName;

  /** 企业签署经办人或个人签署人的姓名。 */
  private String transactorName;

  /** 签署账号手机号或邮箱。 */
  private String transactorAccount;

  /** 我方手动签署对应的本地用户。 */
  private Long localUserId;

  /** 本次任务是否由我方企业后台自动落章。 */
  private boolean autoSign;

  /** 签署顺序。 */
  private int signOrder = 1;

  /** 发起时选定的我方印章 ID；手动签署可空。 */
  private String sealId;

  /** 发起时由平台核实的印章名称快照。 */
  private String sealName;

  /** 发起时由平台核实的印章业务类型快照。 */
  private com.ruoyi.signing.model.enums.SigningSealType sealType;

  /** 平台签署方状态。 */
  private String status;

  /** 该方签章位置。 */
  private List<SigningPosition> positions = new ArrayList<SigningPosition>();
}
