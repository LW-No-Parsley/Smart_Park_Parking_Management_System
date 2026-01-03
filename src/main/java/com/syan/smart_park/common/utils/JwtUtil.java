package com.syan.smart_park.common.utils;

import com.syan.smart_park.dao.TokenBlacklistMapper;
import com.syan.smart_park.entity.TokenBlacklist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret:smart-park-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration; // 默认24小时

    private final TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 生成访问token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token字符串
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(username, userId, expiration);
    }

    /**
     * 生成刷新token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        // 刷新token有效期更长，例如7天
        return generateToken(username, userId, expiration * 7);
    }

    /**
     * 生成重置密码token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token字符串
     */
    public String generateResetToken(Long userId, String username) {
        // 重置密码token有效期较短，例如1小时
        return generateToken(username, userId, 3600000L);
    }

    /**
     * 生成JWT token
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param expirationTime 过期时间（毫秒）
     * @return token字符串
     */
    private String generateToken(String username, Long userId, Long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("created", new Date());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 获取访问token过期时间（毫秒）
     *
     * @return 过期时间
     */
    public Long getAccessTokenExpiration() {
        return expiration;
    }

    /**
     * 从token中获取用户名
     *
     * @param token token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取用户ID
     *
     * @param token token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中获取Claims
     *
     * @param token token字符串
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证token是否有效
     *
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 检查token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return false;
            }
            
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查token是否过期
     *
     * @param token token字符串
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 从token中获取过期时间
     *
     * @param token token字符串
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 生成过期时间
     *
     * @return 过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration);
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param token token字符串
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        TokenBlacklist blacklist = tokenBlacklistMapper.selectByToken(token);
        return blacklist != null;
    }

    /**
     * 将token加入黑名单
     *
     * @param token token字符串
     */
    public void addToBlacklist(String token) {
        if (validateToken(token)) {
            Date expirationDate = getExpirationDateFromToken(token);
            TokenBlacklist blacklist = new TokenBlacklist();
            blacklist.setToken(token);
            // 将Date转换为LocalDateTime
            blacklist.setExpireTime(expirationDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            tokenBlacklistMapper.insert(blacklist);
        }
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
