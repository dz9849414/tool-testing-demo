package com.example.tooltestingdemo.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 元对象字段填充处理器
 * 用于自动填充创建时间、更新时间、创建人等字段
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    // ThreadLocal 存储当前用户ID
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<>();
    
    /**
     * 设置当前用户（在Controller中调用）
     */
    public static void setCurrentUser(Long userId, String userName) {
        CURRENT_USER_ID.set(userId);
        CURRENT_USER_NAME.set(userName);
    }
    
    /**
     * 清除当前用户（在Controller中调用）
     */
    public static void clearCurrentUser() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_NAME.remove();
    }
    
    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 填充创建人ID（create_id）
        if (metaObject.hasSetter("createId")) {
            Long userId = CURRENT_USER_ID.get();
            if (userId == null) {
                userId = 1L; // 默认用户ID
            }
            this.strictInsertFill(metaObject, "createId", Long.class, userId);
        }
        
        // 填充创建人姓名（create_name）
        if (metaObject.hasSetter("createName")) {
            String userName = CURRENT_USER_NAME.get();
            if (userName == null) {
                userName = "管理员";
            }
            this.strictInsertFill(metaObject, "createName", String.class, userName);
        }
        
        // 填充创建人ID（create_id）- 用于历史记录表
        if (metaObject.hasSetter("createId")) {
            Long userId = CURRENT_USER_ID.get();
            if (userId == null) {
                userId = 1L;
            }
            this.strictInsertFill(metaObject, "createId", Long.class, userId);
        }
        
        // 填充创建人姓名（create_name）- 用于历史记录表
        if (metaObject.hasSetter("createName")) {
            String userName = CURRENT_USER_NAME.get();
            if (userName == null) {
                userName = "管理员";
            }
            this.strictInsertFill(metaObject, "createName", String.class, userName);
        }
    }
    
    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 填充修改人ID（update_id）
        if (metaObject.hasSetter("updateId")) {
            Long userId = CURRENT_USER_ID.get();
            if (userId == null) {
                userId = 1L; // 默认用户ID
            }
            this.strictUpdateFill(metaObject, "updateId", Long.class, userId);
        }
        
        // 填充修改人姓名（update_name）
        if (metaObject.hasSetter("updateName")) {
            String userName = CURRENT_USER_NAME.get();
            if (userName == null) {
                userName = "管理员";
            }
            this.strictUpdateFill(metaObject, "updateName", String.class, userName);
        }
        
        // 填充删除时间（软删除时）
        if (metaObject.hasSetter("deletedTime") && metaObject.getValue("deletedTime") != null) {
            this.strictUpdateFill(metaObject, "deletedTime", LocalDateTime.class, LocalDateTime.now());
        }
        
        // 填充删除人（软删除时）
        if (metaObject.hasSetter("deletedBy") && metaObject.getValue("deletedBy") != null) {
            Long userId = CURRENT_USER_ID.get();
            if (userId == null) {
                userId = 1L;
            }
            this.strictUpdateFill(metaObject, "deletedBy", Long.class, userId);
        }
    }
}
