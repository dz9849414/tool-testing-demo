package com.example.tooltestingdemo.controller;

import com.example.tooltestingdemo.entity.SysLoginLog;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.SysUserOrganization;
import com.example.tooltestingdemo.mapper.SysUserOrganizationMapper;
import com.example.tooltestingdemo.security.JwtUtil;
import com.example.tooltestingdemo.service.SysLoginLogService;
import com.example.tooltestingdemo.service.SysUserService;
import com.example.tooltestingdemo.dto.AuthLoginDTO;
import com.example.tooltestingdemo.dto.AuthRegisterDTO;
import com.example.tooltestingdemo.vo.AuthVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<?> login(@RequestBody AuthLoginDTO loginDTO, HttpServletRequest request) {
        // 创建登录日志
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(loginDTO.getUsername());
        loginLog.setIpAddress(getClientIP(request));
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        loginLog.setLoginType("LOCAL");

        // 先检查用户是否存在
        SysUser user = userService.findByUsername(loginDTO.getUsername());
        if (user == null) {
            loginLog.setStatus(0);
            loginLog.setErrorMessage("用户不存在: " + loginDTO.getUsername());
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在: " + loginDTO.getUsername());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查用户是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLog.setUserId(user.getId());
            loginLog.setStatus(0);
            loginLog.setErrorMessage("用户已被禁用: " + loginDTO.getUsername());
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户已被禁用: " + loginDTO.getUsername());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = jwtUtil.generateToken(loginDTO.getUsername());
            
            // 更新用户最后登录信息
            userService.updateLastLoginInfo(user.getId(), getClientIP(request));
            
            // 记录成功登录日志
            loginLog.setUserId(user.getId());
            loginLog.setStatus(1);
            loginLog.setErrorMessage(null);
            loginLogService.recordLoginLog(loginLog);
            
            // 创建响应VO
            AuthVO authVO = new AuthVO();
            authVO.setToken(jwt);
            authVO.setUsername(loginDTO.getUsername());
            authVO.setExpiresIn(jwtUtil.getExpirationDateFromToken(jwt));
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", authVO);
            
            return ResponseEntity.ok(response);
            
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // 记录失败登录日志
            loginLog.setUserId(user.getId());
            loginLog.setStatus(0);
            loginLog.setErrorMessage(e.getMessage());
            loginLogService.recordLoginLog(loginLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
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
    public ResponseEntity<?> register(@RequestBody AuthRegisterDTO registerDTO) {
        if (userService.existsByUsername(registerDTO.getUsername())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "用户名已存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        if (userService.existsByEmail(registerDTO.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "邮箱已存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        SysUser user = new SysUser();
        // 生成尽量不重复的随机Long类型ID
        // 使用时间戳 + 随机数组合，确保唯一性
        long timestamp = System.currentTimeMillis();
        long random = (long) (Math.random() * 1000000);
        long userId = (timestamp << 20) | (random & 0xFFFFF);
        user.setId(userId);
        
        // 使用BeanUtils.copyProperties()复制属性
        try {
            BeanUtils.copyProperties(user, registerDTO);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "参数转换失败");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        SysUser savedUser = userService.save(user);
        
        // 处理部门关联
        if (registerDTO.getOrganizationId() != null && !registerDTO.getOrganizationId().isEmpty()) {
            SysUserOrganization userOrganization = new SysUserOrganization();
            userOrganization.setId(java.util.UUID.randomUUID().toString().replace("-", "_"));
            userOrganization.setUserId(String.valueOf(savedUser.getId()));
            userOrganization.setOrgId(registerDTO.getOrganizationId());
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
    

}