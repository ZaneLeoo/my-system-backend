package com.ruoyi.signing.api.result;

import com.ruoyi.signing.model.SigningSeal;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 印章分页结果，保持平台分页总数。 */
@Getter
@Setter
public class SigningSealPage {
  /** 当前页印章。 */
  private List<SigningSeal> rows = new ArrayList<SigningSeal>();

  /** 筛选后的总数。 */
  private long total;
}
