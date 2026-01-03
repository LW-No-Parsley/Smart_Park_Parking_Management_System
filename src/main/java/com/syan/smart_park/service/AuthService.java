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
     * 刷新token
     *
     * @param refreshToken 刷新token
     * @return 新的访问token
     */
    String refreshToken(String refreshToken);
    
    /**
     * 登出
     *
     * @param token 访问token
     * @return 是否成功
     */
    boolean logout(String token);
    
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
