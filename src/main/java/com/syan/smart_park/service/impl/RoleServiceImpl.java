package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;

    @Override
    public List<Role> getAllRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByAsc("create_time");
        return roleMapper.selectList(queryWrapper);
    }

    @Override
    public Role getRoleById(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC606); // 角色不存在
        }
        return role;
    }

    @Override
    @Transactional
    public boolean createRole(Role role) {
        // 检查角色编码是否已存在
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_code", role.getRoleCode())
                   .eq("deleted", 0);
        Role existingRole = roleMapper.selectOne(queryWrapper);
        if (existingRole != null) {
            throw new BusinessException(ReturnCode.RC607); // 角色编码已存在
        }

        // 设置默认值
        role.setStatus(1); // 默认启用
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setDeleted(0);

        int result = roleMapper.insert(role);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
        // 检查角色是否存在
        Role existingRole = getRoleById(role.getId());
        if (existingRole == null) {
            throw new BusinessException(ReturnCode.RC606); // 角色不存在
        }

        // 检查角色编码是否与其他角色冲突
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_code", role.getRoleCode())
                   .ne("id", role.getId())
                   .eq("deleted", 0);
        Role conflictRole = roleMapper.selectOne(queryWrapper);
        if (conflictRole != null) {
            throw new BusinessException(ReturnCode.RC607); // 角色编码已存在
        }

        // 更新角色
        role.setUpdateTime(LocalDateTime.now());
        int result = roleMapper.updateById(role);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        // 检查是否有用户使用该角色
        QueryWrapper<UserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("role_id", roleId);
        long userCount = userRoleMapper.selectCount(userRoleQuery);
        if (userCount > 0) {
            throw new BusinessException(ReturnCode.RC608); // 角色正在使用中，无法删除
        }

        // 软删除角色
        role.setDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        int result = roleMapper.updateById(role);
        
        // 删除角色权限关联
        QueryWrapper<RolePermission> rolePermissionQuery = new QueryWrapper<>();
        rolePermissionQuery.eq("role_id", roleId);
        rolePermissionMapper.delete(rolePermissionQuery);
        
        return result > 0;
    }

    @Override
    @Transactional
    public boolean assignRoleToUser(Long userId, Long roleId) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }

        // 检查角色是否存在
        getRoleById(roleId);

        // 检查是否已分配该角色
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("role_id", roleId);
        UserRole existingUserRole = userRoleMapper.selectOne(queryWrapper);
        if (existingUserRole != null) {
            throw new BusinessException(ReturnCode.RC609); // 用户已拥有该角色
        }

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(LocalDateTime.now());
        
        int result = userRoleMapper.insert(userRole);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("role_id", roleId);
        
        int result = userRoleMapper.delete(queryWrapper);
        return result > 0;
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        // 查询用户角色关联
        QueryWrapper<UserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 查询角色信息
        QueryWrapper<Role> roleQuery = new QueryWrapper<>();
        roleQuery.in("id", roleIds)
                .eq("deleted", 0)
                .eq("status", 1);
        
        return roleMapper.selectList(roleQuery);
    }

    @Override
    public List<Permission> getAllPermissions() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByAsc("type", "create_time");
        return permissionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean assignPermissionToRole(Long roleId, Long permissionId) {
        // 检查角色是否存在
        getRoleById(roleId);

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC610); // 权限不存在
        }

        // 检查是否已分配该权限
        QueryWrapper<RolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId)
                   .eq("permission_id", permissionId);
        RolePermission existingRolePermission = rolePermissionMapper.selectOne(queryWrapper);
        if (existingRolePermission != null) {
            throw new BusinessException(ReturnCode.RC611); // 角色已拥有该权限
        }

        // 分配权限
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermission.setCreateTime(LocalDateTime.now());
        
        int result = rolePermissionMapper.insert(rolePermission);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        QueryWrapper<RolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId)
                   .eq("permission_id", permissionId);
        
        int result = rolePermissionMapper.delete(queryWrapper);
        return result > 0;
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        // 查询角色权限关联
        QueryWrapper<RolePermission> rolePermissionQuery = new QueryWrapper<>();
        rolePermissionQuery.eq("role_id", roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionQuery);
        
        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        
        // 查询权限信息
        QueryWrapper<Permission> permissionQuery = new QueryWrapper<>();
        permissionQuery.in("id", permissionIds)
                      .eq("deleted", 0);
        
        return permissionMapper.selectList(permissionQuery);
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        // 获取用户角色
        List<Role> userRoles = getUserRoles(userId);
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());
        
        // 查询角色权限关联
        QueryWrapper<RolePermission> rolePermissionQuery = new QueryWrapper<>();
        rolePermissionQuery.in("role_id", roleIds);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionQuery);
        
        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取权限ID列表（去重）
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询权限信息
        QueryWrapper<Permission> permissionQuery = new QueryWrapper<>();
        permissionQuery.in("id", permissionIds)
                      .eq("deleted", 0);
        
        return permissionMapper.selectList(permissionQuery);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 获取用户权限
        List<Permission> userPermissions = getUserPermissions(userId);
        
        // 检查是否有指定权限
        return userPermissions.stream()
                .anyMatch(permission -> permissionCode.equals(permission.getPermissionCode()));
    }
}
