package com.ruoyi.business.signing.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 通用来源单据查询。 */
@Getter
@Setter
public class SigningSourceQuery extends PageQuery {
  /** 单据号关键词，由业务适配器解释。 */
  @Size(max = 64)
  private String keyword;
}
