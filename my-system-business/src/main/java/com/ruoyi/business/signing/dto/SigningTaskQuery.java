package com.ruoyi.business.signing.dto;

import com.ruoyi.common.core.page.PageQuery;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 电子签工作台的受控分页筛选。 */
@Getter
@Setter
public class SigningTaskQuery extends PageQuery {
  /** 已登记业务类型，留空查询当前用户可访问的全部场景。 */
  @Size(max = 64)
  private String businessType;

  /** 平台流程状态，草稿为本地尚未发起的任务。 */
  @Pattern(regexp = "DRAFT|SIGNING|COMPLETED|REVOKED|EXPIRED|REJECTED")
  private String status;
}
