package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.dto.SysDictionaryDTO;
import com.example.tooltestingdemo.entity.SysDictionary;
import com.example.tooltestingdemo.service.SysDictionaryService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysDictionaryController.class)
@DisplayName("数据字典控制器测试")
class SysDictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysDictionaryService dictionaryService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysDictionary testDictionary;
    private SysDictionaryDTO dictionaryDTO;

    @BeforeEach
    void setUp() {
        testDictionary = new SysDictionary();
        testDictionary.setId("dict_001");
        testDictionary.setType("user_status");
        testDictionary.setCode("active");
        testDictionary.setValue("1");
        testDictionary.setDescription("激活状态");
        testDictionary.setSort(1);
        testDictionary.setStatus(1);
        testDictionary.setCreateTime(LocalDateTime.now());
        testDictionary.setUpdateTime(LocalDateTime.now());

        dictionaryDTO = new SysDictionaryDTO();
        dictionaryDTO.setType("user_status");
        dictionaryDTO.setCode("inactive");
        dictionaryDTO.setValue("0");
        dictionaryDTO.setDescription("未激活状态");
        dictionaryDTO.setSort(2);
        dictionaryDTO.setStatus(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增数据字典 - 成功")
    void testCreateDictionary_Success() throws Exception {
        when(dictionaryService.checkCodeUnique("inactive", "user_status", null)).thenReturn(true);
        when(dictionaryService.saveDictionary(any(SysDictionary.class))).thenReturn(true);

        mockMvc.perform(post("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dictionaryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增数据字典成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增数据字典 - 字典已存在")
    void testCreateDictionary_AlreadyExists() throws Exception {
        when(dictionaryService.checkCodeUnique("inactive", "user_status", null)).thenReturn(false);

        mockMvc.perform(post("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dictionaryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("字典已存在，请勿重复添加"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("新增数据字典 - 保存失败")
    void testCreateDictionary_SaveFailure() throws Exception {
        when(dictionaryService.checkCodeUnique("inactive", "user_status", null)).thenReturn(true);
        when(dictionaryService.saveDictionary(any(SysDictionary.class))).thenReturn(false);

        mockMvc.perform(post("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dictionaryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("新增数据字典失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("编辑数据字典 - 成功")
    void testUpdateDictionary_Success() throws Exception {
        when(dictionaryService.updateDictionary(any(SysDictionary.class))).thenReturn(true);

        SysDictionaryDTO updateDTO = new SysDictionaryDTO();
        updateDTO.setCode("updated_code");
        updateDTO.setValue("updated_value");
        updateDTO.setDescription("更新后的描述");
        updateDTO.setSort(3);
        updateDTO.setStatus(1);

        mockMvc.perform(put("/api/dictionaries/dict_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("编辑数据字典成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("编辑数据字典 - 失败")
    void testUpdateDictionary_Failure() throws Exception {
        when(dictionaryService.updateDictionary(any(SysDictionary.class))).thenReturn(false);

        SysDictionaryDTO updateDTO = new SysDictionaryDTO();
        updateDTO.setValue("updated_value");

        mockMvc.perform(put("/api/dictionaries/dict_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("编辑数据字典失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除数据字典 - 成功")
    void testDeleteDictionary_Success() throws Exception {
        when(dictionaryService.deleteDictionary("dict_001")).thenReturn(true);

        mockMvc.perform(delete("/api/dictionaries/dict_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除数据字典成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除数据字典 - 失败")
    void testDeleteDictionary_Failure() throws Exception {
        when(dictionaryService.deleteDictionary("dict_001")).thenReturn(false);

        mockMvc.perform(delete("/api/dictionaries/dict_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除数据字典失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量删除数据字典 - 成功")
    void testDeleteDictionaries_Success() throws Exception {
        List<String> ids = Arrays.asList("dict_001", "dict_002", "dict_003");
        when(dictionaryService.deleteDictionaries(ids)).thenReturn(true);

        mockMvc.perform(delete("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量删除数据字典成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量删除数据字典 - 失败")
    void testDeleteDictionaries_Failure() throws Exception {
        List<String> ids = Arrays.asList("dict_001", "dict_002");
        when(dictionaryService.deleteDictionaries(ids)).thenReturn(false);

        mockMvc.perform(delete("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("批量删除数据字典失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("启用/禁用数据字典 - 成功")
    void testToggleStatus_Success() throws Exception {
        when(dictionaryService.toggleStatus("dict_001")).thenReturn(true);

        mockMvc.perform(put("/api/dictionaries/dict_001/status")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("切换数据字典状态成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("启用/禁用数据字典 - 失败")
    void testToggleStatus_Failure() throws Exception {
        when(dictionaryService.toggleStatus("dict_001")).thenReturn(false);

        mockMvc.perform(put("/api/dictionaries/dict_001/status")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("切换数据字典状态失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查字典键唯一性 - 唯一（新增时）")
    void testCheckCodeUnique_IsUnique_New() throws Exception {
        when(dictionaryService.checkCodeUnique("new_code", "user_status", null)).thenReturn(true);

        mockMvc.perform(get("/api/dictionaries/check-code")
                        .param("code", "new_code")
                        .param("type", "user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("检查字典键唯一性成功"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查字典键唯一性 - 不唯一（新增时）")
    void testCheckCodeUnique_NotUnique_New() throws Exception {
        when(dictionaryService.checkCodeUnique("existing_code", "user_status", null)).thenReturn(false);

        mockMvc.perform(get("/api/dictionaries/check-code")
                        .param("code", "existing_code")
                        .param("type", "user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查字典键唯一性 - 编辑时排除自身")
    void testCheckCodeUnique_ExcludeSelf() throws Exception {
        when(dictionaryService.checkCodeUnique("active", "user_status", "dict_001")).thenReturn(true);

        mockMvc.perform(get("/api/dictionaries/check-code")
                        .param("code", "active")
                        .param("type", "user_status")
                        .param("id", "dict_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询数据字典 - 成功（无筛选条件）")
    void testGetDictionaries_NoFilters() throws Exception {
        Page<SysDictionary> dictPage = new Page<>(1, 10);
        dictPage.setRecords(Arrays.asList(testDictionary));
        dictPage.setTotal(1);

        when(dictionaryService.getDictionariesByPage(any(Page.class), isNull(), isNull(), isNull())).thenReturn(dictPage);

        mockMvc.perform(get("/api/dictionaries")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询数据字典成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].code").value("active"))
                .andExpect(jsonPath("$.data.records[0].type").value("user_status"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询数据字典 - 成功（带类型筛选）")
    void testGetDictionaries_WithTypeFilter() throws Exception {
        Page<SysDictionary> dictPage = new Page<>(1, 10);
        dictPage.setRecords(Arrays.asList(testDictionary));
        dictPage.setTotal(1);

        when(dictionaryService.getDictionariesByPage(any(Page.class), eq("user_status"), isNull(), isNull())).thenReturn(dictPage);

        mockMvc.perform(get("/api/dictionaries")
                        .param("page", "1")
                        .param("size", "10")
                        .param("type", "user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询数据字典成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询数据字典 - 成功（带关键词搜索）")
    void testGetDictionaries_WithKeyword() throws Exception {
        Page<SysDictionary> dictPage = new Page<>(1, 10);
        dictPage.setRecords(Arrays.asList(testDictionary));
        dictPage.setTotal(1);

        when(dictionaryService.getDictionariesByPage(any(Page.class), isNull(), eq("激活"), isNull())).thenReturn(dictPage);

        mockMvc.perform(get("/api/dictionaries")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "激活"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询数据字典 - 成功（带状态筛选）")
    void testGetDictionaries_WithStatusFilter() throws Exception {
        Page<SysDictionary> dictPage = new Page<>(1, 10);
        dictPage.setRecords(Arrays.asList(testDictionary));
        dictPage.setTotal(1);

        when(dictionaryService.getDictionariesByPage(any(Page.class), isNull(), isNull(), eq(1))).thenReturn(dictPage);

        mockMvc.perform(get("/api/dictionaries")
                        .param("page", "1")
                        .param("size", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页查询数据字典 - 成功（多条件组合筛选）")
    void testGetDictionaries_MultipleFilters() throws Exception {
        Page<SysDictionary> dictPage = new Page<>(1, 10);
        dictPage.setRecords(Arrays.asList(testDictionary));
        dictPage.setTotal(1);

        when(dictionaryService.getDictionariesByPage(any(Page.class), eq("user_status"), eq("激活"), eq(1))).thenReturn(dictPage);

        mockMvc.perform(get("/api/dictionaries")
                        .param("page", "1")
                        .param("size", "10")
                        .param("type", "user_status")
                        .param("keyword", "激活")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据类型查询数据字典 - 成功")
    void testGetDictionariesByType_Success() throws Exception {
        List<SysDictionary> dictionaries = Arrays.asList(testDictionary);
        when(dictionaryService.getDictionariesByType("user_status")).thenReturn(dictionaries);

        mockMvc.perform(get("/api/dictionaries/type/user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("根据类型查询数据字典成功"))
                .andExpect(jsonPath("$.data[0].code").value("active"))
                .andExpect(jsonPath("$.data[0].value").value("1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据类型查询数据字典 - 返回空列表")
    void testGetDictionariesByType_EmptyList() throws Exception {
        when(dictionaryService.getDictionariesByType("nonexistent_type")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/dictionaries/type/nonexistent_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID查询数据字典详情 - 成功")
    void testGetDictionaryById_Success() throws Exception {
        when(dictionaryService.getById("dict_001")).thenReturn(testDictionary);

        mockMvc.perform(get("/api/dictionaries/dict_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取数据字典详情成功"))
                .andExpect(jsonPath("$.data.id").value("dict_001"))
                .andExpect(jsonPath("$.data.code").value("active"))
                .andExpect(jsonPath("$.data.type").value("user_status"))
                .andExpect(jsonPath("$.data.value").value("1"))
                .andExpect(jsonPath("$.data.description").value("激活状态"))
                .andExpect(jsonPath("$.data.sort").value(1))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID查询数据字典详情 - 字典不存在")
    void testGetDictionaryById_NotFound() throws Exception {
        when(dictionaryService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/dictionaries/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("数据字典不存在"));
    }

    @Test
    @WithMockUser
    @DisplayName("无权限访问 - 返回403")
    void testNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/dictionaries"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("普通用户无权限 - 返回403")
    void testRegularUser_NoPermission() throws Exception {
        mockMvc.perform(post("/api/dictionaries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dictionaryDTO)))
                .andExpect(status().isForbidden());
    }
}
