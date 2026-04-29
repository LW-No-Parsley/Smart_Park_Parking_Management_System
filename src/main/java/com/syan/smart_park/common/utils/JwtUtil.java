package com.syan.smart_park.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.dao.TokenBlacklistMapper;
import com.syan.smart_park.entity.TokenBlacklist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    // token类型常量
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String TOKEN_TYPE_RESET = "reset";

    /**
     * 生成访问token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token字符串
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(username, userId, expiration, TOKEN_TYPE_ACCESS);
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
        return generateToken(username, userId, expiration * 24 * 7, TOKEN_TYPE_REFRESH);
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
        return generateToken(username, userId, 3600000L, TOKEN_TYPE_RESET);
    }

    /**
     * 生成JWT token
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param expirationTime 过期时间（毫秒）
     * @param tokenType token类型（access/refresh/reset）
     * @return token字符串
     */
    private String generateToken(String username, Long userId, Long expirationTime, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("created", new Date());
        claims.put("tokenType", tokenType); // 添加token类型，用于区分accessToken和refreshToken
        // 添加jti（JWT ID）用于黑名单管理
        String jti = UUID.randomUUID().toString();
        claims.put("jti", jti);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 判断token是否为refreshToken类型
     *
     * @param token token字符串
     * @return true如果是refreshToken
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get("tokenType", String.class);
            return TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
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
     * 从token中获取用户名（即使token过期也能获取）
     * <p>
     * 用于登出等场景，需要即使token过期也能获取用户名。
     *
     * @param token token字符串
     * @return 用户名
     */
    public String getUsernameFromTokenEvenIfExpired(String token) {
        Claims claims = getClaimsEvenIfExpired(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取用户ID（即使token过期也能获取）
     * <p>
     * 用于登出等场景，需要即使token过期也能获取用户ID。
     *
     * @param token token字符串
     * @return 用户ID
     */
    public Long getUserIdFromTokenEvenIfExpired(String token) {
        Claims claims = getClaimsEvenIfExpired(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中获取Claims
     * <p>
     * 注意：此方法会验证签名和过期时间。如果token过期，会抛出ExpiredJwtException。
     * 在需要即使token过期也要获取claims的场景（如加入黑名单），请使用getClaimsEvenIfExpired()。
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
     * 从token中获取Claims（即使token过期也能获取）
     * <p>
     * 使用parseSignedClaims并允许过期，这样即使token过期也能获取到claims。
     * 用于addToBlacklist等场景，需要即使token过期也要将其加入黑名单。
     *
     * @param token token字符串
     * @return Claims对象
     */
    public Claims getClaimsEvenIfExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // token过期时，从异常中获取claims
            return e.getClaims();
        }
    }

    /**
     * 验证token是否有效
     * <p>
     * 验证逻辑：
     * 1. 检查token是否在黑名单中（按jti检查）- 使用getClaimsEvenIfExpired()确保即使token过期也能检查
     * 2. 检查token签名和过期时间
     * 3. 检查该用户是否被全局拉黑（按userId兜底检查）
     *
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 1. 检查token是否在黑名单中（按jti检查）
            // 使用getClaimsEvenIfExpired()确保即使token过期也能获取jti进行黑名单检查
            if (isTokenBlacklisted(token)) {
                return false;
            }
            
            // 2. 检查token签名和过期时间
            // 使用getClaimsFromToken()验证签名和过期时间，过期时会抛出ExpiredJwtException
            Claims claims = getClaimsFromToken(token);
            
            // 3. 额外检查：该用户是否被全局拉黑（按userId兜底检查）
            Long userId = claims.get("userId", Long.class);
            if (userId != null && isUserGloballyInvalidated(userId)) {
                return false;
            }
            
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // token已过期，但需要检查是否在黑名单中（已在上面的isTokenBlacklisted中检查过）
            // 如果token已过期且不在黑名单中，则返回false（过期无效）
            return false;
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
     * <p>
     * 使用getClaimsEvenIfExpired()解析token，即使token过期也能获取jti进行黑名单检查。
     * 确保已过期的token也能被正确识别为黑名单中的token。
     *
     * @param token token字符串
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            Claims claims = getClaimsEvenIfExpired(token);
            String jti = claims.get("jti", String.class);
            if (jti == null) {
                return false;
            }
            
            QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
            // 注意：数据库列名为jti，不是token。使用列名jti进行查询。
            queryWrapper.eq("jti", jti);
            TokenBlacklist blacklist = tokenBlacklistMapper.selectOne(queryWrapper);
            return blacklist != null;
        } catch (Exception e) {
            System.out.println("Failed to check token blacklist: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将token加入黑名单
     *
     * @param token token字符串
     */
    public void addToBlacklist(String token) {
        addToBlacklist(token, 1);
    }

    /**
     * 将token加入黑名单（指定原因）
     * <p>
     * 注意：此方法不调用validateToken()，因为validateToken()会检查黑名单和全局拉黑，
     * 而addToBlacklist的目的是将token加入黑名单，只需要验证token的签名和过期时间即可。
     * 如果使用validateToken()，在登出场景中可能会因为全局拉黑标记已存在而导致拉黑失败。
     *
     * @param token token字符串
     * @param reason 加入黑名单原因：1-用户登出，2-密码修改，3-管理员强制下线
     */
    public void addToBlacklist(String token, Integer reason) {
        try {
            // 使用getClaimsEvenIfExpired()解析token，即使token过期也能获取claims
            Claims claims = getClaimsEvenIfExpired(token);
            String jti = claims.get("jti", String.class);
            Long userId = claims.get("userId", Long.class);
            Date expirationDate = claims.getExpiration();
            
            System.out.println("addToBlacklist - jti: " + jti + ", userId: " + userId + ", reason: " + reason);
            
            if (jti == null) {
                System.out.println("addToBlacklist - jti is null, skipping");
                return;
            }
            
            // 检查是否已经在黑名单中，避免重复插入
            QueryWrapper<TokenBlacklist> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("jti", jti);
            TokenBlacklist existing = tokenBlacklistMapper.selectOne(checkWrapper);
            if (existing != null) {
                System.out.println("addToBlacklist - jti already in blacklist, skipping");
                return; // 已在黑名单中，不重复插入
            }
            
            TokenBlacklist blacklist = new TokenBlacklist();
            blacklist.setToken(jti); // 存储jti而不是整个token
            blacklist.setUserId(userId);
            // 将Date转换为LocalDateTime
            blacklist.setExpirationTime(expirationDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            // 设置失效时间为当前时间
            blacklist.setInvalidatedTime(LocalDateTime.now());
            // 设置原因
            blacklist.setReason(reason);
            // 显式设置逻辑删除标志为0（未删除），因为@TableLogic注解要求deleted=0才能被查询到
            blacklist.setDeleted(0);
            int insertResult = tokenBlacklistMapper.insert(blacklist);
            System.out.println("addToBlacklist - insert result: " + insertResult + ", jti: " + jti);
        } catch (Exception e) {
            System.out.println("Failed to add token to blacklist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 使该用户的所有token失效（按userId兜底拉黑）
     * <p>
     * 在TokenBlacklist表中插入一条userId维度的全局失效标记记录，
     * 后续在validateToken中会额外检查该用户的全局失效标记。
     * 这样即使有未传入的token（如客户端只传了accessToken没传refreshToken），
     * 也会因为userId被全局拉黑而失效。
     *
     * @param userId 用户ID
     */
    public void invalidateAllUserTokens(Long userId) {
        try {
            // 插入一条userId维度的全局失效标记
            // jti使用固定前缀 + userId，表示这是针对整个用户的失效标记
            String globalJti = "user_global_invalid_" + userId;
            
            // 先检查是否已存在
            QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("jti", globalJti);
            TokenBlacklist existing = tokenBlacklistMapper.selectOne(queryWrapper);
            if (existing != null) {
                return; // 已存在则不再重复插入
            }
            
            TokenBlacklist blacklist = new TokenBlacklist();
            blacklist.setToken(globalJti);
            blacklist.setUserId(userId);
            // 设置一个很长的过期时间（比如30天），确保覆盖所有token的有效期
            blacklist.setExpirationTime(LocalDateTime.now().plusDays(30));
            blacklist.setInvalidatedTime(LocalDateTime.now());
            blacklist.setReason(1); // 用户登出
            // 显式设置逻辑删除标志为0（未删除），因为@TableLogic注解要求deleted=0才能被查询到
            blacklist.setDeleted(0);
            tokenBlacklistMapper.insert(blacklist);
            
            System.out.println("User " + userId + " all tokens invalidated (by userId global flag)");
        } catch (Exception e) {
            System.out.println("Failed to invalidate all user tokens: " + e.getMessage());
        }
    }

    /**
     * 检查该用户是否被全局拉黑
     *
     * @param userId 用户ID
     * @return true如果该用户的所有token都应被视为无效
     */
    public boolean isUserGloballyInvalidated(Long userId) {
        try {
            String globalJti = "user_global_invalid_" + userId;
            QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
            // 注意：数据库列名为jti，不是token。使用列名jti进行查询。
            queryWrapper.eq("jti", globalJti);
            TokenBlacklist blacklist = tokenBlacklistMapper.selectOne(queryWrapper);
            return blacklist != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清除该用户的全局拉黑标记
     * <p>
     * 在用户登录成功后调用，移除之前登出时设置的全局拉黑标记，
     * 使新生成的token能够正常使用。
     *
     * @param userId 用户ID
     */
    public void clearUserGlobalInvalidation(Long userId) {
        try {
            String globalJti = "user_global_invalid_" + userId;
            QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
            // 注意：数据库列名为jti，不是token。使用列名jti进行查询。
            queryWrapper.eq("jti", globalJti);
            tokenBlacklistMapper.delete(queryWrapper);
            System.out.println("User " + userId + " global invalidation flag cleared");
        } catch (Exception e) {
            System.out.println("Failed to clear user global invalidation: " + e.getMessage());
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
