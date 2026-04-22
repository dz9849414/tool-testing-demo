package com.example.tooltestingdemo.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.tooltestingdemo.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private final SecurityService securityService;

    public MyMetaObjectHandler(SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        LocalDateTime currentTime = LocalDateTime.now();
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, currentTime);

        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, currentTime);

        // 填充创建人ID（create_id）
        if (metaObject.hasSetter("createId")) {
            this.strictInsertFill(metaObject, "createId", Long.class, getUserId());
        }

        // 填充创建人姓名（create_name）
        if (metaObject.hasSetter("createName")) {
            this.strictInsertFill(metaObject, "createName", String.class, getUsername());
        }

        if (metaObject.hasSetter("isDeleted")) {
            this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        }
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        LocalDateTime currentTIme = LocalDateTime.now();
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, currentTIme);

        // 填充修改人ID（update_id）
        if (metaObject.hasSetter("updateId")) {
            this.strictUpdateFill(metaObject, "updateId", Long.class, getUserId());
        }

        // 填充修改人姓名（update_name）
        if (metaObject.hasSetter("updateName")) {
            this.strictUpdateFill(metaObject, "updateName", String.class, getUsername());
        }
    }

    private Long getUserId() {
        String currentUserId = securityService.getCurrentUserId();
        if (StringUtils.isNumeric(currentUserId)) {
            return Long.valueOf(currentUserId);
        }
        return 1L;
    }

    private String getUsername() {
        String username = securityService.getCurrentUsername();
        return StringUtils.isNotBlank(username) ? username : "管理员";
    }

}
