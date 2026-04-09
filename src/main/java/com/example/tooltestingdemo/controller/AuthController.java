package com.example.tooltestingdemo.controller;

import com.example.tooltestingdemo.entity.SysLoginLog;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.SysUserOrganization;
import com.example.tooltestingdemo.mapper.SysUserOrganizationMapper;
import com.example.tooltestingdemo.security.JwtUtil;
import com.example.tooltestingdemo.service.SysLoginLogService;
import com.example.tooltestingdemo.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SysUserOrganizationMapper userOrganizationMapper;
    private final SysLoginLogService loginLogService;


    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // 创建登录日志
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(loginRequest.getUsername());
        loginLog.setIpAddress(getClientIP(request));
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        loginLog.setLoginType("LOCAL");

        // 先检查用户是否存在
        SysUser user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            loginLog.setStatus(0);
            loginLog.setErrorMessage("用户不存在: " + loginRequest.getUsername());
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在: " + loginRequest.getUsername());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查用户是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLog.setUserId(user.getId());
            loginLog.setStatus(0);
            loginLog.setErrorMessage("用户已被禁用: " + loginRequest.getUsername());
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户已被禁用: " + loginRequest.getUsername());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = jwtUtil.generateToken(loginRequest.getUsername());
            
            // 更新用户最后登录信息
            userService.updateLastLoginInfo(user.getId(), getClientIP(request));
            
            // 记录成功登录日志
            loginLog.setUserId(user.getId());
            loginLog.setStatus(1);
            loginLog.setErrorMessage(null);
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", new HashMap<String, Object>() {
                {
                    put("token", jwt);
                    put("username", loginRequest.getUsername());
                    put("expiresIn", jwtUtil.getExpirationDateFromToken(jwt));
                }
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 记录失败登录日志
            loginLog.setUserId(user.getId());
            loginLog.setStatus(0);
            loginLog.setErrorMessage("用户名或密码错误");
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "用户名已存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        if (userService.existsByEmail(registerRequest.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "邮箱已存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        SysUser user = new SysUser();
        user.setId(java.util.UUID.randomUUID().toString().replace("-", "_"));
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRealName(registerRequest.getRealName());
        user.setOrganizationId(registerRequest.getOrganizationId());
        user.setStatus(registerRequest.getStatus()); // 使用请求中的状态，默认待激活
        
        SysUser savedUser = userService.save(user);
        
        // 处理部门关联
        if (registerRequest.getOrganizationId() != null && !registerRequest.getOrganizationId().isEmpty()) {
            SysUserOrganization userOrganization = new SysUserOrganization();
            userOrganization.setId(java.util.UUID.randomUUID().toString().replace("-", "_"));
            userOrganization.setUserId(savedUser.getId());
            userOrganization.setOrgId(registerRequest.getOrganizationId());
            userOrganization.setIsPrimary(1); // 设置为主要部门
            userOrganization.setCreateTime(LocalDateTime.now());
            userOrganizationMapper.insert(userOrganization);
        }
        
        Map<String, Object> response = new HashMap<>();
        String statusText = savedUser.getStatus() == 1 ? "已启用" : "待激活";
        response.put("code", 200);
        response.put("message", "注册成功，账户状态为：" + statusText);
        response.put("data", new HashMap<String, Object>() {
            {
                put("userId", savedUser.getId());
                put("status", statusText);
            }
        });
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        String username = authentication.getName();
        SysUser user = userService.findByUsername(username);
        
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 隐藏密码信息
        user.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String realName;
        private String organizationId;
        private Integer status = 0; // 0-待激活，1-已启用，默认待激活
    }
}