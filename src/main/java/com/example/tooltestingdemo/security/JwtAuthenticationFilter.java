package com.example.tooltestingdemo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return
                 uri.startsWith("/api/auth/")
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs/")
                || uri.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if ( uri.startsWith("/api/auth/")
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs/")
                || uri.startsWith("/webjars/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(tokenHeader);
        String username = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                log.error("JWT Token解析失败: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 检查Token是否在黑名单中
                String blacklistedToken = redisTemplate.opsForValue().get("logout:token:" + jwtToken);
                if (blacklistedToken != null) {
                    log.warn("Token已被加入黑名单，拒绝访问: {}", jwtToken);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json; charset=utf-8");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"code\": 401, \"message\": \"Token已失效，请重新登录\", \"data\": null}");
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwtToken) && !jwtUtil.isTokenExpired(jwtToken)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("用户加载失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\": 401, \"message\": \"" + e.getMessage() + "\", \"data\": null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}