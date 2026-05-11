package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;

import com.syan.smart_park.entity.Permission;
import com.syan.smart_park.entity.PermissionDTO;
import com.syan.smart_park.service.PermissionService;
import com.syan.smart_park.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final RoleService roleService;

    /**
     * 获取所有权限列表（平铺）
     */
    @GetMapping("/list")
    @RequirePermission("system:permission:list")
    public R<List<PermissionDTO>> getAllPermissions() {
        return R.success(permissionService.getAllPermissions());
    }

    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    @RequirePermission("system:permission:list")
    public R<List<PermissionDTO>> getPermissionTree() {
        return R.success(permissionService.getPermissionTree());
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    @RequirePermission("system:permission:list")
    public R<PermissionDTO> getPermissionById(@PathVariable Long id) {
        PermissionDTO dto = permissionService.getPermissionById(id);
        return R.success(dto);
    }

    /**
     * 创建权限
     */
    @PostMapping
    @RequirePermission("system:permission:create")
    public R<PermissionDTO> createPermission(@Valid @RequestBody Permission permission) {
        // 防止客户端传入敏感字段（Mass Assignment）
        permission.setId(null);
        permission.setStatus(null);
        permission.setDeleted(null);
        PermissionDTO dto = permissionService.createPermission(permission);
        return R.success(dto);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @RequirePermission("system:permission:update")
    public R<PermissionDTO> updatePermission(@PathVariable Long id, @Valid @RequestBody Permission permission) {
        // 防止客户端修改敏感字段
        permission.setStatus(null);
        permission.setDeleted(null);
        PermissionDTO dto = permissionService.updatePermission(id, permission);
        return R.success(dto);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:permission:delete")
    public R<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return R.success();
    }

    /**
     * 获取当前登录用户的菜单树（无需额外权限，任何已认证用户均可访问）
     */
    @GetMapping("/user-menus")
    public R<List<PermissionDTO>> getUserMenus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return R.success(List.of());
        }
        return R.success(permissionService.getUserMenuTree(userId));
    }

    /**
     * 获取当前登录用户拥有的所有权限编码
     * 用于前端按钮级权限控制（v-if="hasPermission('xxx')"）
     */
    @GetMapping("/my-codes")
    public R<List<String>> getMyPermissionCodes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return R.success(List.of());
        }
        List<Permission> permissions = roleService.getUserPermissions(userId);
        List<String> codes = permissions.stream()
                .map(Permission::getPermissionCode)
                .toList();
        return R.success(codes);
    }
}
