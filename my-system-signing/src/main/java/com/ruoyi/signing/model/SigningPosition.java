package com.ruoyi.signing.model;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

/** 最终 PDF 上的签章位置。 */
@Getter
@Setter
public class SigningPosition {
  /** 从 1 开始的页码。 */
  private int page;

  /** 平台坐标 X。 */
  private double x;

  /** 平台坐标 Y。 */
  private double y;
}
