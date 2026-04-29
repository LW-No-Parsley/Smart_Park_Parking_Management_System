package com.syan.smart_park.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.dao.TokenBlacklistMapper;
import com.syan.smart_park.dao.UserMapper;
import com.syan.smart_park.entity.LoginLog;
import com.syan.smart_park.entity.TokenBlacklist;
import com.syan.smart_park.entity.User;
import com.syan.smart_park.entity.UserDTO;
import com.syan.smart_park.service.AuthService;
import com.syan.smart_park.service.CaptchaService;
import com.syan.smart_park.service.LoginLogService;
import com.syan.smart_park.service.UserService;
import com.syan.smart_park.common.utils.IpUtil;
import com.syan.smart_park.common.utils.JwtUtil;
import com.syan.smart_park.common.utils.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final CaptchaService captchaService;
    private final LoginLogService loginLogService;
    private final TokenBlacklistMapper tokenBlacklistMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public Map<String, Object> login(String username, String password) {
        // 1. 验证用户登录
        User user = userService.login(username, password);

        // 2. 清除该用户的全局拉黑标记（如果之前登出过）
        jwtUtil.clearUserGlobalInvalidation(user.getId());

        // 3. 生成token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 3. 记录登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIpAddress(""); // 实际项目中从请求中获取
        loginLog.setBrowser(""); // 实际项目中从请求中获取
        loginLog.setOs(""); // 实际项目中从请求中获取
        loginLog.setStatus(1); // 登录成功
        loginLog.setFailureReason(""); // 登录成功时为空
        loginLogService.recordLogin(loginLog);

        // 4. 返回结果（启用双token机制，返回refreshToken）
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken); // 返回refresh token给客户端
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
        result.put("user", UserDTO.fromUser(user));
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        // 1. 验证用户登录
        User user = userService.login(username, password);

        // 2. 清除该用户的全局拉黑标记（如果之前登出过）
        jwtUtil.clearUserGlobalInvalidation(user.getId());

        // 3. 生成token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 3. 获取IP地址和地理位置
        String ipAddress = IpUtil.getIpAddress(request);
        String location = IpUtil.getLocationByIp(ipAddress);
        
        // 4. 获取浏览器和操作系统信息
        String browser = UserAgentUtil.getBrowser(request);
        String os = UserAgentUtil.getOs(request);

        // 5. 记录登录日志（包含IP、地理位置、浏览器、操作系统信息）
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIpAddress(ipAddress);
        loginLog.setLocation(location);
        loginLog.setBrowser(browser);
        loginLog.setOs(os);
        loginLog.setStatus(1); // 登录成功
        loginLog.setFailureReason(""); // 登录成功时为空
        loginLogService.recordLogin(loginLog);

        // 6. 返回结果（启用双token机制，返回refreshToken）
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken); // 返回refresh token给客户端
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
        result.put("user", UserDTO.fromUser(user));
        
        return result;
    }

    @Override
    @Transactional
    public boolean register(User user) {
        // 注册用户（验证码已在Controller层验证）
        return userService.register(user);
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 1. 验证refresh token（包含黑名单检查、签名验证、过期检查和全局拉黑检查）
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ReturnCode.RC609); // token无效
        }

        // 2. 检查token类型是否为refreshToken，防止使用accessToken来刷新
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ReturnCode.RC609, "仅支持使用refreshToken刷新");
        }

        // 3. 解析token获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 4. 生成新的access token（不生成新的refreshToken）
        // 安全设计：refreshToken仅在登录时获取一次，刷新接口只返回新的accessToken，
        // 不返回新的refreshToken。这样即使refreshToken泄露，攻击者也只能获取短期
        // 有效的accessToken，无法无限续期。当refreshToken过期后，用户必须重新登录。
        return jwtUtil.generateAccessToken(userId, username);
    }

    @Override
    public String refreshToken(String refreshToken, String oldAccessToken) {
        // 1. 验证refresh token（包含黑名单检查、签名验证、过期检查和全局拉黑检查）
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ReturnCode.RC609); // token无效
        }

        // 2. 检查token类型是否为refreshToken，防止使用accessToken来刷新
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ReturnCode.RC609, "仅支持使用refreshToken刷新");
        }

        // 3. 解析token获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 4. 将旧的accessToken加入黑名单，防止其在有效期内被继续使用
        if (oldAccessToken != null && !oldAccessToken.isEmpty()) {
            jwtUtil.addToBlacklist(oldAccessToken);
        }

        // 5. 生成新的access token（不生成新的refreshToken）
        return jwtUtil.generateAccessToken(userId, username);
    }

    @Override
    @Transactional
    public boolean logout(String accessToken, String refreshToken) {
        // 1. 验证accessToken是否存在
        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }

        // 2. 尝试从accessToken中解析userId（使用getUserIdFromTokenEvenIfExpired，即使token过期也能解析）
        Long userId = null;
        try {
            userId = jwtUtil.getUserIdFromTokenEvenIfExpired(accessToken);
        } catch (Exception e) {
            // 如果accessToken完全无法解析（如格式错误、签名无效），尝试从refreshToken获取userId
            System.out.println("Failed to get userId from accessToken: " + e.getMessage());
        }

        // 如果accessToken无法解析出userId，尝试从refreshToken获取
        if (userId == null && refreshToken != null && !refreshToken.isEmpty()) {
            try {
                userId = jwtUtil.getUserIdFromTokenEvenIfExpired(refreshToken);
            } catch (Exception ex) {
                System.out.println("Failed to get userId from refreshToken: " + ex.getMessage());
            }
        }

        // 如果两个token都无法解析出userId，则登出失败
        if (userId == null) {
            return false;
        }

        // 3. 将accessToken加入黑名单（即使token已过期，也能通过getClaimsEvenIfExpired获取jti）
        jwtUtil.addToBlacklist(accessToken, 1);

        // 4. 如果传入了refreshToken，也将其加入黑名单
        if (refreshToken != null && !refreshToken.isEmpty()) {
            jwtUtil.addToBlacklist(refreshToken, 1);
        }

        // 5. 兜底：将该用户所有token全局拉黑
        // 这样即使有未传入的token（如客户端只传了accessToken没传refreshToken），
        // 也会因为userId被全局拉黑而失效
        jwtUtil.invalidateAllUserTokens(userId);

        return true;
    }

    @Override
    public boolean validateToken(String token) {
        // 使用JwtUtil的validateToken方法进行完整验证：
        // 1. 检查token是否在黑名单中（按jti检查）
        // 2. 检查token签名和过期时间
        // 3. 检查该用户是否被全局拉黑（按userId兜底检查）
        return jwtUtil.validateToken(token);
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String oldPassword, String newPassword, String captchaCode, String captchaId) {
        // 1. 验证验证码
        if (!captchaService.validateCaptcha(captchaId, captchaCode)) {
            throw new BusinessException(ReturnCode.RC605); // 验证码错误
        }

        // 2. 验证旧密码
        User user = userService.login(username, oldPassword);
        if (user == null) {
            throw new BusinessException(ReturnCode.RC601); // 用户名或密码错误
        }

        // 3. 更新密码
        user.setPassword(DigestUtil.md5Hex(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public boolean forgotPassword(String username, String email, String captchaCode, String captchaId) {
        // 1. 验证验证码
        if (!captchaService.validateCaptcha(captchaId, captchaCode)) {
            throw new BusinessException(ReturnCode.RC605); // 验证码错误
        }

        // 2. 验证用户和邮箱
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }

        // 3. 验证邮箱（实际项目中需要验证邮箱是否匹配）
        // 这里简化处理，实际项目中应该发送重置密码邮件
        User mail = userService.findByUsername(email);
        if (mail == null) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }
        
        // 4. 生成重置token（实际项目中应该发送邮件）
        String resetToken = jwtUtil.generateResetToken(user.getId(), user.getUsername());
        
        // 5. 记录重置请求（实际项目中应该保存到数据库）
        
        return true;
    }
}
