package com.syan.smart_park.entity;

import lombok.Data;

/**
 * 简化角色数据传输对象
 * 用于登录接口返回，只包含必要字段
 */
@Data
public class RoleSimpleDTO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 从Role实体转换为RoleSimpleDTO
     */
    public static RoleSimpleDTO fromRole(Role role) {
        if (role == null) return null;
        RoleSimpleDTO dto = new RoleSimpleDTO();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        return dto;
    }
}
