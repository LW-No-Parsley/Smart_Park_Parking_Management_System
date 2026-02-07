package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ParkingZoneDTO;
import com.syan.smart_park.service.ParkingZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车位分区控制器
 */
@RestController
@RequestMapping("/api/parking-zone")
@RequiredArgsConstructor
public class ParkingZoneController {

    private final ParkingZoneService parkingZoneService;

    /**
     * 获取所有车位分区列表
     *
     * @return 车位分区列表
     */
    @GetMapping("/list")
    public R<List<ParkingZoneDTO>> getAllParkingZones() {
        List<ParkingZoneDTO> parkingZones = parkingZoneService.getAllParkingZones();
        return R.success(parkingZones);
    }

    /**
     * 根据ID获取车位分区详情
     *
     * @param id 分区ID
     * @return 车位分区详情
     */
    @GetMapping("/{id}")
    public R<ParkingZoneDTO> getParkingZoneById(@PathVariable Long id) {
        ParkingZoneDTO parkingZoneDTO = parkingZoneService.getParkingZoneById(id);
        if (parkingZoneDTO == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(parkingZoneDTO);
    }

    /**
     * 创建车位分区
     *
     * @param parkingZoneDTO 车位分区信息
     * @return 创建的车位分区
     */
    @PostMapping
    public R<ParkingZoneDTO> createParkingZone(@Valid @RequestBody ParkingZoneDTO parkingZoneDTO) {
        ParkingZoneDTO createdParkingZone = parkingZoneService.createParkingZone(parkingZoneDTO);
        if (createdParkingZone == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdParkingZone);
    }

    /**
     * 更新车位分区信息
     *
     * @param id 分区ID
     * @param parkingZoneDTO 车位分区信息
     * @return 更新后的车位分区
     */
    @PutMapping("/{id}")
    public R<ParkingZoneDTO> updateParkingZone(@PathVariable Long id, @Valid @RequestBody ParkingZoneDTO parkingZoneDTO) {
        ParkingZoneDTO updatedParkingZone = parkingZoneService.updateParkingZone(id, parkingZoneDTO);
        if (updatedParkingZone == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在或更新失败
        }
        return R.success(updatedParkingZone);
    }

    /**
     * 删除车位分区
     *
     * @param id 分区ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteParkingZone(@PathVariable Long id) {
        boolean success = parkingZoneService.deleteParkingZone(id);
        if (!success) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 根据园区ID获取车位分区列表
     *
     * @param parkAreaId 园区ID
     * @return 车位分区列表
     */
    @GetMapping("/park-area/{parkAreaId}")
    public R<List<ParkingZoneDTO>> getParkingZonesByParkAreaId(@PathVariable Long parkAreaId) {
        List<ParkingZoneDTO> parkingZones = parkingZoneService.getParkingZonesByParkAreaId(parkAreaId);
        return R.success(parkingZones);
    }

    /**
     * 根据状态获取车位分区列表
     *
     * @param status 分区状态：0-禁用，1-启用
     * @return 车位分区列表
     */
    @GetMapping("/status/{status}")
    public R<List<ParkingZoneDTO>> getParkingZonesByStatus(@PathVariable Integer status) {
        List<ParkingZoneDTO> parkingZones = parkingZoneService.getParkingZonesByStatus(status);
        return R.success(parkingZones);
    }

    /**
     * 搜索车位分区（按分区名称）
     *
     * @param keyword 搜索关键词
     * @return 车位分区列表
     */
    @GetMapping("/search")
    public R<List<ParkingZoneDTO>> searchParkingZones(@RequestParam String keyword) {
        // 这里需要实现搜索逻辑，暂时返回所有车位分区
        // 实际项目中应该实现具体的搜索逻辑
        List<ParkingZoneDTO> parkingZones = parkingZoneService.getAllParkingZones();
        return R.success(parkingZones);
    }
}
