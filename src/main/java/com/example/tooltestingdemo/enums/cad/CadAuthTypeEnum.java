package com.example.tooltestingdemo.enums.cad;

import java.util.Arrays;

public enum CadAuthTypeEnum {
    NONE("NONE", "无认证"),
    BASIC("BASIC", "Basic认证"),
    BEARER("BEARER", "Bearer Token认证"),
    API_KEY("API_KEY", "API Key认证"),
    CUSTOM_HEADERS("CUSTOM_HEADERS", "自定义请求头认证");

    private final String code;
    private final String description;

    CadAuthTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(String code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equalsIgnoreCase(code));
    }
}
