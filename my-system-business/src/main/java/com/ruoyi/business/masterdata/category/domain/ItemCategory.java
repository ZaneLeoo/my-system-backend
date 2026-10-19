package com.ruoyi.business.masterdata.category.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BusinessEntity;
import lombok.Getter;
import lombok.Setter;

/** 存货分类实体，继承公共审计与逻辑删除约定。 */
@Getter
@Setter
@TableName("md_item_category")
public class ItemCategory extends BusinessEntity {
  /** 分类 ID。 */
  @TableId(type = IdType.AUTO)
  private Long categoryId;

  /** 上级分类 ID。 */
  private Long parentId;

  /** 分类编码。 */
  private String categoryCode;

  /** 分类名称。 */
  private String categoryName;

  /** 显示顺序。 */
  private Integer sortOrder;

  /** 启停状态。 */
  private CategoryStatus status;

  /** 备注。 */
  private String remark;

  /** 编辑版本号。 */
  private Integer version;
}
