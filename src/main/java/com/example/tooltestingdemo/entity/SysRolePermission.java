package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 */
@TableName("sys_role_permission")
@Data
public class SysRolePermission {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "role_id")
    private String roleId;
    
    @TableField(value = "permission_id")
    private String permissionId;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "create_user")
    private String createUser;
}