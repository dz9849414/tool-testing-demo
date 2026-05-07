package com.example.tooltestingdemo.enums;

/**
 * 报告类型枚举
 */
public enum ReportTypeEnum {
    
    WEEKLY_EXECUTION("WEEKLY_EXECUTION", "周执行"),
    SUCCESS_RATE("SUCCESS_RATE", "成功率"),
    RESPONSE_TIME("RESPONSE_TIME", "响应时间"),
    PROTOCOL_DISTRIBUTION("PROTOCOL_DISTRIBUTION", "协议分布"),
    FAILURE_REASONS("FAILURE_REASONS", "失败原因");
    
    private final String code;
    private final String description;
    
    ReportTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static ReportTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ReportTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取中文描述
     */
    public static String getDescriptionByCode(String code) {
        ReportTypeEnum type = getByCode(code);
        return type != null ? type.description : code;
    }
    
    /**
     * 将字符串中的英文报告类型替换为中文
     */
    public static String translateReportType(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String result = text;
        for (ReportTypeEnum type : values()) {
            result = result.replace(type.getCode(), type.getDescription());
        }
        return result;
    }
}