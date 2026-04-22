package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysOperationLogController.class)
@DisplayName("操作日志控制器测试")
class SysOperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysOperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysOperationLog testLog1;
    private SysOperationLog testLog2;

    @BeforeEach
    void setUp() {
        testLog1 = new SysOperationLog();
        testLog1.setId("log_001");
        testLog1.setUserId("user_001");
        testLog1.setUsername("张三");
        testLog1.setRoleId("role_001");
        testLog1.setModule("用户管理");
        testLog1.setOperation("新增用户");
        testLog1.setMethod("POST");
        testLog1.setRequestUrl("/api/users");
        testLog1.setRequestParams("{\"name\":\"李四\"}");
        testLog1.setIpAddress("192.168.1.100");
        testLog1.setUserAgent("Mozilla/5.0");
        testLog1.setStatus(1);
        testLog1.setErrorMessage(null);
        testLog1.setExecuteTime(150L);
        testLog1.setCreateTime(LocalDateTime.now().minusHours(2));

        testLog2 = new SysOperationLog();
        testLog2.setId("log_002");
        testLog2.setUserId("user_002");
        testLog2.setUsername("王五");
        testLog2.setRoleId("role_002");
        testLog2.setModule("系统配置");
        testLog2.setOperation("修改配置");
        testLog2.setMethod("PUT");
        testLog2.setRequestUrl("/api/configs/config_001");
        testLog2.setRequestParams("{\"value\":\"new_value\"}");
        testLog2.setIpAddress("192.168.1.101");
        testLog2.setUserAgent("Mozilla/5.0");
        testLog2.setStatus(1);
        testLog2.setErrorMessage(null);
        testLog2.setExecuteTime(200L);
        testLog2.setCreateTime(LocalDateTime.now().minusHours(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取用户操作日志 - 成功（无时间范围）")
    void testGetUserOperationLogs_NoTimeRange_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1, testLog2));
        logPage.setTotal(2);

        when(operationLogService.getOperationLogsByUserIdAndTimeRange(any(Page.class), eq("user_001"), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/user/user_001")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户操作日志成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("log_001"))
                .andExpect(jsonPath("$.data.records[0].userId").value("user_001"))
                .andExpect(jsonPath("$.data.records[0].username").value("张三"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取用户操作日志 - 成功（带时间范围）")
    void testGetUserOperationLogs_WithTimeRange_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        String startTime = "2024-01-01 00:00:00";
        String endTime = "2024-12-31 23:59:59";

        when(operationLogService.getOperationLogsByUserIdAndTimeRange(any(Page.class), eq("user_001"), any(), any()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/user/user_001")
                        .param("page", "1")
                        .param("size", "10")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户操作日志成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取用户操作日志 - 开始时间格式错误")
    void testGetUserOperationLogs_InvalidStartTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/user/user_001")
                        .param("startTime", "invalid-date-format"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取用户操作日志 - 结束时间格式错误")
    void testGetUserOperationLogs_InvalidEndTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/user/user_001")
                        .param("endTime", "invalid-date-format"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页获取操作日志列表 - 成功（无筛选条件）")
    void testGetOperationLogsByPage_NoFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1, testLog2));
        logPage.setTotal(2);

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取操作日志列表成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("log_001"))
                .andExpect(jsonPath("$.data.records[1].id").value("log_002"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页获取操作日志列表 - 成功（带筛选条件）")
    void testGetOperationLogsByPage_WithFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        when(operationLogService.getOperationLogsByPage(any(Page.class), eq("user_001"), any(), any(), eq("用户管理")))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("page", "1")
                        .param("size", "10")
                        .param("userId", "user_001")
                        .param("startTime", "2024-01-01 00:00:00")
                        .param("endTime", "2024-12-31 23:59:59")
                        .param("module", "用户管理"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取操作日志列表成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页获取操作日志列表 - 时间格式错误")
    void testGetOperationLogsByPage_InvalidTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/page")
                        .param("startTime", "invalid-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取最近的操作日志 - 成功（默认数量）")
    void testGetRecentOperationLogs_DefaultLimit_Success() throws Exception {
        List<SysOperationLog> logs = Arrays.asList(testLog1, testLog2);

        when(operationLogService.getRecentOperationLogs(10)).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取最近操作日志成功"))
                .andExpect(jsonPath("$.data[0].id").value("log_001"))
                .andExpect(jsonPath("$.data[1].id").value("log_002"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取最近的操作日志 - 成功（自定义数量）")
    void testGetRecentOperationLogs_CustomLimit_Success() throws Exception {
        List<SysOperationLog> logs = Arrays.asList(testLog1);

        when(operationLogService.getRecentOperationLogs(5)).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/recent")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取最近操作日志成功"))
                .andExpect(jsonPath("$.data[0].id").value("log_001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据模块获取操作日志 - 成功")
    void testGetOperationLogsByModule_Success() throws Exception {
        List<SysOperationLog> logs = Arrays.asList(testLog1);

        when(operationLogService.getOperationLogsByModule("用户管理")).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/module")
                        .param("module", "用户管理"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取模块操作日志成功"))
                .andExpect(jsonPath("$.data[0].id").value("log_001"))
                .andExpect(jsonPath("$.data[0].module").value("用户管理"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据角色ID获取操作日志 - 成功（无筛选条件）")
    void testGetRoleOperationLogs_NoFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1, testLog2));
        logPage.setTotal(2);

        when(operationLogService.getOperationLogsByRoleIdAndPage(any(Page.class), eq("role_001"), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/role/role_001")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色操作日志成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].roleId").value("role_001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据角色ID获取操作日志 - 成功（带时间范围和模块筛选）")
    void testGetRoleOperationLogs_WithFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        when(operationLogService.getOperationLogsByRoleIdAndPage(any(Page.class), eq("role_001"), any(), any(), eq("用户管理")))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/role/role_001")
                        .param("page", "1")
                        .param("size", "10")
                        .param("startTime", "2024-01-01 00:00:00")
                        .param("endTime", "2024-12-31 23:59:59")
                        .param("module", "用户管理"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色操作日志成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据角色ID获取操作日志 - 开始时间格式错误")
    void testGetRoleOperationLogs_InvalidStartTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/role/role_001")
                        .param("startTime", "invalid-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据角色ID获取操作日志 - 结束时间格式错误")
    void testGetRoleOperationLogs_InvalidEndTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/role/role_001")
                        .param("endTime", "invalid-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("导出角色操作日志 - 成功（无筛选条件）")
    void testExportRoleOperationLogs_NoFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10000);
        logPage.setRecords(Arrays.asList(testLog1, testLog2));
        logPage.setTotal(2);

        when(operationLogService.getOperationLogsByRoleIdAndPage(any(Page.class), eq("role_001"), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/role/role_001/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("角色操作日志.xlsx")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("导出角色操作日志 - 成功（带筛选条件）")
    void testExportRoleOperationLogs_WithFilters_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10000);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        when(operationLogService.getOperationLogsByRoleIdAndPage(any(Page.class), eq("role_001"), any(), any(), eq("用户管理")))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/role/role_001/export")
                        .param("startTime", "2024-01-01 00:00:00")
                        .param("endTime", "2024-12-31 23:59:59")
                        .param("module", "用户管理"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("导出角色操作日志 - 开始时间格式错误")
    void testExportRoleOperationLogs_InvalidStartTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/role/role_001/export")
                        .param("startTime", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("导出角色操作日志 - 结束时间格式错误")
    void testExportRoleOperationLogs_InvalidEndTimeFormat() throws Exception {
        mockMvc.perform(get("/api/operation-logs/role/role_001/export")
                        .param("endTime", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @WithMockUser
    @DisplayName("无权限访问 - 返回403")
    void testNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/operation-logs/page"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("当前用户访问自己的操作日志 - 成功")
    void testCurrentUserAccessOwnLogs_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        when(operationLogService.getOperationLogsByUserIdAndTimeRange(any(Page.class), eq("user_001"), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/user/user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("非管理员访问其他用户操作日志 - 返回403")
    void testNonAdminAccessOtherUserLogs_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/operation-logs/user/user_001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页参数边界值 - 第一页")
    void testPagination_FirstPage_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页参数边界值 - 大页码")
    void testPagination_LargePageNumber_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(100, 10);
        logPage.setRecords(Arrays.asList());
        logPage.setTotal(0);

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("page", "100")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(100))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("空结果集 - 返回空列表")
    void testEmptyResult_ReturnsEmptyList() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList());
        logPage.setTotal(0);

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取最近日志 - 限制数量为1")
    void testGetRecentOperationLogs_LimitOne_Success() throws Exception {
        List<SysOperationLog> logs = Arrays.asList(testLog1);

        when(operationLogService.getRecentOperationLogs(1)).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/recent")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("模块查询 - 空模块名称")
    void testGetOperationLogsByModule_EmptyModule_Success() throws Exception {
        List<SysOperationLog> logs = Arrays.asList();

        when(operationLogService.getOperationLogsByModule("")).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/module")
                        .param("module", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("操作日志包含失败状态")
    void testOperationLogWithFailedStatus_Success() throws Exception {
        SysOperationLog failedLog = new SysOperationLog();
        failedLog.setId("log_003");
        failedLog.setUserId("user_001");
        failedLog.setUsername("张三");
        failedLog.setModule("系统配置");
        failedLog.setOperation("删除配置");
        failedLog.setStatus(0);
        failedLog.setErrorMessage("配置不存在");
        failedLog.setCreateTime(LocalDateTime.now());

        List<SysOperationLog> logs = Arrays.asList(failedLog);

        when(operationLogService.getRecentOperationLogs(10)).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(0))
                .andExpect(jsonPath("$.data[0].errorMessage").value("配置不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("操作日志包含长文本参数")
    void testOperationLogWithLongParams_Success() throws Exception {
        SysOperationLog logWithLongParams = new SysOperationLog();
        logWithLongParams.setId("log_004");
        logWithLongParams.setUserId("user_001");
        logWithLongParams.setUsername("张三");
        logWithLongParams.setModule("模板管理");
        logWithLongParams.setOperation("导入模板");
        logWithLongParams.setRequestParams("{\"template\":\"very long json string...\"}");
        logWithLongParams.setStatus(1);
        logWithLongParams.setCreateTime(LocalDateTime.now());

        List<SysOperationLog> logs = Arrays.asList(logWithLongParams);

        when(operationLogService.getRecentOperationLogs(10)).thenReturn(logs);

        mockMvc.perform(get("/api/operation-logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requestParams").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("多个用户操作日志混合查询")
    void testMultipleUsersOperationLogs_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1, testLog2));
        logPage.setTotal(2);

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].userId").value("user_001"))
                .andExpect(jsonPath("$.data.records[1].userId").value("user_002"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("时间范围查询 - 同一天")
    void testTimeRangeQuery_SameDay_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10);
        logPage.setRecords(Arrays.asList(testLog1));
        logPage.setTotal(1);

        String startTime = "2024-01-15 00:00:00";
        String endTime = "2024-01-15 23:59:59";

        when(operationLogService.getOperationLogsByPage(any(Page.class), isNull(), any(), any(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/page")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("导出操作日志 - 空结果集")
    void testExportRoleOperationLogs_EmptyResult_Success() throws Exception {
        Page<SysOperationLog> logPage = new Page<>(1, 10000);
        logPage.setRecords(Arrays.asList());
        logPage.setTotal(0);

        when(operationLogService.getOperationLogsByRoleIdAndPage(any(Page.class), eq("role_001"), isNull(), isNull(), isNull()))
                .thenReturn(logPage);

        mockMvc.perform(get("/api/operation-logs/role/role_001/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
