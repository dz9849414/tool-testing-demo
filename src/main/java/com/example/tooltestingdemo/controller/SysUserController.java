package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {
    
    private final SysUserService userService;
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysUser>> getAllUsers() {
        List<SysUser> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SysUser>> getUsersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findAll(pageParam);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<SysUser> getUserById(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody SysUser user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        
        if (user.getEmail() != null && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("邮箱已存在");
        }
        
        SysUser savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody SysUser user) {
        user.setId(id);
        SysUser updatedUser = userService.update(user);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "用户删除成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据状态获取用户列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysUser>> getUsersByStatus(@PathVariable Integer status) {
        List<SysUser> users = userService.findByStatus(status);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据角色ID获取用户列表
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysUser>> getUsersByRoleId(@PathVariable String roleId) {
        List<SysUser> users = userService.findByRoleId(roleId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}