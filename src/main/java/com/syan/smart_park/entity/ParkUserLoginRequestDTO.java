package com.syan.smart_park.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 停车场小程序用户登录请求DTO
 */
@Data
public class ParkUserLoginRequestDTO {
    
    /**
     * 微信openid
     */
    @NotBlank(message = "openid不能为空")
    private String openid;
    
    /**
     * 用户名（微信昵称，可选，首次登录时提供）
     */
    private String username;
    
    /**
     * 头像URL（可选，首次登录时提供）
     */
    private String avatar;
    
    /**
     * 手机号（可选，绑定手机时提供）
     */
    private String phone;
    
    /**
     * 用户类型：1-车主，2-访客（可选，默认1-车主）
     */
    private Integer userType;
    
    /**
     * 邮箱（可选）
     */
    private String email;
}
