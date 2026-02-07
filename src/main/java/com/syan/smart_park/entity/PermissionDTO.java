package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限数据传输对象
 * 用于过滤敏感字段，如deleted、updateTime等
 */
@Data
public class PermissionDTO {
    
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
     * 权限路径/URL
     */
    private String permissionPath;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 权限状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 从Permission实体转换为PermissionDTO
     */
    public static PermissionDTO fromPermission(Permission permission) {
        if (permission == null) return null;
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setPermissionName(permission.getPermissionName());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setPermissionType(permission.getPermissionType());
        dto.setPermissionPath(permission.getPermissionPath());
        dto.setParentId(permission.getParentId());
        dto.setIcon(permission.getIcon());
        dto.setStatus(permission.getStatus());
        dto.setSortOrder(permission.getSortOrder());
        dto.setCreateTime(permission.getCreateTime());
        return dto;
    }
}
