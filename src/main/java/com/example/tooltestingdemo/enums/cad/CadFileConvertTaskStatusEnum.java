package com.example.tooltestingdemo.enums.cad;

public enum CadFileConvertTaskStatusEnum {
    WAITING(10, "待转换"),
    RUNNING(20, "转换中"),
    SUCCESS(30, "转换成功"),
    FAILED(40, "转换失败"),
    CANCELED(50, "已取消");

    private final int code;
    private final String description;

    CadFileConvertTaskStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
