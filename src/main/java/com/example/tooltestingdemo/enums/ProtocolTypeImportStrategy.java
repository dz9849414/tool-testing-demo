package com.example.tooltestingdemo.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 协议类型导入策略
 */
@Getter
public enum ProtocolTypeImportStrategy {

    OVERWRITE("OVERWRITE"),
    INCREMENTAL("INCREMENTAL");

    private final String code;

    ProtocolTypeImportStrategy(String code) {
        this.code = code;
    }

    public static ProtocolTypeImportStrategy fromCode(String code) {
        for (ProtocolTypeImportStrategy strategy : values()) {
            if (StringUtils.equalsIgnoreCase(strategy.code, code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("导入策略不合法，仅支持 OVERWRITE 或 INCREMENTAL");
    }
}