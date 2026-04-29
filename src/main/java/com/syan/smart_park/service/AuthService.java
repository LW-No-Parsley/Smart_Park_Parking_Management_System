package com.syan.smart_park.service;

import com.syan.smart_park.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 用户登录（增强版，包含请求信息）
     *
     * @param username 用户名
     * @param password 密码
     * @param request HttpServletRequest
     * @return 登录结果，包含token和用户信息
     */
    Map<String, Object> login(String username, String password, HttpServletRequest request);
    
    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    boolean register(User user);
    
    /**
     * 刷新token（旧版，仅刷新accessToken）
     *
     * @param refreshToken 刷新token
     * @return 新的accessToken
     */
    String refreshToken(String refreshToken);

    /**
     * 刷新token（安全版，同时将旧accessToken加入黑名单）
     * <p>
     * 安全设计：refreshToken仅在登录时获取一次，刷新接口只返回新的accessToken，
     * 不返回新的refreshToken。同时会将旧的accessToken加入黑名单，防止其继续使用。
     * 这样即使refreshToken泄露，攻击者也只能获取短期有效的accessToken，无法无限续期。
     * 当refreshToken过期后，用户必须重新登录。
     *
     * @param refreshToken 刷新token（仅在登录时获取）
     * @param oldAccessToken 当前即将过期的accessToken（将被加入黑名单）
     * @return 新的accessToken
     */
    String refreshToken(String refreshToken, String oldAccessToken);
    
    /**
     * 登出（安全版）
     * <p>
     * 同时将accessToken和refreshToken加入黑名单，并兜底拉黑该用户所有token。
     * 确保用户登出后，所有已签发的token都立即失效。
     *
     * @param accessToken 访问token（通过Authorization头传递）
     * @param refreshToken 刷新token（通过X-Refresh-Token头传递，可为null）
     * @return 是否成功
     */
    boolean logout(String accessToken, String refreshToken);
    
    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 重置密码
     *
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param captchaCode 验证码
     * @param captchaId 验证码ID
     * @return 是否成功
     */
    boolean resetPassword(String username, String oldPassword, String newPassword, String captchaCode, String captchaId);
    
    /**
     * 忘记密码 - 发送重置邮件/短信
     *
     * @param username 用户名
     * @param email 邮箱
     * @param captchaCode 验证码
     * @param captchaId 验证码ID
     * @return 是否成功发送
     */
    boolean forgotPassword(String username, String email, String captchaCode, String captchaId);
}
