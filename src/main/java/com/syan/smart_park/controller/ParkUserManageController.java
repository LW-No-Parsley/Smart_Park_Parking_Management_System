package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.service.ParkUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 园区用户管理控制器（后台管理用）
 */
@RestController
@RequestMapping("/api/park-user-manage")
@RequiredArgsConstructor
public class ParkUserManageController {

    private final ParkUserService parkUserService;

    @GetMapping("/list")
    public R<List<ParkUserDTO>> getAllParkUsers() {
        return R.success(parkUserService.getAllParkUsers());
    }

    @GetMapping("/{id}")
    public R<ParkUserDTO> getParkUserById(@PathVariable Long id) {
        return R.success(parkUserService.getParkUserById(id));
    }

    @PostMapping
    public R<ParkUserDTO> createParkUser(@RequestBody ParkUser parkUser) {
        return R.success(parkUserService.createParkUser(parkUser));
    }

    @PutMapping("/{id}")
    public R<ParkUserDTO> updateParkUser(@PathVariable Long id, @RequestBody ParkUser parkUser) {
        return R.success(parkUserService.updateParkUser(id, parkUser));
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteParkUser(@PathVariable Long id) {
        parkUserService.deleteParkUser(id);
        return R.success();
    }

    @GetMapping("/status/{status}")
    public R<List<ParkUserDTO>> getParkUsersByStatus(@PathVariable Integer status) {
        return R.success(parkUserService.getParkUsersByStatus(status));
    }

    @GetMapping("/type/{userType}")
    public R<List<ParkUserDTO>> getParkUsersByType(@PathVariable Integer userType) {
        return R.success(parkUserService.getParkUsersByType(userType));
    }
}
