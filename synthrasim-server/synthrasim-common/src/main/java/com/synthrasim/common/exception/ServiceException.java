package com.synthrasim.common.exception;

/**
 * 业务异常
 * 
 * 在Service层中，当业务逻辑校验不通过时抛出此异常。
 * 全局异常处理器会捕获此异常并返回友好的错误提示给前端。
 * 
 * 使用示例：
 *   throw new ServiceException("用户名或密码错误");
 *   throw new ServiceException("该用户已被禁用", HttpStatus.FORBIDDEN);
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private Integer code;

    /** 错误提示 */
    private String message;

    /** 错误明细（内部调试用，不对外暴露） */
    private String detailMessage;

    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
