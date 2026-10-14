package com.ruoyi.business.masterdata.category.vo;

import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import java.time.LocalDateTime;

/** 分类详情与平铺树表行；matched=false 表示仅为保留层级而返回的祖先。 */
public record CategoryView(
    Long categoryId,
    Long parentId,
    String categoryCode,
    String categoryName,
    Integer sortOrder,
    CategoryStatus status,
    String remark,
    Integer version,
    String createBy,
    LocalDateTime createTime,
    String updateBy,
    LocalDateTime updateTime,
    boolean matched) {

  /** 将持久化对象转换为只读响应，不暴露逻辑删除标记。 */
  public static CategoryView from(ItemCategory category, boolean matched) {
    return new CategoryView(
        category.getCategoryId(),
        category.getParentId(),
        category.getCategoryCode(),
        category.getCategoryName(),
        category.getSortOrder(),
        category.getStatus(),
        category.getRemark(),
        category.getVersion(),
        category.getCreateBy(),
        category.getCreateTime(),
        category.getUpdateBy(),
        category.getUpdateTime(),
        matched);
  }
}
