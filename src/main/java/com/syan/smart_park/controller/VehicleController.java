package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.VehicleDTO;
import com.syan.smart_park.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆控制器
 */
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    /**
     * 统一查询车辆列表（支持多条件筛选 + 分页）
     *
     * @param userId      用户ID（可选）
     * @param plateNumber 车牌号（可选，支持模糊匹配）
     * @param status      车辆状态：0-禁用，1-正常（可选）
     * @param vehicleType 车辆类型：1-小车，2-大车，3-新能源车（可选）
     * @param isDefault   是否默认车辆：0-否，1-是（可选）
     * @param page        页码（默认1）
     * @param size        每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("vehicle:list")
    public R<PageResult<VehicleDTO>> listVehicles(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer vehicleType,
            @RequestParam(required = false) Integer isDefault,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<VehicleDTO> result = vehicleService.listVehicles(userId, plateNumber, status, vehicleType, isDefault, page, size);
        return R.success(result);
    }
    
    /**
     * 根据ID获取车辆详情
     */
    @GetMapping("/{id}")
    @RequirePermission("vehicle:list")
    public R<VehicleDTO> getVehicleById(@PathVariable Long id) {
        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        if (vehicle == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(vehicle);
    }
    
    /**
     * 创建车辆
     */
    @PostMapping
    @RequirePermission("vehicle:create")
    public R<VehicleDTO> createVehicle(@Valid @RequestBody VehicleDTO vehicleDTO) {
        VehicleDTO createdVehicle = vehicleService.createVehicle(vehicleDTO);
        if (createdVehicle == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdVehicle);
    }
    
    /**
     * 更新车辆
     */
    @PutMapping("/{id}")
    @RequirePermission("vehicle:update")
    public R<VehicleDTO> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleDTO vehicleDTO) {
        VehicleDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleDTO);
        if (updatedVehicle == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedVehicle);
    }
    
    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    @RequirePermission("vehicle:delete")
    public R<Boolean> deleteVehicle(@PathVariable Long id) {
        boolean result = vehicleService.deleteVehicle(id);
        if (!result) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }
    
    /**
     * 设置默认车辆
     */
    @PutMapping("/user/{userId}/set-default/{vehicleId}")
    @RequirePermission("vehicle:update")
    public R<Boolean> setDefaultVehicle(@PathVariable Long userId, @PathVariable Long vehicleId) {
        boolean result = vehicleService.setDefaultVehicle(userId, vehicleId);
        if (!result) {
            return R.error(ReturnCode.RC500); // 设置失败
        }
        return R.success(true);
    }
    
    /**
     * 批量更新车辆状态
     */
    @PutMapping("/batch/update-status")
    @RequirePermission("vehicle:update")
    public R<Boolean> batchUpdateVehicleStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean result = vehicleService.batchUpdateVehicleStatus(ids, status);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
}
