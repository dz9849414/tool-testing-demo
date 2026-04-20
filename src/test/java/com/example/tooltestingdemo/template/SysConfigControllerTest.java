package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.dto.SysConfigDTO;
import com.example.tooltestingdemo.entity.SysConfig;
import com.example.tooltestingdemo.service.SysConfigService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysConfigController.class)
@DisplayName("系统配置控制器测试")
class SysConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysConfigService configService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysConfig testConfig;
    private SysConfigDTO configDTO;

    @BeforeEach
    void setUp() {
        testConfig = new SysConfig();
        testConfig.setId("config_001");
        testConfig.setConfigKey("test.key");
        testConfig.setConfigValue("test_value");
        testConfig.setConfigName("测试配置");
        testConfig.setDescription("这是一个测试配置");
        testConfig.setType("TEXT");
        testConfig.setIsEncrypted(0);
        testConfig.setStatus(1);
        testConfig.setIsBuiltIn(0);
        testConfig.setUpdateTime(LocalDateTime.now());
        testConfig.setCreateTime(LocalDateTime.now());
        testConfig.setUpdateUser("admin");

        configDTO = new SysConfigDTO();
        configDTO.setConfigKey("new.key");
        configDTO.setConfigValue("new_value");
        configDTO.setConfigName("新配置");
        configDTO.setDescription("这是一个新配置");
        configDTO.setType("TEXT");
        configDTO.setIsEncrypted(0);
        configDTO.setStatus(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增系统配置 - 成功")
    void testCreateConfig_Success() throws Exception {
        when(configService.existsByConfigKey("new.key", null)).thenReturn(false);
        when(configService.saveConfig(any(SysConfig.class))).thenReturn(true);

        mockMvc.perform(post("/api/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增配置成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增系统配置 - 配置键已存在")
    void testCreateConfig_KeyExists() throws Exception {
        when(configService.existsByConfigKey("new.key", null)).thenReturn(true);

        mockMvc.perform(post("/api/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("配置键已存在，请勿重复添加"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("编辑系统配置 - 成功（非内置配置）")
    void testUpdateConfig_NonBuiltIn_Success() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);
        when(configService.updateConfig(any(SysConfig.class))).thenReturn(true);

        SysConfigDTO updateDTO = new SysConfigDTO();
        updateDTO.setConfigKey("updated.key");
        updateDTO.setConfigValue("updated_value");
        updateDTO.setDescription("更新后的描述");

        mockMvc.perform(put("/api/configs/config_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("编辑配置成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("编辑系统配置 - 成功（内置配置保留原key）")
    void testUpdateConfig_BuiltIn_Success() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(true);
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.updateConfig(any(SysConfig.class))).thenReturn(true);

        SysConfigDTO updateDTO = new SysConfigDTO();
        updateDTO.setConfigKey("should.be.ignored");
        updateDTO.setConfigValue("updated_value");
        updateDTO.setDescription("更新后的描述");

        mockMvc.perform(put("/api/configs/config_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("编辑配置成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("编辑系统配置 - 失败")
    void testUpdateConfig_Failure() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);
        when(configService.updateConfig(any(SysConfig.class))).thenReturn(false);

        SysConfigDTO updateDTO = new SysConfigDTO();
        updateDTO.setConfigValue("updated_value");

        mockMvc.perform(put("/api/configs/config_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("编辑配置失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除系统配置 - 成功")
    void testDeleteConfig_Success() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);
        when(configService.deleteConfig("config_001")).thenReturn(true);

        mockMvc.perform(delete("/api/configs/config_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除配置成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除系统配置 - 内置配置不能删除")
    void testDeleteConfig_BuiltInCannotDelete() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(true);

        mockMvc.perform(delete("/api/configs/config_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("内置配置不能删除"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除系统配置 - 删除失败")
    void testDeleteConfig_Failure() throws Exception {
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);
        when(configService.deleteConfig("config_001")).thenReturn(false);

        mockMvc.perform(delete("/api/configs/config_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除配置失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询系统配置 - 成功（无筛选条件）")
    void testGetConfigs_NoFilters() throws Exception {
        Page<SysConfig> configPage = new Page<>(1, 10);
        configPage.setRecords(Arrays.asList(testConfig));
        configPage.setTotal(1);

        when(configService.getConfigsByPage(any(Page.class), isNull(), isNull())).thenReturn(configPage);

        mockMvc.perform(get("/api/configs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询配置成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].configKey").value("test.key"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询系统配置 - 成功（带筛选条件）")
    void testGetConfigs_WithFilters() throws Exception {
        Page<SysConfig> configPage = new Page<>(1, 10);
        configPage.setRecords(Arrays.asList(testConfig));
        configPage.setTotal(1);

        when(configService.getConfigsByPage(any(Page.class), eq("test.key"), eq("1"))).thenReturn(configPage);

        mockMvc.perform(get("/api/configs")
                        .param("page", "1")
                        .param("size", "10")
                        .param("configKey", "test.key")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询配置成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取系统配置 - 成功")
    void testGetConfigById_Success() throws Exception {
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);

        mockMvc.perform(get("/api/configs/config_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取配置成功"))
                .andExpect(jsonPath("$.data.configKey").value("test.key"))
                .andExpect(jsonPath("$.data.configValue").value("test_value"))
                .andExpect(jsonPath("$.data.isBuiltIn").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取系统配置 - 配置不存在")
    void testGetConfigById_NotFound() throws Exception {
        when(configService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/configs/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("配置不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取所有系统配置 - 成功")
    void testGetAllConfigs_Success() throws Exception {
        List<SysConfig> configs = Arrays.asList(testConfig);
        when(configService.getAllConfigs()).thenReturn(configs);

        mockMvc.perform(get("/api/configs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取所有配置成功"))
                .andExpect(jsonPath("$.data[0].configKey").value("test.key"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查配置键是否存在 - 唯一")
    void testCheckKeyUnique_IsUnique() throws Exception {
        when(configService.existsByConfigKey("new.key", null)).thenReturn(false);

        mockMvc.perform(get("/api/configs/check-key")
                        .param("configKey", "new.key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("检查配置键成功"))
                .andExpect(jsonPath("$.data.unique").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查配置键是否存在 - 不唯一（新增时）")
    void testCheckKeyUnique_NotUnique_New() throws Exception {
        when(configService.existsByConfigKey("existing.key", null)).thenReturn(true);

        mockMvc.perform(get("/api/configs/check-key")
                        .param("configKey", "existing.key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unique").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查配置键是否存在 - 编辑时排除自身")
    void testCheckKeyUnique_ExcludeSelf() throws Exception {
        when(configService.existsByConfigKey("test.key", "config_001")).thenReturn(false);

        mockMvc.perform(get("/api/configs/check-key")
                        .param("configKey", "test.key")
                        .param("id", "config_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unique").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取配置详情 - 成功")
    void testGetConfigDetail_Success() throws Exception {
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.isBuiltInConfigById("config_001")).thenReturn(false);

        mockMvc.perform(get("/api/configs/detail/config_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取配置详情成功"))
                .andExpect(jsonPath("$.data.id").value("config_001"))
                .andExpect(jsonPath("$.data.configKey").value("test.key"))
                .andExpect(jsonPath("$.data.configValue").value("test_value"))
                .andExpect(jsonPath("$.data.configName").value("测试配置"))
                .andExpect(jsonPath("$.data.description").value("这是一个测试配置"))
                .andExpect(jsonPath("$.data.type").value("TEXT"))
                .andExpect(jsonPath("$.data.isEncrypted").value(0))
                .andExpect(jsonPath("$.data.isBuiltIn").value(false))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取配置详情 - 配置不存在")
    void testGetConfigDetail_NotFound() throws Exception {
        when(configService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/configs/detail/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("配置不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取配置详情 - 内置配置")
    void testGetConfigDetail_BuiltIn() throws Exception {
        testConfig.setIsBuiltIn(1);
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.isBuiltInConfigById("config_001")).thenReturn(true);

        mockMvc.perform(get("/api/configs/detail/config_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isBuiltIn").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改配置为内置 - 成功")
    void testUpdateBuiltInStatus_SetBuiltIn_Success() throws Exception {
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.updateBuiltInStatus("config_001", true)).thenReturn(true);

        Map<String, Boolean> request = new HashMap<>();
        request.put("isBuiltIn", true);

        mockMvc.perform(put("/api/configs/config_001/built-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("配置已设置为内置"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改配置为非内置 - 成功")
    void testUpdateBuiltInStatus_SetNonBuiltIn_Success() throws Exception {
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.updateBuiltInStatus("config_001", false)).thenReturn(true);

        Map<String, Boolean> request = new HashMap<>();
        request.put("isBuiltIn", false);

        mockMvc.perform(put("/api/configs/config_001/built-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("配置已设置为非内置"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改配置是否内置状态 - 参数为空")
    void testUpdateBuiltInStatus_MissingParameter() throws Exception {
        Map<String, Boolean> request = new HashMap<>();

        mockMvc.perform(put("/api/configs/config_001/built-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数isBuiltIn不能为空"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改配置是否内置状态 - 配置不存在")
    void testUpdateBuiltInStatus_ConfigNotFound() throws Exception {
        when(configService.getById("nonexistent")).thenReturn(null);

        Map<String, Boolean> request = new HashMap<>();
        request.put("isBuiltIn", true);

        mockMvc.perform(put("/api/configs/nonexistent/built-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("配置不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("修改配置是否内置状态 - 更新失败")
    void testUpdateBuiltInStatus_UpdateFailure() throws Exception {
        when(configService.getById("config_001")).thenReturn(testConfig);
        when(configService.updateBuiltInStatus("config_001", true)).thenReturn(false);

        Map<String, Boolean> request = new HashMap<>();
        request.put("isBuiltIn", true);

        mockMvc.perform(put("/api/configs/config_001/built-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("更新内置状态失败"));
    }

    @Test
    @WithMockUser
    @DisplayName("无权限访问 - 返回403")
    void testNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/configs"))
                .andExpect(status().isForbidden());
    }
}
