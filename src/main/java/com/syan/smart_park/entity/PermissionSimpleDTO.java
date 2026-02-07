package com.syan.smart_park.entity;

import lombok.Data;

/**
 * 简化权限数据传输对象
 * 用于登录接口返回，只包含必要字段
 */
@Data
public class PermissionSimpleDTO {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限编码
     */
    private String permissionCode;
    
    /**
     * 权限类型：1-菜单，2-按钮，3-接口
     */
    private Integer permissionType;
    
    /**
     * 从Permission实体转换为PermissionSimpleDTO
     */
    public static PermissionSimpleDTO fromPermission(Permission permission) {
        if (permission == null) return null;
        PermissionSimpleDTO dto = new PermissionSimpleDTO();
        dto.setId(permission.getId());
        dto.setPermissionName(permission.getPermissionName());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setPermissionType(permission.getPermissionType());
        return dto;
    }
}
