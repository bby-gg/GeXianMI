package com.gexianmi.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌生成与验证工具类。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥，生产环境应从配置文件读取 */
    private static final String SECRET = "GeXianMI-Monitoring-System-JWT-Secret-Key-2026!!";
    /** Token 过期时间，默认 24 小时 */
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT Token，包含用户ID、用户名和角色信息。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT Token 字符串
     */
    public String generate(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中解析用户名。
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    /**
     * 从 Token 中解析用户ID。
     *
     * @param token JWT Token 字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {
        return parse(token).getPayload().get("userId", Long.class);
    }

    /**
     * 从 Token 中解析用户角色。
     *
     * @param token JWT Token 字符串
     * @return 角色名
     */
    public String getRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）。
     *
     * @param token JWT Token 字符串
     * @return true 表示有效，false 表示无效或已过期
     */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 解析 Token 并返回 Claims，签名错误或过期时抛出 JwtException。
     *
     * @param token JWT Token 字符串
     * @return 解析后的 JWS Claims 对象
     */
    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
