package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
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
     * 统一查询车位分区列表（支持多条件筛选 + 分页）
     *
     * @param parkAreaId 园区ID（可选）
     * @param status     分区状态：0-禁用，1-启用（可选）
     * @param keyword    搜索关键词，按分区名称模糊搜索（可选）
     * @param page       页码（默认1）
     * @param size       每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("park:zone:list")
    public R<PageResult<ParkingZoneDTO>> listParkingZones(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ParkingZoneDTO> result = parkingZoneService.listParkingZones(parkAreaId, status, keyword, page, size);
        return R.success(result);
    }

    /**
     * 根据ID获取车位分区详情
     */
    @GetMapping("/{id}")
    @RequirePermission("park:zone:list")
    public R<ParkingZoneDTO> getParkingZoneById(@PathVariable Long id) {
        ParkingZoneDTO parkingZoneDTO = parkingZoneService.getParkingZoneById(id);
        if (parkingZoneDTO == null) {
            return R.error(ReturnCode.RC1300);
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
    @RequirePermission("park:zone:create")
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
    @RequirePermission("park:zone:update")
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
    @RequirePermission("park:zone:delete")
    public R<Boolean> deleteParkingZone(@PathVariable Long id) {
        boolean success = parkingZoneService.deleteParkingZone(id);
        if (!success) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 批量更新分区状态
     *
     * @param ids 分区ID列表
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    @RequirePermission("park:zone:update")
    public R<Boolean> batchUpdateParkingZoneStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean success = parkingZoneService.batchUpdateParkingZoneStatus(ids, status);
        if (!success) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }

    /**
     * 批量删除分区
     *
     * @param ids 分区ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @RequirePermission("park:zone:delete")
    public R<Boolean> batchDeleteParkingZones(@RequestParam List<Long> ids) {
        boolean success = parkingZoneService.batchDeleteParkingZones(ids);
        if (!success) {
            return R.error(ReturnCode.RC500); // 批量删除失败
        }
        return R.success(true);
    }
}
