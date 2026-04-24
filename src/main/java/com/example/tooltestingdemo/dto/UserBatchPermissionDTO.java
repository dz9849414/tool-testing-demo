package com.example.tooltestingdemo.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户批量分配权限DTO
 */
@Data
public class UserBatchPermissionDTO {
    
    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
    
    /**
     * 权限ID列表
     */
    @NotEmpty(message = "权限ID列表不能为空")
    private List<String> permissions;
    
    /**
     * 操作类型：ADD-添加权限，REMOVE-移除权限，REPLACE-替换权限
     */
    private String operationType = "ADD";
}