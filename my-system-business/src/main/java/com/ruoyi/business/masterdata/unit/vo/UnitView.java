package com.ruoyi.business.masterdata.unit.vo;

import com.ruoyi.business.masterdata.unit.domain.Unit;
import com.ruoyi.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 单位列表与详情响应。 */
@Getter
@Setter
public class UnitView {
  /** 单位 ID。 */
  private Long unitId;

  /** 单位编码。 */
  private String unitCode;

  /** 单位名称。 */
  private String unitName;

  /** 启停状态。 */
  private EnableStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;

  /** 创建人。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改人。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 显式映射对外字段，避免返回内部删除标记。 */
  public static UnitView from(Unit entity) {
    UnitView view = new UnitView();
    view.setUnitId(entity.getUnitId());
    view.setUnitCode(entity.getUnitCode());
    view.setUnitName(entity.getUnitName());
    view.setStatus(entity.getStatus());
    view.setRemark(entity.getRemark());
    view.setVersion(entity.getVersion());
    view.setCreateBy(entity.getCreateBy());
    view.setCreateTime(entity.getCreateTime());
    view.setUpdateBy(entity.getUpdateBy());
    view.setUpdateTime(entity.getUpdateTime());
    return view;
  }
}
