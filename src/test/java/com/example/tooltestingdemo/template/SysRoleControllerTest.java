package com.example.tooltestingdemo.template;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.controller.SysRoleController;
import com.example.tooltestingdemo.dto.SysRoleDTO;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysRoleService;
import com.example.tooltestingdemo.service.SysUserService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysRoleController.class)
@DisplayName("角色管理控制器测试")
class SysRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysRoleService roleService;

    @MockBean
    private SysUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysRole testRole1;
    private SysRole testRole2;
    private SysRole adminRole;
    private SysRoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        testRole1 = new SysRole();
        testRole1.setId("role_001");
        testRole1.setName("测试角色1");
        testRole1.setDescription("这是一个测试角色");
        testRole1.setType("SYSTEM");
        testRole1.setScopeId(null);
        testRole1.setStatus(1);
        testRole1.setCreateTime(LocalDateTime.now().minusDays(10));
        testRole1.setUpdateTime(LocalDateTime.now());

        testRole2 = new SysRole();
        testRole2.setId("role_002");
        testRole2.setName("测试角色2");
        testRole2.setDescription("这是另一个测试角色");
        testRole2.setType("CUSTOM");
        testRole2.setScopeId("scope_001");
        testRole2.setStatus(1);
        testRole2.setCreateTime(LocalDateTime.now().minusDays(5));
        testRole2.setUpdateTime(LocalDateTime.now());

        adminRole = new SysRole();
        adminRole.setId("admin");
        adminRole.setName("管理员");
        adminRole.setDescription("系统管理员角色");
        adminRole.setType("SYSTEM");
        adminRole.setScopeId(null);
        adminRole.setStatus(1);
        adminRole.setCreateTime(LocalDateTime.now().minusDays(365));
        adminRole.setUpdateTime(LocalDateTime.now());

        roleDTO = new SysRoleDTO();
        roleDTO.setName("新角色");
        roleDTO.setDescription("新创建的角色");
        roleDTO.setType("CUSTOM");
        roleDTO.setStatus(1);
        roleDTO.setScopeId(null);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取所有角色列表 - 成功")
    void testGetAllRoles_Success() throws Exception {
        List<SysRole> roles = Arrays.asList(testRole1, testRole2);
        when(roleService.list()).thenReturn(roles);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色列表成功"))
                .andExpect(jsonPath("$.data[0].id").value("role_001"))
                .andExpect(jsonPath("$.data[1].id").value("role_002"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页获取角色列表 - 成功")
    void testGetRolesByPage_Success() throws Exception {
        Page<SysRole> rolePage = new Page<>(1, 10);
        rolePage.setRecords(Arrays.asList(testRole1, testRole2));
        rolePage.setTotal(2);

        when(roleService.page(any(Page.class))).thenReturn(rolePage);

        mockMvc.perform(get("/api/roles/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色列表成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("role_001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取角色信息 - 成功")
    void testGetRoleById_Success() throws Exception {
        when(roleService.getById("role_001")).thenReturn(testRole1);

        mockMvc.perform(get("/api/roles/role_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色信息成功"))
                .andExpect(jsonPath("$.data.id").value("role_001"))
                .andExpect(jsonPath("$.data.name").value("测试角色1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取角色信息 - 角色不存在")
    void testGetRoleById_NotFound() throws Exception {
        when(roleService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/roles/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据类型获取角色列表 - 成功")
    void testGetRolesByType_Success() throws Exception {
        Page<SysRole> rolePage = new Page<>(1, 10);
        rolePage.setRecords(Arrays.asList(testRole1));
        rolePage.setTotal(1);

        when(roleService.findByType(any(Page.class), eq("SYSTEM"))).thenReturn(rolePage);

        mockMvc.perform(get("/api/roles/type/SYSTEM")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色列表成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value("SYSTEM"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据用户ID获取角色列表 - 成功")
    void testGetRolesByUserId_Success() throws Exception {
        List<SysRole> roles = Arrays.asList(testRole1, testRole2);
        when(roleService.findByUserId("user_001")).thenReturn(roles);

        mockMvc.perform(get("/api/roles/user/user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色列表成功"))
                .andExpect(jsonPath("$.data[0].id").value("role_001"))
                .andExpect(jsonPath("$.data[1].id").value("role_002"));
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("当前用户访问自己的角色列表 - 成功")
    void testCurrentUserAccessOwnRoles_Success() throws Exception {
        List<SysRole> roles = Arrays.asList(testRole1);
        when(roleService.findByUserId("user_001")).thenReturn(roles);

        mockMvc.perform(get("/api/roles/user/user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("非管理员访问其他用户角色列表 - 返回403")
    void testNonAdminAccessOtherUserRoles_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/roles/user/user_001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新角色 - 成功（自动生成ID）")
    void testCreateRole_Success_AutoGenerateId() throws Exception {
        when(roleService.existsByNameAndScope(eq("新角色"), isNull(), isNull())).thenReturn(false);
        when(roleService.save(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建角色成功"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新角色 - 成功（指定ID）")
    void testCreateRole_Success_SpecifiedId() throws Exception {
        SysRoleDTO dtoWithId = new SysRoleDTO();
        dtoWithId.setId("role_custom_001");
        dtoWithId.setName("自定义角色");
        dtoWithId.setDescription("自定义描述");
        dtoWithId.setType("CUSTOM");

        when(roleService.existsByNameAndScope(eq("自定义角色"), isNull(), isNull())).thenReturn(false);
        when(roleService.getById("role_custom_001")).thenReturn(null);
        when(roleService.save(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建角色成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新角色 - 角色名称已存在")
    void testCreateRole_NameExists() throws Exception {
        when(roleService.existsByNameAndScope(eq("新角色"), isNull(), isNull())).thenReturn(true);

        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色名称在当前作用域下已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新角色 - 角色编码已存在")
    void testCreateRole_IdExists() throws Exception {
        SysRoleDTO dtoWithId = new SysRoleDTO();
        dtoWithId.setId("role_001");
        dtoWithId.setName("重复编码角色");

        when(roleService.getById("role_001")).thenReturn(testRole1);

        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色编码已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新角色 - scopeId为空字符串时设为null")
    void testCreateRole_EmptyScopeId_SetToNull() throws Exception {
        SysRoleDTO dtoWithEmptyScope = new SysRoleDTO();
        dtoWithEmptyScope.setName("空作用域角色");
        dtoWithEmptyScope.setScopeId("");

        when(roleService.existsByNameAndScope(eq("空作用域角色"), isNull(), isNull())).thenReturn(false);
        when(roleService.save(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithEmptyScope)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色信息 - 成功（非admin角色）")
    void testUpdateRole_Success_NonAdmin() throws Exception {
        SysRoleDTO updateDTO = new SysRoleDTO();
        updateDTO.setName("更新后的角色");
        updateDTO.setDescription("更新后的描述");

        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(roleService.existsByNameAndScope(eq("更新后的角色"), isNull(), eq("role_001"))).thenReturn(false);
        when(roleService.updateRole(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(put("/api/roles/role_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新角色信息成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色信息 - admin角色不能修改")
    void testUpdateRole_AdminRoleCannotModify() throws Exception {
        SysRoleDTO updateDTO = new SysRoleDTO();
        updateDTO.setName("更新的管理员");

        mockMvc.perform(put("/api/roles/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能修改admin角色"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色信息 - 角色不存在")
    void testUpdateRole_NotFound() throws Exception {
        when(roleService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(put("/api/roles/nonexistent")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色信息 - 名称已存在")
    void testUpdateRole_NameExists() throws Exception {
        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(roleService.existsByNameAndScope(eq("测试角色1"), isNull(), eq("role_001"))).thenReturn(true);

        mockMvc.perform(put("/api/roles/role_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色名称在当前作用域下已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色信息 - 更新失败")
    void testUpdateRole_UpdateFailure() throws Exception {
        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(roleService.existsByNameAndScope(anyString(), isNull(), eq("role_001"))).thenReturn(false);
        when(roleService.updateRole(any(SysRole.class))).thenReturn(false);

        mockMvc.perform(put("/api/roles/role_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("更新角色信息失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除角色 - 成功")
    void testDeleteRole_Success() throws Exception {
        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(roleService.deleteRole("role_001")).thenReturn(true);

        mockMvc.perform(delete("/api/roles/role_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色删除成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除角色 - admin角色不能删除")
    void testDeleteRole_AdminRoleCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/roles/admin")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能删除admin角色"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除角色 - 角色不存在")
    void testDeleteRole_NotFound() throws Exception {
        when(roleService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(delete("/api/roles/nonexistent")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除角色 - 删除失败")
    void testDeleteRole_DeleteFailure() throws Exception {
        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(roleService.deleteRole("role_001")).thenReturn(false);

        mockMvc.perform(delete("/api/roles/role_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("删除角色失败"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为角色分配权限 - 成功")
    void testAssignPermissions_Success() throws Exception {
        List<String> permissionIds = Arrays.asList("perm_001", "perm_002");

        doNothing().when(roleService).assignPermissions(eq("role_001"), eq(permissionIds));

        mockMvc.perform(post("/api/roles/role_001/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("权限分配成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为角色分配权限 - admin角色不能分配")
    void testAssignPermissions_AdminRoleCannotAssign() throws Exception {
        List<String> permissionIds = Arrays.asList("perm_001");

        mockMvc.perform(post("/api/roles/admin/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能为admin角色分配权限"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为角色分配权限 - 包含admin权限")
    void testAssignPermissions_ContainsAdminPermission() throws Exception {
        List<String> permissionIds = Arrays.asList("perm_001", "admin_permission");

        mockMvc.perform(post("/api/roles/role_001/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能分配admin权限"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为角色分配用户 - 成功")
    void testAssignUsers_Success() throws Exception {
        List<String> userIds = Arrays.asList("user_001", "user_002");

        doNothing().when(roleService).assignUsers(eq("role_001"), eq(userIds));

        mockMvc.perform(post("/api/roles/role_001/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户分配成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为角色分配用户 - admin角色不能分配")
    void testAssignUsers_AdminRoleCannotAssign() throws Exception {
        List<String> userIds = Arrays.asList("user_001");

        mockMvc.perform(post("/api/roles/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能为admin角色分配用户"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("从角色中移除权限 - 成功")
    void testRemovePermissions_Success() throws Exception {
        List<String> permissionIds = Arrays.asList("perm_001");

        doNothing().when(roleService).removePermissions(eq("role_001"), eq(permissionIds));

        mockMvc.perform(delete("/api/roles/role_001/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("权限移除成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("从角色中移除权限 - admin角色不能移除")
    void testRemovePermissions_AdminRoleCannotRemove() throws Exception {
        List<String> permissionIds = Arrays.asList("perm_001");

        mockMvc.perform(delete("/api/roles/admin/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能从admin角色中移除权限"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("从角色中移除权限 - 包含admin权限")
    void testRemovePermissions_ContainsAdminPermission() throws Exception {
        List<String> permissionIds = Arrays.asList("admin_perm");

        mockMvc.perform(delete("/api/roles/role_001/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能操作admin权限"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("从角色中移除用户 - 成功")
    void testRemoveUsers_Success() throws Exception {
        List<String> userIds = Arrays.asList("user_001");

        doNothing().when(roleService).removeUsers(eq("role_001"), eq(userIds));

        mockMvc.perform(delete("/api/roles/role_001/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户移除成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("从角色中移除用户 - admin角色不能移除")
    void testRemoveUsers_AdminRoleCannotRemove() throws Exception {
        List<String> userIds = Arrays.asList("user_001");

        mockMvc.perform(delete("/api/roles/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能从admin角色中移除用户"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据状态获取角色列表 - 成功")
    void testGetRolesByStatus_Success() throws Exception {
        List<SysRole> roles = Arrays.asList(testRole1);
        when(roleService.findByStatus(1)).thenReturn(roles);

        mockMvc.perform(get("/api/roles/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色列表成功"))
                .andExpect(jsonPath("$.data[0].status").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色状态 - 启用成功")
    void testUpdateRoleStatus_Enable_Success() throws Exception {
        doNothing().when(roleService).updateRoleStatus("role_001", 1);

        mockMvc.perform(put("/api/roles/role_001/status")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色启用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色状态 - 禁用成功")
    void testUpdateRoleStatus_Disable_Success() throws Exception {
        doNothing().when(roleService).updateRoleStatus("role_001", 0);

        mockMvc.perform(put("/api/roles/role_001/status")
                        .with(csrf())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色禁用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色状态 - admin角色不能修改")
    void testUpdateRoleStatus_AdminRoleCannotModify() throws Exception {
        mockMvc.perform(put("/api/roles/admin/status")
                        .with(csrf())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能修改admin角色的状态"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新角色状态 - 非法状态值")
    void testUpdateRoleStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/roles/role_001/status")
                        .with(csrf())
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值必须是0（禁用）或1（启用）"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查角色名称是否存在 - 不存在")
    void testCheckNameExists_NotExists() throws Exception {
        when(roleService.existsByNameAndScope(eq("新角色"), isNull(), isNull())).thenReturn(false);

        mockMvc.perform(get("/api/roles/check-name")
                        .param("name", "新角色"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("检查角色名称成功"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查角色名称是否存在 - 已存在")
    void testCheckNameExists_Exists() throws Exception {
        when(roleService.existsByNameAndScope(eq("测试角色1"), isNull(), isNull())).thenReturn(true);

        mockMvc.perform(get("/api/roles/check-name")
                        .param("name", "测试角色1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("检查角色名称是否存在 - 带作用域和排除ID")
    void testCheckNameExists_WithScopeAndExcludeId() throws Exception {
        when(roleService.existsByNameAndScope(eq("测试角色1"), eq("scope_001"), eq("role_001"))).thenReturn(false);

        mockMvc.perform(get("/api/roles/check-name")
                        .param("name", "测试角色1")
                        .param("scopeId", "scope_001")
                        .param("excludeId", "role_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取角色的权限列表 - 成功")
    void testGetRolePermissions_Success() throws Exception {
        List<SysPermission> permissions = Arrays.asList(new SysPermission(), new SysPermission());
        when(roleService.getPermissionsByRoleId("role_001")).thenReturn(permissions);

        mockMvc.perform(get("/api/roles/role_001/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色权限列表成功"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量更新角色状态 - 批量启用成功")
    void testBatchUpdateRoleStatus_Enable_Success() throws Exception {
        List<String> roleIds = Arrays.asList("role_001", "role_002");

        doNothing().when(roleService).batchUpdateRoleStatus(eq(roleIds), eq(1));

        mockMvc.perform(post("/api/roles/batch/status")
                        .with(csrf())
                        .param("roleIds", "role_001", "role_002")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色批量启用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量更新角色状态 - 批量禁用成功")
    void testBatchUpdateRoleStatus_Disable_Success() throws Exception {
        List<String> roleIds = Arrays.asList("role_001", "role_002");

        doNothing().when(roleService).batchUpdateRoleStatus(eq(roleIds), eq(0));

        mockMvc.perform(post("/api/roles/batch/status")
                        .with(csrf())
                        .param("roleIds", "role_001", "role_002")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色批量禁用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("批量更新角色状态 - 非法状态值")
    void testBatchUpdateRoleStatus_InvalidStatus() throws Exception {
        mockMvc.perform(post("/api/roles/batch/status")
                        .with(csrf())
                        .param("roleIds", "role_001")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值必须是0（禁用）或1（启用）"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("查询角色关联的用户列表 - 成功")
    void testGetRoleUsers_Success() throws Exception {
        List<SysUser> users = Arrays.asList(new SysUser(), new SysUser());
        when(roleService.getById("role_001")).thenReturn(testRole1);
        when(userService.findByRoleId("role_001")).thenReturn(users);

        mockMvc.perform(get("/api/roles/role_001/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取角色关联用户列表成功"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("查询角色关联的用户列表 - 角色不存在")
    void testGetRoleUsers_RoleNotFound() throws Exception {
        when(roleService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/roles/nonexistent/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @WithMockUser
    @DisplayName("无权限访问 - 返回403")
    void testNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("普通用户无权限访问 - 返回403")
    void testRegularUserNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页参数边界值 - 第一页")
    void testPagination_FirstPage_Success() throws Exception {
        Page<SysRole> rolePage = new Page<>(1, 10);
        rolePage.setRecords(Arrays.asList(testRole1));
        rolePage.setTotal(1);

        when(roleService.page(any(Page.class))).thenReturn(rolePage);

        mockMvc.perform(get("/api/roles/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("空结果集 - 返回空列表")
    void testEmptyResult_ReturnsEmptyList() throws Exception {
        when(roleService.list()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}