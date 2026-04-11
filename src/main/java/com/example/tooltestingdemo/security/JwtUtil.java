package com.example.tooltestingdemo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey signingKey;
    
    @PostConstruct
    public void init() {
        // 确保密钥长度足够，至少32字节
        String key = secret;
        if (key.length() < 32) {
            // 如果密钥太短，用空格填充
            key = String.format("%-32s", key);
        } else if (key.length() > 32) {
            // 如果密钥太长，截取前32字节
            key = key.substring(0, 32);
        }
        signingKey = Keys.hmacShaKeyFor(key.getBytes());
    }
    
    private SecretKey getSigningKey() {
        return signingKey;
    }
    
    /**
     * 生成JWT Token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        // 获取UTC时间
        Date utcExpiration = claims.getExpiration();
        
        // 转换为Asia/Shanghai时区
        ZonedDateTime utcDateTime = utcExpiration.toInstant().atZone(ZoneId.of("UTC"));
        ZonedDateTime shanghaiDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        
        // 转换回Date对象
        return Date.from(shanghaiDateTime.toInstant());
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}