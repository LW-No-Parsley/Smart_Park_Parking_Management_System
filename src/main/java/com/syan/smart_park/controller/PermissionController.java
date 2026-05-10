package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.entity.Permission;
import com.syan.smart_park.entity.PermissionDTO;
import com.syan.smart_park.service.PermissionService;
import lombok.RequiredArgsConstructor;
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

    /**
     * 获取所有权限列表（平铺）
     */
    @GetMapping("/list")
    public R<List<PermissionDTO>> getAllPermissions() {
        return R.success(permissionService.getAllPermissions());
    }

    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    public R<List<PermissionDTO>> getPermissionTree() {
        return R.success(permissionService.getPermissionTree());
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    public R<PermissionDTO> getPermissionById(@PathVariable Long id) {
        PermissionDTO dto = permissionService.getPermissionById(id);
        return R.success(dto);
    }

    /**
     * 创建权限
     */
    @PostMapping
    public R<PermissionDTO> createPermission(@RequestBody Permission permission) {
        PermissionDTO dto = permissionService.createPermission(permission);
        return R.success(dto);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    public R<PermissionDTO> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        PermissionDTO dto = permissionService.updatePermission(id, permission);
        return R.success(dto);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public R<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return R.success();
    }

    /**
     * 获取用户的菜单树（根据角色权限过滤）
     */
    @GetMapping("/user-menus")
    public R<List<PermissionDTO>> getUserMenus(@RequestParam Long userId) {
        return R.success(permissionService.getUserMenuTree(userId));
    }
}
