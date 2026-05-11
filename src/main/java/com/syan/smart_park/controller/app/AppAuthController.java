package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.common.utils.JwtUtil;
import com.syan.smart_park.common.utils.RateLimitUtil;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.entity.ParkUserLoginRequestDTO;
import com.syan.smart_park.service.ParkUserService;
import com.syan.smart_park.service.WechatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Slf4j
public class AppAuthController {

    private final ParkUserService parkUserService;
    private final JwtUtil jwtUtil;
    private final WechatService wechatService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody ParkUserLoginRequestDTO loginRequest) {
        // 登录频率限制（按openid）
        if (!RateLimitUtil.tryAcquire("app-login:" + loginRequest.getOpenid())) {
            return R.error(ReturnCode.RC500, "登录尝试次数过多，请稍后再试");
        }

        // 优先通过 code 换取真实 openid，防止客户端伪造
        String realOpenid = null;
        if (loginRequest.getCode() != null && !loginRequest.getCode().isBlank()) {
            realOpenid = wechatService.getOpenidByCode(loginRequest.getCode(), true);
        }
        if (realOpenid == null && loginRequest.getCode() != null && !loginRequest.getCode().isBlank()) {
            return R.error(ReturnCode.RC500, "微信登录凭证校验失败");
        }
        String effectiveOpenid = realOpenid != null ? realOpenid : loginRequest.getOpenid();
        if (realOpenid == null) {
            log.warn("未通过 code2session 验证 openid，请配置微信 app-id 和 app-secret");
        }

        ParkUserDTO parkUserDTO = parkUserService.getByOpenid(effectiveOpenid);

        ParkUser parkUser = new ParkUser();
        parkUser.setOpenid(effectiveOpenid);
        parkUser.setUsername(loginRequest.getUsername());
        parkUser.setAvatar(loginRequest.getAvatar());
        parkUser.setPhone(loginRequest.getPhone());
        parkUser.setUserType(loginRequest.getUserType() != null ? loginRequest.getUserType() : 2);
        parkUser.setEmail(loginRequest.getEmail());

        parkUserDTO = parkUserService.createOrUpdate(parkUser);

        if (parkUserDTO == null) {
            return R.error(ReturnCode.RC500);
        }

        String accessToken = jwtUtil.generateAccessToken(parkUserDTO.getId(), parkUserDTO.getUsername(), JwtUtil.USER_TYPE_PARK_USER);
        String refreshToken = jwtUtil.generateRefreshToken(parkUserDTO.getId(), parkUserDTO.getUsername(), JwtUtil.USER_TYPE_PARK_USER);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());
        result.put("user", parkUserDTO);

        return R.success(result);
    }

    @PostMapping("/refresh")
    public R<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String token = refreshTokenHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            return R.unauthorized();
        }

        // 将旧refreshToken加入黑名单，防止重复使用（token轮换）
        jwtUtil.addToBlacklist(token);

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);

        String newAccessToken = jwtUtil.generateAccessToken(userId, username, userType);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, userType);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtil.getAccessTokenExpiration());

        return R.success(result);
    }

    @PostMapping("/validate")
    public R<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
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
