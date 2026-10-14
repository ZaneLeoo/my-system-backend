package com.ruoyi.framework.web.exception;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 新业务 Controller 统一返回 R，若依原有 Controller 继续使用原异常处理器。 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.ruoyi.business")
public class BusinessExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(BusinessExceptionHandler.class);

  /** 保留业务错误提示和业务指定的状态码。 */
  @ExceptionHandler(ServiceException.class)
  public R<Void> serviceError(ServiceException exception) {
    return R.fail(
        exception.getCode() == null ? R.FAIL : exception.getCode(), exception.getMessage());
  }

  /** 对新业务方法权限校验失败返回统一的 403 业务码。 */
  @ExceptionHandler(AccessDeniedException.class)
  public R<Void> accessDenied(AccessDeniedException exception) {
    return R.fail(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
  }

  /** 处理 JavaBean 查询参数和 JSON 请求体的字段校验错误。 */
  @ExceptionHandler(BindException.class)
  public R<Void> invalidBinding(BindException exception) {
    String message =
        exception.getAllErrors().isEmpty()
            ? "请求参数校验失败"
            : exception.getAllErrors().get(0).getDefaultMessage();
    return R.fail(message);
  }

  /** 拒绝非法 JSON、错误字段类型和未知枚举值，不暴露解析器内部信息。 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public R<Void> invalidBody(HttpMessageNotReadableException exception) {
    return R.fail("请求格式不正确，请检查字段类型和枚举值");
  }

  /** 为缺失的必需查询参数提供明确提示。 */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public R<Void> missingParameter(MissingServletRequestParameterException exception) {
    return R.fail("缺少必要参数：" + exception.getParameterName());
  }

  /** 处理方法级路径参数和查询参数校验。 */
  @ExceptionHandler(ConstraintViolationException.class)
  public R<Void> invalidParameter(ConstraintViolationException exception) {
    String message =
        exception.getConstraintViolations().isEmpty()
            ? "请求参数校验失败"
            : exception.getConstraintViolations().iterator().next().getMessage();
    return R.fail(message);
  }

  /** 对数字路径或查询参数的绑定类型错误返回稳定提示。 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public R<Void> typeMismatch(MethodArgumentTypeMismatchException exception) {
    return R.fail("请求参数类型不正确：" + exception.getName());
  }

  /** 记录未知异常，向客户端返回稳定提示而非底层异常细节。 */
  @ExceptionHandler(Exception.class)
  public R<Void> unexpectedError(Exception exception, HttpServletRequest request) {
    log.error("业务请求 {} 处理失败", request.getRequestURI(), exception);
    return R.fail("系统异常，请联系管理员");
  }
}
