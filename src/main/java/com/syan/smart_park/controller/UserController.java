package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器（需要认证的接口示例）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户信息
     *
     * @param token JWT token
     * @return 用户信息
     */
    @GetMapping("/info")
    public R<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            return R.unauthorized();
        }
        
        // 获取用户信息
        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 构建用户信息（这里应该从数据库查询，这里简化处理）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("username", username);
        userInfo.put("message", "这是需要JWT认证才能访问的接口");
        
        return R.success(userInfo);
    }

    /**
     * 验证token有效性
     *
     * @param token JWT token
     * @return 验证结果
     */
    @PostMapping("/validate")
    public R<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        boolean isValid = jwtUtil.validateToken(token);
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("expired", isExpired);
        
        if (isValid && !isExpired) {
            result.put("username", jwtUtil.getUsernameFromToken(token));
            result.put("userId", jwtUtil.getUserIdFromToken(token));
        }
        
        return R.success(result);
    }
}
