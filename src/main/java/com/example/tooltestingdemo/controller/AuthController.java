package com.example.tooltestingdemo.controller;

import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.SysUserOrganization;
import com.example.tooltestingdemo.mapper.SysUserOrganizationMapper;
import com.example.tooltestingdemo.security.JwtUtil;
import com.example.tooltestingdemo.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PasswordEncoder passwordEncoder;
    private final SysUserOrganizationMapper userOrganizationMapper;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = jwtUtil.generateToken(loginRequest.getUsername());
            
            // 更新用户最后登录信息
            SysUser user = userService.findByUsername(loginRequest.getUsername());
            if (user != null) {
                userService.updateLastLoginInfo(user.getId(), getClientIP(request));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", loginRequest.getUsername());
            response.put("expiresIn", jwtUtil.getExpirationDateFromToken(jwt));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("邮箱已存在");
        }
        
        SysUser user = new SysUser();
        user.setId(java.util.UUID.randomUUID().toString());
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
            userOrganization.setId(java.util.UUID.randomUUID().toString());
            userOrganization.setUserId(savedUser.getId());
            userOrganization.setOrgId(registerRequest.getOrganizationId());
            userOrganization.setIsPrimary(1); // 设置为主要部门
            userOrganization.setCreateTime(LocalDateTime.now());
            userOrganizationMapper.insert(userOrganization);
        }
        
        Map<String, Object> response = new HashMap<>();
        String statusText = savedUser.getStatus() == 1 ? "已启用" : "待激活";
        response.put("message", "注册成功，账户状态为：" + statusText);
        response.put("userId", savedUser.getId());
        response.put("status", statusText);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("用户未登录");
        }
        
        String username = authentication.getName();
        SysUser user = userService.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        // 隐藏密码信息
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
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