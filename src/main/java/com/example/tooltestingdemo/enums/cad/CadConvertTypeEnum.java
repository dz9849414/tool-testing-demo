package com.example.tooltestingdemo.enums.cad;

import java.util.Arrays;

public enum CadConvertTypeEnum {
    DATA("DATA", "数据字段转换"),
    FILE("FILE", "文件格式转换"),
    BOTH("BOTH", "数据和文件转换");

    private final String code;
    private final String description;

    CadConvertTypeEnum(String code, String description) {
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
