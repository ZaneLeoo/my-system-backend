package com.ruoyi.business.masterdata.category.vo;

import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.domain.ItemCategory;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 分类详情与平铺树表响应，与持久化基类独立。 */
@Getter
@Setter
public class CategoryView {
  /** 分类 ID。 */
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

  /** 创建账号。 */
  private String createBy;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 修改账号。 */
  private String updateBy;

  /** 修改时间。 */
  private LocalDateTime updateTime;

  /** 是否真正匹配筛选条件，false 表示层级上下文。 */
  private boolean matched;

  /** 创建供请求绑定和普通 Java 调用使用的空对象。 */
  public CategoryView() {}

  /** 使用完整字段创建对象，复用 Setter 的规范化规则。 */
  public CategoryView(
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
    setCategoryId(categoryId);
    setParentId(parentId);
    setCategoryCode(categoryCode);
    setCategoryName(categoryName);
    setSortOrder(sortOrder);
    setStatus(status);
    setRemark(remark);
    setVersion(version);
    setCreateBy(createBy);
    setCreateTime(createTime);
    setUpdateBy(updateBy);
    setUpdateTime(updateTime);
    setMatched(matched);
  }

  /** 从实体转换为只读接口数据，不暴露逻辑删除标记。 */
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
