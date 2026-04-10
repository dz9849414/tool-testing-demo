package com.example.tooltestingdemo.enums;

/**
 * 角色枚举
 */
public enum RoleEnum {
    
    /**
     * 管理员角色
     */
    ADMIN("admin", "系统管理员"),
    
    /**
     * 经理角色
     */
    MANAGER("manager", "部门经理"),
 
    /**
     * 普通用户角色
     */
    USER("user", "普通用户");
    
    private final String code;
    private final String name;
    
    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据角色代码获取角色枚举
     */
    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER;
    }
}