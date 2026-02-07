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

        // 2. 生成token
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

        // 2. 生成token
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
        // 1. 验证refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ReturnCode.RC605); // token无效
        }

        // 2. 检查token是否在黑名单中
        QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", refreshToken);
        TokenBlacklist blacklistedToken = tokenBlacklistMapper.selectOne(queryWrapper);
        if (blacklistedToken != null) {
            throw new BusinessException(ReturnCode.RC605); // token无效
        }

        // 3. 解析token获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 4. 生成新的access token
        return jwtUtil.generateAccessToken(userId, username);
    }

    @Override
    @Transactional
    public boolean logout(String token) {
        // 1. 验证token
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 2. 将token加入黑名单
        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setToken(token);
        tokenBlacklist.setCreateTime(LocalDateTime.now());
        // 将Date转换为LocalDateTime
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        tokenBlacklist.setExpireTime(expirationDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime());
        
        int result = tokenBlacklistMapper.insert(tokenBlacklist);
        return result > 0;
    }

    @Override
    public boolean validateToken(String token) {
        // 1. 验证token本身
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 2. 检查token是否在黑名单中
        QueryWrapper<TokenBlacklist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", token);
        TokenBlacklist blacklistedToken = tokenBlacklistMapper.selectOne(queryWrapper);
        
        return blacklistedToken == null;
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
