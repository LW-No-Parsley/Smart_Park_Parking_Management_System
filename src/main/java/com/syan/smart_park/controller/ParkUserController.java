package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.common.utils.JwtUtil;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.entity.ParkUserLoginRequestDTO;
import com.syan.smart_park.service.ParkUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 停车场小程序用户控制器
 */
@RestController
@RequestMapping("/api/park-user")
@RequiredArgsConstructor
public class ParkUserController {

    private final ParkUserService parkUserService;
    private final JwtUtil jwtUtil;

    /**
     * 小程序用户登录（微信登录）
     *
     * @param loginRequest 登录请求，包含openid和用户信息
     * @return 登录结果，包含token和用户基本信息
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody ParkUserLoginRequestDTO loginRequest) {
        // 根据openid查询用户
        ParkUserDTO parkUserDTO = parkUserService.getByOpenid(loginRequest.getOpenid());
        
        // 创建或更新用户信息
        ParkUser parkUser = new ParkUser();
        parkUser.setOpenid(loginRequest.getOpenid());
        parkUser.setUsername(loginRequest.getUsername());
        parkUser.setAvatar(loginRequest.getAvatar());
        parkUser.setPhone(loginRequest.getPhone());
        parkUser.setUserType(loginRequest.getUserType() != null ? loginRequest.getUserType() : 2); // 默认1-车主
        parkUser.setEmail(loginRequest.getEmail());
        
        // 如果用户不存在，创建新用户；如果存在，更新用户信息
        parkUserDTO = parkUserService.createOrUpdate(parkUser);
        
        if (parkUserDTO == null) {
            return R.error(ReturnCode.RC500);
        }
        
        // 生成双token
        String accessToken = jwtUtil.generateAccessToken(parkUserDTO.getId(), parkUserDTO.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(parkUserDTO.getId(), parkUserDTO.getUsername());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
        result.put("user", parkUserDTO);
        
        return R.success(result);
    }

    /**
     * 获取用户信息
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    @GetMapping("/info")
    public R<ParkUserDTO> getUserInfo(@RequestParam String openid) {
        ParkUserDTO parkUserDTO = parkUserService.getByOpenid(openid);
        if (parkUserDTO == null) {
            return R.error(ReturnCode.RC600); // 用户不存在
        }
        return R.success(parkUserDTO);
    }

    /**
     * 绑定手机号
     *
     * @param openid 微信openid
     * @param phone 手机号
     * @return 绑定结果
     */
    @PostMapping("/bind-phone")
    public R<ParkUserDTO> bindPhone(@RequestParam String openid, @RequestParam String phone) {
        // 检查手机号是否已被其他用户绑定
        ParkUserDTO existingUser = parkUserService.getByPhone(phone);
        if (existingUser != null && !existingUser.getOpenid().equals(openid)) {
            return R.error(ReturnCode.RC603); // 手机号已被其他用户绑定
        }
        
        // 获取当前用户
        ParkUserDTO currentUser = parkUserService.getByOpenid(openid);
        if (currentUser == null) {
            return R.error(ReturnCode.RC600); // 用户不存在
        }
        
        // 更新用户手机号
        ParkUser parkUser = new ParkUser();
        parkUser.setOpenid(openid);
        parkUser.setPhone(phone);
        
        ParkUserDTO updatedUser = parkUserService.createOrUpdate(parkUser);
        return R.success(updatedUser);
    }

    /**
     * 刷新token（双token机制）
     *
     * @param refreshToken 刷新token
     * @return 新的access token和refresh token
     */
    @PostMapping("/refresh")
    public R<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        // 移除Bearer前缀
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        // 验证refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            return R.unauthorized();
        }
        
        // 从refresh token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        
        // 生成新的access token
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        
        // 生成新的refresh token（refresh token轮换）
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
        
        return R.success(result);
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
        
        boolean isValid = jwtUtil.validateToken(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("message", isValid ? "Token有效" : "Token无效或已过期");
        
        return R.success(result);
    }
}
