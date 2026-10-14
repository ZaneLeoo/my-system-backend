package com.ruoyi.business.masterdata.category.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 分类请求错误使用明确提示，避免返回底层 JSON 解析细节。 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ItemCategoryController.class)
public class CategoryExceptionHandler {

  /** 拒绝不合法 JSON、错误类型和未知状态编码。 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public AjaxResult invalidBody(HttpMessageNotReadableException exception) {
    return AjaxResult.error("请求格式不正确，请检查字段类型；分类状态只能为 0（启用）或 1（停用）");
  }

  /** 为缺失版本或其他必需查询参数返回稳定的业务提示。 */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public AjaxResult missingParameter(MissingServletRequestParameterException exception) {
    return AjaxResult.error("缺少必要参数：" + exception.getParameterName());
  }

  /** 对方法级路径和查询参数约束返回校验信息。 */
  @ExceptionHandler(ConstraintViolationException.class)
  public AjaxResult invalidParameter(ConstraintViolationException exception) {
    return AjaxResult.error(exception.getConstraintViolations().iterator().next().getMessage());
  }
}
