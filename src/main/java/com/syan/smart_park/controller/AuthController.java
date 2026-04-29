package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.common.utils.JwtUtil;
import com.syan.smart_park.entity.User;
import com.syan.smart_park.entity.LoginLog;
import com.syan.smart_park.entity.RegisterRequestDTO;
import com.syan.smart_park.entity.LoginRequestDTO;
import com.syan.smart_park.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final RoleService roleService;
    private final CaptchaService captchaService;
    private final LoginLogService loginLogService;
    private final JwtUtil jwtUtil;

    /**
     * 获取验证码
     *
     * @return 验证码信息
     */
    @GetMapping("/captcha")
    public R<Map<String, Object>> getCaptcha() {
        CaptchaService.CaptchaInfo captchaInfo = captchaService.generateCaptcha();
        Map<String, Object> captchaData = new HashMap<>();
        captchaData.put("captchaId", captchaInfo.getCaptchaId());
        captchaData.put("captchaImage", captchaInfo.getCaptchaImageBase64());
        return R.success(captchaData);
    }

    /**
     * 用户登录（增强版，支持验证码和登录日志记录）
     *
     * @param loginRequest 登录请求，包含用户名、密码和验证码
     * @param request HttpServletRequest对象，用于获取客户端信息
     * @return 登录结果，包含token和用户基本信息
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request) {
        // 验证验证码
        boolean captchaValid = captchaService.validateCaptcha(loginRequest.getCaptchaId(), loginRequest.getCaptchaCode());
        if (!captchaValid) {
            return R.error(ReturnCode.RC605);
        }

        // 使用AuthService进行登录认证（验证码已在Controller层验证）
        // 传递HttpServletRequest以获取客户端IP、地理位置等信息
        Map<String, Object> loginResult = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), request);
        
        if (loginResult == null) {
            return R.error(ReturnCode.RC601);
        }

        // 不再返回权限和角色信息，由后端进行权限校验
        // 只返回token和用户基本信息
        
        return R.success(loginResult);
    }

    /**
     * 用户注册（增强版，支持验证码）
     *
     * @param registerRequest 注册请求，包含用户信息和验证码
     * @return 注册结果
     */
    @PostMapping("/register")
    public R register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        // 验证验证码
        boolean captchaValid = captchaService.validateCaptcha(registerRequest.getCaptchaId(), registerRequest.getCaptchaCode());
        if (!captchaValid) {
            return R.error(ReturnCode.RC605);
        }

        // 将DTO转换为User实体
        User user = registerRequest.toUser();
        
        // 使用AuthService进行注册（不再传递验证码参数）
        boolean success = authService.register(user);
        
        if (success) {
            return R.success("注册成功");
        } else {
            return R.error(ReturnCode.RC500);
        }
    }

    /**
     * 刷新token
     * <p>
     * 安全设计：refreshToken仅在登录时获取一次，刷新接口只返回新的accessToken，
     * 不返回新的refreshToken。同时会将旧的accessToken加入黑名单，防止其继续使用。
     * 这样即使refreshToken泄露，攻击者也只能获取短期有效的accessToken，无法无限续期。
     * 当refreshToken过期后，用户必须重新登录。
     *
     * @param refreshToken 刷新token（通过Authorization头传递）
     * @param oldAccessToken 当前即将过期的accessToken（通过X-Access-Token头传递，用于加入黑名单）
     * @return 新的access token（不返回新的refreshToken）
     */
    @PostMapping("/refresh")
    public R<Map<String, Object>> refreshToken(
            @RequestHeader("Authorization") String refreshToken,
            @RequestHeader("X-Access-Token") String oldAccessToken) {
        // 移除Bearer前缀
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        if (oldAccessToken != null && oldAccessToken.startsWith("Bearer ")) {
            oldAccessToken = oldAccessToken.substring(7);
        }
        
        try {
            // 使用AuthService刷新token，同时将旧的accessToken加入黑名单
            String newAccessToken = authService.refreshToken(refreshToken, oldAccessToken);
            
            if (newAccessToken != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("accessToken", newAccessToken);
                result.put("tokenType", "Bearer");
                result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
                return R.success(result);
            } else {
                return R.error(ReturnCode.RC500);
            }
        } catch (BusinessException e) {
            // 捕获业务异常，返回对应的错误码和消息
            System.out.println("BusinessException caught: code=" + e.getCode() + ", message=" + e.getMessage());
            return R.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回401未授权
            e.printStackTrace();
            return R.error(ReturnCode.RC401, "Token刷新失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出（安全版）
     * <p>
     * 同时将accessToken和refreshToken加入黑名单，并兜底拉黑该用户所有token。
     * 确保用户登出后，所有已签发的token都立即失效。
     *
     * @param accessToken 访问token（通过Authorization头传递）
     * @param refreshToken 刷新token（通过X-Refresh-Token头传递，可选）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public R logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        // 移除Bearer前缀
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        // 使用AuthService登出（同时拉黑accessToken和refreshToken）
        boolean success = authService.logout(accessToken, refreshToken);
        
        if (success) {
            return R.success("登出成功");
        } else {
            return R.error(ReturnCode.RC500);
        }
    }

    /**
     * 忘记密码（发送重置密码邮件/短信）
     *
     * @param username 用户名
     * @param email 邮箱
     * @param captchaId 验证码ID
     * @param captchaCode 验证码
     * @return 发送结果
     */
    @PostMapping("/forgot-password")
    public R forgotPassword(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String captchaId,
                           @RequestParam String captchaCode) {
        // 验证验证码
        boolean captchaValid = captchaService.validateCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return R.error(ReturnCode.RC605);
        }

        // 使用AuthService发送重置密码邮件
        boolean success = authService.forgotPassword(username, email, captchaCode, captchaId);
        
        if (success) {
            return R.success("重置密码邮件已发送，请查收邮箱");
        } else {
            return R.error(ReturnCode.RC600);
        }
    }

    /**
     * 重置密码
     *
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param captchaId 验证码ID
     * @param captchaCode 验证码
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public R resetPassword(@RequestParam String username,
                          @RequestParam String oldPassword,
                          @RequestParam String newPassword,
                          @RequestParam String captchaId,
                          @RequestParam String captchaCode) {
        // 验证验证码
        boolean captchaValid = captchaService.validateCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return R.error(ReturnCode.RC605);
        }

        // 使用AuthService重置密码
        boolean success = authService.resetPassword(username, oldPassword, newPassword, captchaCode, captchaId);
        
        if (success) {
            return R.success("密码重置成功");
        } else {
            return R.error(ReturnCode.RC609);
        }
    }

    /**
     * 验证token
     *
     * @param token token
     * @return 验证结果
     */
    @PostMapping("/validate")
    public R<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 使用AuthService验证token
        boolean isValid = authService.validateToken(token);
        
        if (isValid) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("message", "Token有效");
            return R.success(result);
        } else {
            return R.unauthorized();
        }
    }

    /**
     * 获取用户登录日志
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 登录日志列表
     */
    @GetMapping("/login-logs")
    public R<Map<String, Object>> getLoginLogs(@RequestParam(required = false) Long userId,
                                              @RequestParam(defaultValue = "10") Integer limit) {
        List<LoginLog> logs = loginLogService.getUserLoginLogs(userId, limit);
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", logs.size());
        return R.success(result);
    }

    /**
     * 构建用户信息
     *
     * @param user 用户实体
     * @return 用户信息Map
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("status", user.getStatus());
        return userInfo;
    }
}
