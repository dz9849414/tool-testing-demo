package com.example.tooltestingdemo.util;

import java.util.UUID;

/**
 * ID生成工具类
 */
public class IdGenerator {
    
    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成雪花ID（简化版，实际项目中应使用完整的雪花算法）
     */
    public static String generateSnowflakeId() {
        // 简化实现，实际项目中应使用完整的雪花算法
        return String.valueOf(System.currentTimeMillis()) + 
               String.valueOf((int)(Math.random() * 1000));
    }
    
    /**
     * 生成短ID（8位）
     */
    public static String generateShortId() {
        return Long.toHexString(System.currentTimeMillis()).substring(0, 8).toUpperCase();
    }
}