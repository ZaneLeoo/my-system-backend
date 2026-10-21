package com.ruoyi.business.signing.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 来源选择统一视图。 */
@Getter
@Setter
public class SigningSourceView {
  /** 来源主键。 */
  private String id;

  /** 业务单据号。 */
  private String number;

  /** 默认合同标题。 */
  private String title;

  /** 单据日期。 */
  private LocalDate businessDate;

  /** 合同相对方名称。 */
  private String counterpartyName;
}
