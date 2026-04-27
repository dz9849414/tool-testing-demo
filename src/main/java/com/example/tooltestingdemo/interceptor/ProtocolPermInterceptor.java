package com.example.tooltestingdemo.interceptor;

import com.example.tooltestingdemo.service.ProtocolPermService;
import com.example.tooltestingdemo.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 协议权限拦截器【核心】✅ 基于Spring Security获取用户ID，再查询角色ID
 */
@Component
@RequiredArgsConstructor
public class ProtocolPermInterceptor implements HandlerInterceptor {

    private final ProtocolPermService protocolPermService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 拿当前请求地址
        String requestUri = request.getRequestURI();
        
        // 从Spring Security上下文获取用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new RuntimeException("用户信息格式错误");
        }

        CustomUserDetails sysUser = (CustomUserDetails) principal;
        Long userId = sysUser.getUserId();
        
        // 协议权限校验（基于用户ID，使用新的协议权限表）
        protocolPermService.checkPermByUserId(requestUri, userId);
        
        // 校验通过放行
        return true;
    }
}