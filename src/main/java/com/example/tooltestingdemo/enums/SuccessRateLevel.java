package com.example.tooltestingdemo.enums;

/**
 * 成功率等级枚举，用于成功率预警判断和建议
 */
public enum SuccessRateLevel {
    EXCELLENT(95, 100, "优秀", "接口成功率优秀，服务运行稳定",
            "保持当前监控策略，系统运行状态良好，无需额外处理"),

    GOOD(85, 94, "良好", "接口成功率良好，服务运行正常",
            "持续日常监控，关注流量波动，确保服务稳定运行"),

    WARNING(70, 84, "警告", "接口成功率偏低，存在潜在风险",
            "核查接口响应耗时、调用链路及异常日志，排查性能瓶颈与调用异常"),

    DANGER(50, 69, "危险", "接口成功率过低，影响业务正常使用",
            "立即分析接口失败原因，检查第三方依赖、数据库状态及服务承载能力"),

    CRITICAL(0, 49, "严重", "接口成功率严重异常，业务基本不可用",
            "紧急处置：检查服务可用性、网络连通性、中间件状态，必要时进行服务重启与扩容");

    private final int minRate;
    private final int maxRate;
    private final String level;
    private final String description;
    private final String suggestion;

    SuccessRateLevel(int minRate, int maxRate, String level, String description, String suggestion) {
        this.minRate = minRate;
        this.maxRate = maxRate;
        this.level = level;
        this.description = description;
        this.suggestion = suggestion;
    }

    public int getMinRate() {
        return minRate;
    }

    public int getMaxRate() {
        return maxRate;
    }

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public String getSuggestion() {
        return suggestion;
    }

    /**
     * 根据成功率获取对应的等级
     */
    public static SuccessRateLevel fromRate(double rate) {
        if (rate >= 100) {
            return EXCELLENT;
        }
        if (rate < 0) {
            return CRITICAL;
        }
        for (SuccessRateLevel level : values()) {
            if (rate >= level.minRate && rate <= level.maxRate) {
                return level;
            }
        }
        return CRITICAL;
    }

    /**
     * 判断是否需要预警
     */
    public boolean needsWarning() {
        return this == WARNING || this == DANGER || this == CRITICAL;
    }

    /**
     * 获取预警级别（HIGH/MEDIUM/LOW）
     */
    public String getWarningLevel() {
        switch (this) {
            case CRITICAL:
            case DANGER:
                return "HIGH";
            case WARNING:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
}