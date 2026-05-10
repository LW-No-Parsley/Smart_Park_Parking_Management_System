package com.syan.smart_park.service;

import com.syan.smart_park.entity.Permission;
import com.syan.smart_park.entity.PermissionDTO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取所有权限列表（平铺）
     */
    List<PermissionDTO> getAllPermissions();

    /**
     * 获取权限树形结构（按 parentId 层级返回）
     */
    List<PermissionDTO> getPermissionTree();

    /**
     * 根据ID获取权限
     */
    PermissionDTO getPermissionById(Long id);

    /**
     * 创建权限
     */
    PermissionDTO createPermission(Permission permission);

    /**
     * 更新权限
     */
    PermissionDTO updatePermission(Long id, Permission permission);

    /**
     * 删除权限（检查是否有子权限或关联角色）
     */
    void deletePermission(Long id);

    /**
     * 获取用户的菜单树（仅返回 permissionType=1 的菜单，
     * 且只包含用户有权限的节点及其祖先节点）
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<PermissionDTO> getUserMenuTree(Long userId);
}
