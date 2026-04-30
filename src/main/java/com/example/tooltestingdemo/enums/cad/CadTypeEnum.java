package com.example.tooltestingdemo.enums.cad;

import java.util.Arrays;

public enum CadTypeEnum {
    NX("NX", "NX软件"),
    CATIA("CATIA", "CATIA软件"),
    ZWCAD("ZWCAD", "中望CAD软件"),
    CUSTOM("CUSTOM", "自定义CAD软件");

    private final String code;
    private final String description;

    CadTypeEnum(String code, String description) {
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
