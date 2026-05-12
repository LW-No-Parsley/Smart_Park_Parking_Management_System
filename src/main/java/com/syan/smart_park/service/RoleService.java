package com.syan.smart_park.service;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.Role;
import com.syan.smart_park.entity.Permission;

import java.util.List;

/**
 * 角色权限服务接口
 */
public interface RoleService {
    
    /**
     * 统一查询角色列表（支持多条件筛选 + 分页）
     *
     * @param userId 用户ID（可选，查询该用户拥有的角色）
     * @param status 角色状态：0-禁用，1-启用（可选）
     * @param page   页码
     * @param size   每页大小
     */
    PageResult<Role> listRoles(Long userId, Integer status, Integer page, Integer size);
    
    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long roleId);
    
    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    boolean createRole(Role role);
    
    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    boolean updateRole(Role role);
    
    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);
    
    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean assignRoleToUser(Long userId, Long roleId);
    
    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean removeRoleFromUser(Long userId, Long roleId);
    
    /**
     * 获取所有权限
     *
     * @return 权限列表
     */
    List<Permission> getAllPermissions();
    
    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 批量设置角色的权限（全量覆盖）
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean assignPermissionToRole(Long roleId, Long permissionId);
    
    /**
     * 移除角色的权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean removePermissionFromRole(Long roleId, Long permissionId);
    
    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getRolePermissions(Long roleId);
    
    /**
     * 获取用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);
    
    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);
}
