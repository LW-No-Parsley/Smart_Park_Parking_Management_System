package com.syan.smart_park.controller;

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
     * 获取所有车辆列表
     */
    @GetMapping("/list")
    @RequirePermission("vehicle:list")
    public R<List<VehicleDTO>> getAllVehicles() {
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles();
        return R.success(vehicles);
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
     * 根据用户ID获取车辆列表
     */
    @GetMapping("/user/{userId}")
    @RequirePermission("vehicle:list")
    public R<List<VehicleDTO>> getVehiclesByUserId(@PathVariable Long userId) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByUserId(userId);
        return R.success(vehicles);
    }
    
    /**
     * 根据车牌号获取车辆
     */
    @GetMapping("/plate-number/{plateNumber}")
    @RequirePermission("vehicle:list")
    public R<VehicleDTO> getVehicleByPlateNumber(@PathVariable String plateNumber) {
        VehicleDTO vehicle = vehicleService.getVehicleByPlateNumber(plateNumber);
        if (vehicle == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(vehicle);
    }
    
    /**
     * 根据车辆状态获取车辆列表
     */
    @GetMapping("/status/{status}")
    @RequirePermission("vehicle:list")
    public R<List<VehicleDTO>> getVehiclesByStatus(@PathVariable Integer status) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByStatus(status);
        return R.success(vehicles);
    }
    
    /**
     * 根据车辆类型获取车辆列表
     */
    @GetMapping("/type/{vehicleType}")
    @RequirePermission("vehicle:list")
    public R<List<VehicleDTO>> getVehiclesByType(@PathVariable Integer vehicleType) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByType(vehicleType);
        return R.success(vehicles);
    }
    
    /**
     * 获取用户的默认车辆
     */
    @GetMapping("/user/{userId}/default")
    @RequirePermission("vehicle:list")
    public R<VehicleDTO> getDefaultVehicleByUserId(@PathVariable Long userId) {
        VehicleDTO vehicle = vehicleService.getDefaultVehicleByUserId(userId);
        if (vehicle == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(vehicle);
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
