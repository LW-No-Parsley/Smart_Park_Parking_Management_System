package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ParkingSpaceDTO;
import com.syan.smart_park.service.ParkingSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车位控制器
 */
@RestController
@RequestMapping("/api/parking-space")
@RequiredArgsConstructor
public class ParkingSpaceController {
    
    private final ParkingSpaceService parkingSpaceService;
    
    /**
     * 获取所有车位列表
     */
    @GetMapping("/list")
    public R<List<ParkingSpaceDTO>> getAllParkingSpaces() {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getAllParkingSpaces();
        return R.success(parkingSpaces);
    }
    
    /**
     * 根据ID获取车位详情
     */
    @GetMapping("/{id}")
    public R<ParkingSpaceDTO> getParkingSpaceById(@PathVariable Long id) {
        ParkingSpaceDTO parkingSpace = parkingSpaceService.getParkingSpaceById(id);
        if (parkingSpace == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(parkingSpace);
    }
    
    /**
     * 创建车位
     */
    @PostMapping
    public R<ParkingSpaceDTO> createParkingSpace(@Valid @RequestBody ParkingSpaceDTO parkingSpaceDTO) {
        ParkingSpaceDTO createdParkingSpace = parkingSpaceService.createParkingSpace(parkingSpaceDTO);
        if (createdParkingSpace == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdParkingSpace);
    }
    
    /**
     * 更新车位
     */
    @PutMapping("/{id}")
    public R<ParkingSpaceDTO> updateParkingSpace(@PathVariable Long id, @Valid @RequestBody ParkingSpaceDTO parkingSpaceDTO) {
        ParkingSpaceDTO updatedParkingSpace = parkingSpaceService.updateParkingSpace(id, parkingSpaceDTO);
        if (updatedParkingSpace == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedParkingSpace);
    }
    
    /**
     * 删除车位
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteParkingSpace(@PathVariable Long id) {
        boolean result = parkingSpaceService.deleteParkingSpace(id);
        if (!result) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }
    
    /**
     * 根据园区ID获取车位列表
     */
    @GetMapping("/park-area/{parkAreaId}")
    public R<List<ParkingSpaceDTO>> getParkingSpacesByParkAreaId(@PathVariable Long parkAreaId) {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getParkingSpacesByParkAreaId(parkAreaId);
        return R.success(parkingSpaces);
    }
    
    /**
     * 根据分区ID获取车位列表
     */
    @GetMapping("/zone/{zoneId}")
    public R<List<ParkingSpaceDTO>> getParkingSpacesByZoneId(@PathVariable Long zoneId) {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getParkingSpacesByZoneId(zoneId);
        return R.success(parkingSpaces);
    }
    
    /**
     * 根据车位状态获取车位列表
     */
    @GetMapping("/status/{status}")
    public R<List<ParkingSpaceDTO>> getParkingSpacesByStatus(@PathVariable Integer status) {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getParkingSpacesByStatus(status);
        return R.success(parkingSpaces);
    }
    
    /**
     * 根据车位类型获取车位列表
     */
    @GetMapping("/type/{spaceType}")
    public R<List<ParkingSpaceDTO>> getParkingSpacesByType(@PathVariable Integer spaceType) {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getParkingSpacesByType(spaceType);
        return R.success(parkingSpaces);
    }
    
    /**
     * 根据绑定用户ID获取车位列表
     */
    @GetMapping("/bind-user/{bindUserId}")
    public R<List<ParkingSpaceDTO>> getParkingSpacesByBindUserId(@PathVariable Long bindUserId) {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getParkingSpacesByBindUserId(bindUserId);
        return R.success(parkingSpaces);
    }
    
    /**
     * 获取空闲车位列表
     */
    @GetMapping("/available")
    public R<List<ParkingSpaceDTO>> getAvailableParkingSpaces() {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getAvailableParkingSpaces();
        return R.success(parkingSpaces);
    }
    
    /**
     * 批量更新车位状态
     */
    @PutMapping("/batch-update-status")
    public R<Boolean> batchUpdateParkingSpaceStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean result = parkingSpaceService.batchUpdateParkingSpaceStatus(ids, status);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
}
