package com.ruoyi.business.masterdata.category.dto;

import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import jakarta.validation.constraints.*;

/** 分类保存参数；审计字段和删除标记不属于可写入契约。 */
public record SaveCategoryRequest(
    @Positive Long categoryId,
    @NotNull @PositiveOrZero Long parentId,
    @NotBlank @Size(max = 64) String categoryCode,
    @NotBlank @Size(max = 100) String categoryName,
    @NotNull @PositiveOrZero Integer sortOrder,
    @NotNull CategoryStatus status,
    @Size(max = 500) String remark,
    @PositiveOrZero Integer version) {

  /** 统一清理编码、名称和备注的首尾空白，随后由 Bean Validation 校验。 */
  public SaveCategoryRequest {
    categoryCode = categoryCode == null ? null : categoryCode.strip();
    categoryName = categoryName == null ? null : categoryName.strip();
    remark = remark == null ? "" : remark.strip();
  }
}
