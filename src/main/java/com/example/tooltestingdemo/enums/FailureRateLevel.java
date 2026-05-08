package com.example.tooltestingdemo.enums;

/**
 * 断言失败率等级枚举，用于断言失败预警判断和建议
 */
public enum FailureRateLevel {
    EXCELLENT(0, 5, "优秀", "断言失败率极低，数据质量良好", 
        "保持当前状态，持续监控数据质量"),
    
    GOOD(6, 10, "良好", "断言失败率较低，数据质量正常", 
        "建议定期检查断言规则和数据源"),
    
    WARNING(11, 20, "警告", "断言失败率偏高，需要关注", 
        "检查数据质量和断言规则，分析失败原因"),
    
    DANGER(21, 40, "危险", "断言失败率较高，存在严重问题", 
        "立即检查数据源和断言逻辑，修复数据异常"),
    
    CRITICAL(41, 100, "严重", "断言失败率极高，系统异常", 
        "紧急处理：暂停相关任务，全面检查数据流程和断言规则");

    private final int minRate;
    private final int maxRate;
    private final String level;
    private final String description;
    private final String suggestion;

    FailureRateLevel(int minRate, int maxRate, String level, String description, String suggestion) {
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
     * 根据失败率获取对应的等级
     */
    public static FailureRateLevel fromRate(double rate) {
        // 处理边界情况
        if (rate <= 0) {
            return EXCELLENT;
        }
        if (rate > 100) {
            return CRITICAL;
        }
        
        for (FailureRateLevel level : values()) {
            if (rate > level.minRate && rate <= level.maxRate) {
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