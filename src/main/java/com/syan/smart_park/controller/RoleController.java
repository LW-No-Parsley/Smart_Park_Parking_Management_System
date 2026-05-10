package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取所有角色列表
     */
    @GetMapping("/list")
    @RequirePermission("system:role:list")
    public R<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> dtos = roleService.getAllRoles().stream()
                .map(RoleDTO::fromRole)
                .collect(Collectors.toList());
        return R.success(dtos);
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    @RequirePermission("system:role:list")
    public R<RoleDTO> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return R.success(RoleDTO.fromRole(role));
    }

    /**
     * 创建角色
     */
    @PostMapping
    @RequirePermission("system:role:create")
    public R<RoleDTO> createRole(@RequestBody Role role) {
        roleService.createRole(role);
        return R.success(RoleDTO.fromRole(role));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    public R<RoleDTO> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return R.success(RoleDTO.fromRole(role));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:role:delete")
    public R<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.success();
    }

    /**
     * 根据状态获取角色列表
     */
    @GetMapping("/status/{status}")
    @RequirePermission("system:role:list")
    public R<List<RoleDTO>> getRolesByStatus(@PathVariable Integer status) {
        List<RoleDTO> dtos = roleService.getRolesByStatus(status).stream()
                .map(RoleDTO::fromRole)
                .collect(Collectors.toList());
        return R.success(dtos);
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @RequirePermission("system:role:list")
    public R<List<Long>> getRolePermissionIds(@PathVariable Long id) {
        return R.success(roleService.getRolePermissionIds(id));
    }

    /**
     * 批量设置角色的权限（全量覆盖）
     */
    @PutMapping("/{id}/permissions")
    @RequirePermission("system:role:assign-permission")
    public R<Void> assignPermissionsToRole(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissionsToRole(id, permissionIds);
        return R.success();
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/user/{userId}")
    @RequirePermission("system:role:list")
    public R<List<RoleDTO>> getUserRoles(@PathVariable Long userId) {
        List<RoleDTO> dtos = roleService.getUserRoles(userId).stream()
                .map(RoleDTO::fromRole)
                .collect(Collectors.toList());
        return R.success(dtos);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/user/{userId}/assign/{roleId}")
    @RequirePermission("system:role:assign-user")
    public R<Void> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        roleService.assignRoleToUser(userId, roleId);
        return R.success();
    }

    /**
     * 移除用户的角色
     */
    @DeleteMapping("/user/{userId}/assign/{roleId}")
    @RequirePermission("system:role:assign-user")
    public R<Void> removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId) {
        roleService.removeRoleFromUser(userId, roleId);
        return R.success();
    }

    /**
     * 获取用户的所有权限（合并多个角色）
     */
    @GetMapping("/user/{userId}/permissions")
    @RequirePermission("system:role:list")
    public R<List<PermissionDTO>> getUserPermissions(@PathVariable Long userId) {
        List<PermissionDTO> dtos = roleService.getUserPermissions(userId).stream()
                .map(PermissionDTO::fromPermission)
                .collect(Collectors.toList());
        return R.success(dtos);
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check-permission")
    @RequirePermission("system:role:list")
    public R<Boolean> checkPermission(@RequestParam Long userId, @RequestParam String permissionCode) {
        return R.success(roleService.hasPermission(userId, permissionCode));
    }
}
