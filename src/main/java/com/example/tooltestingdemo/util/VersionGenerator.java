package com.example.tooltestingdemo.util;

import org.springframework.util.StringUtils;

/**
 * 版本号生成工具类
 * <p>
 * 规则：
 * 1. 初始版本：V1.0
 * 2. 小修改（编辑）：V1.1 -> V1.2 ...
 * 3. 大修改（提交审核）：V1.0 -> V2.0
 */
public class VersionGenerator {

    /**
     * 生成初始版本号
     */
    public static String generateInitialVersion() {
        return "V1.0";
    }

    /**
     * 递增小版本号（编辑时）
     * V1.0 -> V1.1
     * V1.9 -> V1.10
     */
    public static String incrementMinorVersion(String currentVersion) {
        if (!StringUtils.hasText(currentVersion)) {
            return generateInitialVersion();
        }

        try {
            // 移除V前缀
            String version = currentVersion.trim().toUpperCase();
            if (version.startsWith("V")) {
                version = version.substring(1);
            }

            // 解析主版本和次版本
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            // 递增次版本
            minor++;

            return "V" + major + "." + minor;
        } catch (Exception e) {
            return generateInitialVersion();
        }
    }

    /**
     * 递增主版本号（提交审核通过时）
     * V1.5 -> V2.0
     */
    public static String incrementMajorVersion(String currentVersion) {
        if (!StringUtils.hasText(currentVersion)) {
            return generateInitialVersion();
        }

        try {
            // 移除V前缀
            String version = currentVersion.trim().toUpperCase();
            if (version.startsWith("V")) {
                version = version.substring(1);
            }

            // 解析主版本
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);

            // 递增主版本，次版本归零
            major++;

            return "V" + major + ".0";
        } catch (Exception e) {
            return generateInitialVersion();
        }
    }

    /**
     * 验证版本号格式是否有效
     */
    public static boolean isValidVersion(String version) {
        if (!StringUtils.hasText(version)) {
            return false;
        }

        String v = version.trim().toUpperCase();
        if (!v.startsWith("V")) {
            return false;
        }

        v = v.substring(1);
        String[] parts = v.split("\\.");

        if (parts.length < 1 || parts.length > 2) {
            return false;
        }

        try {
            Integer.parseInt(parts[0]);
            if (parts.length > 1) {
                Integer.parseInt(parts[1]);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
