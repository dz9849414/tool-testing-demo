package com.example.tooltestingdemo.enums;

/**
 * 系统负载等级枚举，用于系统负载预警判断和建议
 */
public enum SystemLoadLevel {
    NORMAL(0, 200, "正常", "系统负载正常，运行平稳", 
        "保持监控，无需特殊处理"),
    
    LOW(201, 400, "轻度负载", "系统负载轻度增加", 
        "关注系统资源使用情况，准备扩容预案"),
    
    MEDIUM(401, 600, "中度负载", "系统负载中度偏高", 
        "检查服务器CPU、内存、磁盘使用情况，考虑横向扩容"),
    
    HIGH(601, 800, "高度负载", "系统负载较高，接近瓶颈", 
        "立即检查系统性能，优化慢查询，考虑紧急扩容"),
    
    CRITICAL(801, Integer.MAX_VALUE, "严重负载", "系统负载严重过高，存在宕机风险", 
        "紧急处理：检查服务状态，开启熔断降级，优先保障核心服务");

    private final int minCount;
    private final int maxCount;
    private final String level;
    private final String description;
    private final String suggestion;

    SystemLoadLevel(int minCount, int maxCount, String level, String description, String suggestion) {
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.level = level;
        this.description = description;
        this.suggestion = suggestion;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
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
     * 根据执行次数获取对应的负载等级
     */
    public static SystemLoadLevel fromCount(int count) {
        // 处理边界情况
        if (count < 0) {
            return NORMAL;
        }
        
        for (SystemLoadLevel level : values()) {
            if (count >= level.minCount && count <= level.maxCount) {
                return level;
            }
        }
        return CRITICAL;
    }

    /**
     * 判断是否需要预警
     */
    public boolean needsWarning() {
        return this != NORMAL;
    }

    /**
     * 获取预警级别（HIGH/MEDIUM/LOW）
     */
    public String getWarningLevel() {
        switch (this) {
            case CRITICAL:
                return "HIGH";
            case HIGH:
                return "HIGH";
            case MEDIUM:
                return "MEDIUM";
            case LOW:
                return "LOW";
            default:
                return "LOW";
        }
    }
}