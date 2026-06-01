package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 操作日志实体类
 */
@TableName("pdm_tool_sys_operation_log")
@Data
@Slf4j
public class SysOperationLog {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "trace_id")
    private String traceId;
    
    @TableField(value = "user_id")
    private String userId;
    
    @TableField(value = "username")
    private String username;
    
    @TableField(value = "role_id")
    private String roleId;
    
    @TableField(value = "operation")
    private String operation;
    
    @TableField(value = "module")
    private String module;
    
    @TableField(value = "method")
    private String method;
    
    /**
     * 方法调用链JSON（Gzip压缩存储）
     */
    @TableField(value = "method_json")
    private byte[] methodJson;
    
    @TableField(value = "request_url")
    private String requestUrl;
    
    @TableField(value = "request_params")
    private String requestParams;
    
    @TableField(value = "ip_address")
    private String ipAddress;
    
    @TableField(value = "user_agent")
    private String userAgent;
    
    @TableField(value = "status")
    private Integer status = 1;
    
    @TableField(value = "error_message")
    private String errorMessage;
    
    @TableField(value = "execute_time")
    private Long executeTime;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "show_in_system_log")
    private Boolean showInSystemLog = true;

    @TableField(value = "generation_log_id")
    private Long generationLogId;

    /**
     * 设置方法调用链JSON字符串（自动压缩）
     */
    public void setMethodJsonString(String json) {
        if (json == null) {
            this.methodJson = null;
            return;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(json.getBytes(StandardCharsets.UTF_8));
            gzos.finish();
            this.methodJson = baos.toByteArray();
            log.debug("method_json压缩完成: 原始大小={}, 压缩后大小={}", json.length(), this.methodJson.length);
        } catch (IOException e) {
            log.error("压缩method_json失败，降级存储原始数据", e);
            this.methodJson = json.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取方法调用链JSON字符串（自动解压）
     */
    public String getMethodJsonString() {
        if (methodJson == null) return null;
        try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(methodJson))) {
            return new String(gzis.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 解压失败，尝试作为原始字符串读取（兼容旧数据或压缩失败的情况）
            log.debug("解压method_json失败，尝试作为原始字符串读取: {}", e.getMessage());
            return new String(methodJson, StandardCharsets.UTF_8);
        }
    }
}