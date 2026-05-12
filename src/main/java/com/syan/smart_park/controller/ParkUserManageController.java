package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.service.ParkUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 园区用户管理控制器（后台管理用）
 */
@RestController
@RequestMapping("/api/park-user-manage")
@RequiredArgsConstructor
public class ParkUserManageController {

    private final ParkUserService parkUserService;

    /**
     * 统一查询园区用户列表（支持多条件筛选 + 分页）
     *
     * @param status   用户状态：0-禁用，1-正常（可选）
     * @param userType 用户类型：1-车主，2-访客（可选）
     * @param page     页码（默认1）
     * @param size     每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("system:user:list")
    public R<PageResult<ParkUserDTO>> listParkUsers(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer userType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ParkUserDTO> result = parkUserService.listParkUsers(status, userType, page, size);
        return R.success(result);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:list")
    public R<ParkUserDTO> getParkUserById(@PathVariable Long id) {
        return R.success(parkUserService.getParkUserById(id));
    }

    @PostMapping
    @RequirePermission("system:user:create")
    public R<ParkUserDTO> createParkUser(@Valid @RequestBody ParkUser parkUser) {
        parkUser.setId(null);
        parkUser.setStatus(null);
        parkUser.setDeleted(null);
        parkUser.setUserType(null);
        return R.success(parkUserService.createParkUser(parkUser));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:update")
    public R<ParkUserDTO> updateParkUser(@PathVariable Long id, @Valid @RequestBody ParkUser parkUser) {
        parkUser.setDeleted(null);
        parkUser.setUserType(null);
        return R.success(parkUserService.updateParkUser(id, parkUser));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public R<Void> deleteParkUser(@PathVariable Long id) {
        parkUserService.deleteParkUser(id);
        return R.success();
    }
}
