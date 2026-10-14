package com.ruoyi.business.masterdata.category.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 分类树表查询参数；状态仍使用字符串以兼容 URL 查询绑定。 */
public record CategoryQuery(
    @Size(max = 64) String categoryCode,
    @Size(max = 100) String categoryName,
    @Pattern(regexp = "[01]", message = "分类状态必须为 0 或 1") String status) {}
