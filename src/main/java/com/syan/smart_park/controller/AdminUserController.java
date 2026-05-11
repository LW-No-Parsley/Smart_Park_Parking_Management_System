package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.entity.AdminUserDTO;
import com.syan.smart_park.entity.User;
import com.syan.smart_park.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin-user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/list")
    @RequirePermission("system:user:list")
    public R<List<AdminUserDTO>> getAllUsers() {
        List<AdminUserDTO> dtos = userService.getAllUsers().stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
        return R.success(dtos);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:list")
    public R<AdminUserDTO> getUserById(@PathVariable Long id) {
        return R.success(AdminUserDTO.fromUser(userService.getUserById(id)));
    }

    @PostMapping
    @RequirePermission("system:user:create")
    public R<AdminUserDTO> createUser(@Valid @RequestBody User user) {
        // 防止客户端传入敏感字段（Mass Assignment）
        user.setId(null);
        user.setStatus(null);
        user.setDeleted(null);
        return R.success(AdminUserDTO.fromUser(userService.createUser(user)));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:update")
    public R<AdminUserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        // 防止客户端修改敏感字段
        user.setStatus(null);
        user.setDeleted(null);
        return R.success(AdminUserDTO.fromUser(userService.updateUser(id, user)));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.success();
    }

    @GetMapping("/status/{status}")
    @RequirePermission("system:user:list")
    public R<List<AdminUserDTO>> getUsersByStatus(@PathVariable Integer status) {
        List<AdminUserDTO> dtos = userService.getUsersByStatus(status).stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
        return R.success(dtos);
    }
}
