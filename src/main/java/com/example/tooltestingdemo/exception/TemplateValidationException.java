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
        NAME_DUPLICATE(1001, "名称重复"),
        REQUIRED_FIELD_EMPTY(1002, "必填项为空"),
        INVALID_FORMAT(1003, "格式无效"),
        VALIDATION_FAILED(1004, "校验失败"),
        BUSINESS_RULE_VIOLATION(1005, "业务规则校验失败"),
        NOT_FOUND(1006, "数据不存在"),
        OPERATION_NOT_ALLOWED(1007, "操作不允许"),
        ALREADY_EXISTS(1008, "数据已存在"),
        RWEMARK_EXISTS(1010, "缺少remark字段"),
        CONVERT_ERROR(1009, "对象转换失败");

        private final Integer code;
        private final String desc;

        ErrorType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
