package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户DTO（不含密码等敏感信息）
 */
@Data
public class AdminUserDTO {

    private Long id;

    private String username;

    private String phone;

    private String email;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static AdminUserDTO fromUser(User user) {
        if (user == null) return null;
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}
