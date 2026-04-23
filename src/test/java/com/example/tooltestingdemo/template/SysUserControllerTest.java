package com.example.tooltestingdemo.template;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.controller.SysUserController;
import com.example.tooltestingdemo.dto.SysUserCreateDTO;
import com.example.tooltestingdemo.dto.SysUserUpdateDTO;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysUserController.class)
@DisplayName("用户管理控制器测试")
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysUser testUser1;
    private SysUser testUser2;
    private SysUser adminUser;
    private SysUserCreateDTO createUserDTO;
    private SysUserUpdateDTO updateUserDTO;

    @BeforeEach
    void setUp() {
        testUser1 = new SysUser();
        testUser1.setId(Long.valueOf("user_001"));
        testUser1.setUsername("zhangsan");
        testUser1.setPassword("$2a$10$encoded_password");
        testUser1.setEmail("zhangsan@example.com");
        testUser1.setPhone("13800138001");
        testUser1.setRealName("张三");
        testUser1.setStatus(1);
        testUser1.setSource("LOCAL");
        testUser1.setOrganizationId("org_001");
        testUser1.setCreateTime(LocalDateTime.now().minusDays(30));
        testUser1.setUpdateTime(LocalDateTime.now());

        testUser2 = new SysUser();
        testUser2.setId(Long.valueOf("user_002"));
        testUser2.setUsername("lisi");
        testUser2.setPassword("$2a$10$encoded_password");
        testUser2.setEmail("lisi@example.com");
        testUser2.setPhone("13800138002");
        testUser2.setRealName("李四");
        testUser2.setStatus(1);
        testUser2.setSource("LOCAL");
        testUser2.setOrganizationId("org_001");
        testUser2.setCreateTime(LocalDateTime.now().minusDays(20));
        testUser2.setUpdateTime(LocalDateTime.now());

        adminUser = new SysUser();
        adminUser.setId(Long.valueOf("admin"));
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$encoded_admin_password");
        adminUser.setEmail("admin@example.com");
        adminUser.setRealName("管理员");
        adminUser.setStatus(1);
        adminUser.setSource("LOCAL");
        adminUser.setCreateTime(LocalDateTime.now().minusDays(365));
        adminUser.setUpdateTime(LocalDateTime.now());

        createUserDTO = new SysUserCreateDTO();
        createUserDTO.setUsername("newuser");
        createUserDTO.setPassword("password123");
        createUserDTO.setEmail("newuser@example.com");
        createUserDTO.setRealName("新用户");
        createUserDTO.setOrganizationId("org_001");
        createUserDTO.setStatus(1);

        updateUserDTO = new SysUserUpdateDTO();
        updateUserDTO.setEmail("updated@example.com");
        updateUserDTO.setPhone("13900139000");
        updateUserDTO.setRealName("更新后的姓名");
    }

    @Test
    @WithMockUser(username = "zhangsan")
    @DisplayName("获取当前用户信息 - 成功")
    void testGetCurrentUser_Success() throws Exception {
        when(userService.findByUsername("zhangsan")).thenReturn(testUser1);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取成功"))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.realName").value("张三"));
    }

    @Test
    @WithMockUser(username = "nonexistent")
    @DisplayName("获取当前用户信息 - 用户不存在")
    void testGetCurrentUser_UserNotFound() throws Exception {
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取所有用户列表 - 成功")
    void testGetAllUsers_Success() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList(testUser1, testUser2));
        userPage.setTotal(2);

        when(userService.findAll(any(Page.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户列表成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].username").value("zhangsan"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页获取用户列表 - 成功")
    void testGetUsersByPage_Success() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList(testUser1));
        userPage.setTotal(1);

        when(userService.findAll(any(Page.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户列表成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取用户信息 - 成功")
    void testGetUserById_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);

        mockMvc.perform(get("/api/users/user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.email").value("zhangsan@example.com"));
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("当前用户访问自己的信息 - 成功")
    void testCurrentUserAccessOwnInfo_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);

        mockMvc.perform(get("/api/users/user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据ID获取用户信息 - 用户不存在")
    void testGetUserById_NotFound() throws Exception {
        when(userService.findById(Long.valueOf("nonexistent"))).thenReturn(null);

        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新用户 - 成功")
    void testCreateUser_Success() throws Exception {
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.save(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(Long.valueOf("user_new_001"));
            return user;
        });

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建用户成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新用户 - 不能创建admin用户")
    void testCreateUser_CannotCreateAdmin() throws Exception {
        SysUserCreateDTO adminDTO = new SysUserCreateDTO();
        adminDTO.setUsername("admin");
        adminDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能创建用户名为admin的用户"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新用户 - 用户名已存在")
    void testCreateUser_UsernameExists() throws Exception {
        when(userService.existsByUsername("newuser")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("创建新用户 - 邮箱已存在")
    void testCreateUser_EmailExists() throws Exception {
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户信息 - 成功（非admin用户）")
    void testUpdateUser_Success_NonAdmin() throws Exception {
        when(userService.update(any(SysUser.class))).thenReturn(testUser1);

        mockMvc.perform(put("/api/users/user_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新用户信息成功"));
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("当前用户更新自己的信息 - 成功")
    void testUpdateUser_CurrentUser_Success() throws Exception {
        when(userService.update(any(SysUser.class))).thenReturn(testUser1);

        mockMvc.perform(put("/api/users/user_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户信息 - admin用户不能更改用户名")
    void testUpdateUser_AdminCannotChangeUsername() throws Exception {
        SysUserUpdateDTO updateDTO = new SysUserUpdateDTO();
        updateDTO.setUsername("newadmin");

        mockMvc.perform(put("/api/users/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能更改admin用户名"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户信息 - 不能将用户名改为admin")
    void testUpdateUser_CannotChangeToAdmin() throws Exception {
        SysUserUpdateDTO updateDTO = new SysUserUpdateDTO();
        updateDTO.setUsername("admin");

        mockMvc.perform(put("/api/users/user_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能将用户名改为admin"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户信息 - 邮箱已存在")
    void testUpdateUser_EmailExists() throws Exception {
        SysUser existingUser = new SysUser();
        existingUser.setId(Long.valueOf("user_002"));
        existingUser.setEmail("updated@example.com");

        when(userService.findByEmail("updated@example.com")).thenReturn(existingUser);

        mockMvc.perform(put("/api/users/user_001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户信息 - 用户不存在")
    void testUpdateUser_NotFound() throws Exception {
        when(userService.update(any(SysUser.class))).thenReturn(null);

        mockMvc.perform(put("/api/users/nonexistent")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除用户 - 成功")
    void testDeleteUser_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        doNothing().when(userService).deleteById(Long.valueOf("user_001"));

        mockMvc.perform(delete("/api/users/user_001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户删除成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("删除用户 - 用户不存在")
    void testDeleteUser_NotFound() throws Exception {
        when(userService.findById(Long.valueOf("nonexistent"))).thenReturn(null);

        mockMvc.perform(delete("/api/users/nonexistent")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据状态获取用户列表 - 成功")
    void testGetUsersByStatus_Success() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList(testUser1));
        userPage.setTotal(1);

        when(userService.findByStatus(any(Page.class), eq(1))).thenReturn(userPage);

        mockMvc.perform(get("/api/users/status/1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户列表成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("根据角色ID获取用户列表 - 成功")
    void testGetUsersByRoleId_Success() throws Exception {
        List<SysUser> users = Arrays.asList(testUser1, testUser2);
        when(userService.findByRoleId("role_001")).thenReturn(users);

        mockMvc.perform(get("/api/users/role/role_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取用户列表成功"))
                .andExpect(jsonPath("$.data[0].username").value("zhangsan"))
                .andExpect(jsonPath("$.data[1].username").value("lisi"));
    }

    @Test
    @DisplayName("检查用户名是否存在 - 不存在")
    void testCheckUsernameExists_NotExists() throws Exception {
        when(userService.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(get("/api/users/check-username")
                        .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("检查用户名成功"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @DisplayName("检查用户名是否存在 - 已存在")
    void testCheckUsernameExists_Exists() throws Exception {
        when(userService.existsByUsername("zhangsan")).thenReturn(true);

        mockMvc.perform(get("/api/users/check-username")
                        .param("username", "zhangsan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("检查邮箱是否存在 - 不存在")
    void testCheckEmailExists_NotExists() throws Exception {
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("检查邮箱成功"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @DisplayName("检查邮箱是否存在 - 已存在")
    void testCheckEmailExists_Exists() throws Exception {
        when(userService.existsByEmail("zhangsan@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "zhangsan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("搜索用户 - 成功")
    void testSearchUsers_Success() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList(testUser1));
        userPage.setTotal(1);

        when(userService.searchUsers(any(Page.class), eq("张"))).thenReturn(userPage);

        mockMvc.perform(get("/api/users/search")
                        .param("search", "张")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索用户成功"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审批用户注册 - 通过")
    void testApproveUser_Approve() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        doNothing().when(userService).updateUserStatusWithApproval(Long.valueOf(ArgumentMatchers.eq("user_001")), eq(1), Long.valueOf(ArgumentMatchers.eq("admin")));

        mockMvc.perform(put("/api/users/user_001/approve")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户审批通过"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审批用户注册 - 拒绝")
    void testApproveUser_Reject() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        doNothing().when(userService).updateUserStatusWithApproval(Long.valueOf(ArgumentMatchers.eq("user_001")), eq(0), Long.valueOf(ArgumentMatchers.eq("admin")));

        mockMvc.perform(put("/api/users/user_001/approve")
                        .with(csrf())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户审批拒绝"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审批用户注册 - 审批人不存在")
    void testApproveUser_ApproverNotFound() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(null);

        mockMvc.perform(put("/api/users/user_001/approve")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("审批人不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("审批用户注册 - 用户不存在")
    void testApproveUser_UserNotFound() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(userService.findById(Long.valueOf("nonexistent"))).thenReturn(null);

        mockMvc.perform(put("/api/users/nonexistent/approve")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("修改用户密码 - 成功")
    void testChangePassword_Success() throws Exception {
        when(userService.changePassword(Long.valueOf(ArgumentMatchers.eq("user_001")), eq("oldpass"), eq("newpass"))).thenReturn(true);

        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("oldPassword", "oldpass");
        passwordRequest.put("newPassword", "newpass");

        mockMvc.perform(put("/api/users/user_001/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));
    }

    @Test
    @WithMockUser(username = "user_001")
    @DisplayName("修改用户密码 - 旧密码错误")
    void testChangePassword_WrongOldPassword() throws Exception {
        when(userService.changePassword(Long.valueOf(ArgumentMatchers.eq("user_001")), eq("wrongpass"), eq("newpass"))).thenReturn(false);

        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("oldPassword", "wrongpass");
        passwordRequest.put("newPassword", "newpass");

        mockMvc.perform(put("/api/users/user_001/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("旧密码错误"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("获取用户权限列表 - 成功")
    void testGetUserPermissions_Success() throws Exception {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("用户管理", Arrays.asList("user:view", "user:edit"));
        permissions.put("系统管理", List.of("system:config"));

        when(userService.getPermissionsByUserIdGrouped(Long.valueOf("user_001"))).thenReturn(permissions);

        mockMvc.perform(get("/api/users/user_001/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取权限列表成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为用户分配角色 - 成功")
    void testAssignRoles_Success() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        List<String> roleIds = Arrays.asList("role_001", "role_002");
        doNothing().when(userService).assignRoles(Long.valueOf(ArgumentMatchers.eq("user_001")), eq(roleIds), Long.valueOf(ArgumentMatchers.eq("admin")));

        mockMvc.perform(post("/api/users/user_001/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("角色分配成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为用户分配角色 - 不能分配admin角色")
    void testAssignRoles_CannotAssignAdminRole() throws Exception {
        List<String> roleIds = Arrays.asList("role_001", "admin");

        mockMvc.perform(post("/api/users/user_001/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能分配admin角色"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("为用户分配角色 - 操作人不存在")
    void testAssignRoles_OperatorNotFound() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(null);
        List<String> roleIds = Arrays.asList("role_001");

        mockMvc.perform(post("/api/users/user_001/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("操作人不存在"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - 启用成功")
    void testUpdateUserStatus_Enable_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        when(userService.update(any(SysUser.class))).thenReturn(testUser1);

        mockMvc.perform(put("/api/users/user_001/status")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户启用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - 禁用成功")
    void testUpdateUserStatus_Disable_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        when(userService.update(any(SysUser.class))).thenReturn(testUser1);

        mockMvc.perform(put("/api/users/user_001/status")
                        .with(csrf())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户禁用成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - 锁定成功")
    void testUpdateUserStatus_Lock_Success() throws Exception {
        when(userService.findById(Long.valueOf("user_001"))).thenReturn(testUser1);
        when(userService.update(any(SysUser.class))).thenReturn(testUser1);

        mockMvc.perform(put("/api/users/user_001/status")
                        .with(csrf())
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户锁定成功"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - admin用户不能修改")
    void testUpdateUserStatus_AdminCannotModify() throws Exception {
        mockMvc.perform(put("/api/users/admin/status")
                        .with(csrf())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能修改admin用户"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - 非法状态值")
    void testUpdateUserStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/users/user_001/status")
                        .with(csrf())
                        .param("status", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值不合法，0-禁用，1-启用，2-锁定"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("更新用户状态 - 用户不存在")
    void testUpdateUserStatus_UserNotFound() throws Exception {
        when(userService.findById(Long.valueOf("nonexistent"))).thenReturn(null);

        mockMvc.perform(put("/api/users/nonexistent/status")
                        .with(csrf())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser
    @DisplayName("无权限访问 - 返回403")
    void testNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("普通用户无权限访问用户列表 - 返回403")
    void testRegularUserNoPermission_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("分页参数边界值 - 第一页")
    void testPagination_FirstPage_Success() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList(testUser1));
        userPage.setTotal(1);

        when(userService.findAll(any(Page.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("空结果集 - 返回空列表")
    void testEmptyResult_ReturnsEmptyList() throws Exception {
        Page<SysUser> userPage = new Page<>(1, 10);
        userPage.setRecords(Arrays.asList());
        userPage.setTotal(0);

        when(userService.findAll(any(Page.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
