package com.synthrasim.common.exception;

import com.synthrasim.common.constant.HttpStatus;
import com.synthrasim.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 
 * 使用 @RestControllerAdvice 拦截所有Controller抛出的异常，
 * 统一转换为AjaxResult格式返回，避免将堆栈信息直接暴露给前端。
 * 
 * 异常处理优先级：精确匹配 > 父类匹配
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 处理业务异常 - 由Service层主动抛出 */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error("业务异常: URI={}, message={}", request.getRequestURI(), e.getMessage());
        Integer code = e.getCode();
        return code != null ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /** 处理权限不足异常 - Spring Security抛出 */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("权限不足: URI={}", request.getRequestURI());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    /** 处理HTTP请求方法不支持异常 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("不支持的请求方法: URI={}, method={}", request.getRequestURI(), e.getMethod());
        return AjaxResult.error("不支持'" + e.getMethod() + "'请求");
    }

    /** 处理参数校验异常 - @Valid注解校验失败时抛出 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验失败: {}", e.getMessage());
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return AjaxResult.error(message);
    }

    /** 处理绑定异常 - 表单参数绑定失败 */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e) {
        log.error("参数绑定失败: {}", e.getMessage());
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /** 兜底处理：所有未被上面捕获的运行时异常 */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: URI={}", request.getRequestURI(), e);
        return AjaxResult.error("系统内部错误，请联系管理员");
    }

    /** 兜底处理：所有未被捕获的异常 */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: URI={}", request.getRequestURI(), e);
        return AjaxResult.error("系统内部错误，请联系管理员");
    }
}
