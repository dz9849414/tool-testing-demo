package com.example.tooltestingdemo.exception;

import com.example.tooltestingdemo.common.ErrorStatus;
import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑相关的异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误状态
     */
    private final ErrorStatus errorStatus;
    
    /**
     * 错误消息
     */
    private final String errorMessage;

    public BusinessException(ErrorStatus errorStatus, String errorMessage) {
        super(errorMessage);
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;
    }

    public BusinessException(ErrorStatus errorStatus, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;
    }

    /**
     * 获取错误代码
     */
    public int getCode() {
        return errorStatus.getCode();
    }
}