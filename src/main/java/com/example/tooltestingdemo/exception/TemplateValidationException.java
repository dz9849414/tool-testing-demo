package com.example.tooltestingdemo.exception;

import lombok.Getter;

import java.util.List;

/**
 * 模板校验异常
 * 
 * 用于返回具体的校验错误信息
 */
@Getter
public class TemplateValidationException extends RuntimeException {

    /**
     * 错误类型
     */
    private final ErrorType errorType;
    
    /**
     * 详细错误信息列表
     */
    private final List<String> errors;

    public TemplateValidationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.errors = List.of(message);
    }

    public TemplateValidationException(ErrorType errorType, List<String> errors) {
        super(String.join("; ", errors));
        this.errorType = errorType;
        this.errors = errors;
    }

    public TemplateValidationException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errors = List.of(message);
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NAME_DUPLICATE("NAME_DUPLICATE", "名称重复"),
        REQUIRED_FIELD_EMPTY("REQUIRED_FIELD_EMPTY", "必填项为空"),
        INVALID_FORMAT("INVALID_FORMAT", "格式无效"),
        VALIDATION_FAILED("VALIDATION_FAILED", "校验失败"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "业务规则校验失败");

        private final String code;
        private final String desc;

        ErrorType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
