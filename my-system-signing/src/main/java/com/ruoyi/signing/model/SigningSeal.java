package com.ruoyi.signing.model;

import com.ruoyi.signing.model.enums.*;
import lombok.Getter;
import lombok.Setter;

/** 我方企业印章的即时信息，不包含平台图片下载链接。 */
@Getter
@Setter
public class SigningSeal {
  /** 平台印章编号。 */
  private String id;

  /** 印章名称。 */
  private String name;

  /** 所属机构编号，由服务端确定。 */
  private String organizationId;

  /** 所属机构名称。 */
  private String organizationName;

  /** 机构印章业务类型。 */
  private SigningSealType type;

  /** 统一印章状态。 */
  private SigningSealStatus status;

  /** 平台返回的状态描述。 */
  private String statusDescription;

  /** 是否为平台默认印章，仅用于展示，不代表自动选中。 */
  private boolean defaultSeal;

  /** 宽度，单位毫米。 */
  private Integer width;

  /** 高度，单位毫米。 */
  private Integer height;

  /** 当前是否允许新选。 */
  private boolean selectable;

  /** 平台是否提供带水印的预览图片。 */
  private boolean previewAvailable;
}
