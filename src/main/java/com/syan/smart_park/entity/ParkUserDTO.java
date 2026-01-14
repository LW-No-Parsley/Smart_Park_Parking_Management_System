package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车场小程序用户DTO
 */
@Data
public class ParkUserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 微信openid
     */
    private String openid;
    
    /**
     * 用户名（微信昵称）
     */
    private String username;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户类型：1-车主，2-访客
     */
    private Integer userType;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 用户状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 静态方法：从ParkUser实体转换为ParkUserDTO
     */
    public static ParkUserDTO fromParkUser(ParkUser parkUser) {
        if (parkUser == null) {
            return null;
        }
        
        ParkUserDTO dto = new ParkUserDTO();
        dto.setId(parkUser.getId());
        dto.setOpenid(parkUser.getOpenid());
        dto.setUsername(parkUser.getUsername());
        dto.setPhone(parkUser.getPhone());
        dto.setUserType(parkUser.getUserType());
        dto.setEmail(parkUser.getEmail());
        dto.setAvatar(parkUser.getAvatar());
        dto.setStatus(parkUser.getStatus());
        dto.setCreateTime(parkUser.getCreateTime());
        dto.setUpdateTime(parkUser.getUpdateTime());
        
        return dto;
    }
}
