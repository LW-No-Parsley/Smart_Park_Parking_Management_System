package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.service.ParkAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 园区控制器
 */
@RestController
@RequestMapping("/api/park-area")
@RequiredArgsConstructor
public class ParkAreaController {

    private final ParkAreaService parkAreaService;

    /**
     * 获取所有园区列表
     *
     * @return 园区列表
     */
    @GetMapping("/list")
    public R<List<ParkAreaDTO>> getAllParkAreas() {
        List<ParkAreaDTO> parkAreas = parkAreaService.getAllParkAreas();
        return R.success(parkAreas);
    }

    /**
     * 根据ID获取园区详情
     *
     * @param id 园区ID
     * @return 园区详情
     */
    @GetMapping("/{id}")
    public R<ParkAreaDTO> getParkAreaById(@PathVariable Long id) {
        ParkAreaDTO parkAreaDTO = parkAreaService.getParkAreaById(id);
        if (parkAreaDTO == null) {
            return R.error(ReturnCode.RC601); // 园区不存在
        }
        return R.success(parkAreaDTO);
    }

    /**
     * 创建园区
     *
     * @param parkAreaDTO 园区信息
     * @return 创建的园区
     */
    @PostMapping
    public R<ParkAreaDTO> createParkArea(@Valid @RequestBody ParkAreaDTO parkAreaDTO) {
        ParkAreaDTO createdParkArea = parkAreaService.createParkArea(parkAreaDTO);
        if (createdParkArea == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdParkArea);
    }

    /**
     * 更新园区信息
     *
     * @param id 园区ID
     * @param parkAreaDTO 园区信息
     * @return 更新后的园区
     */
    @PutMapping("/{id}")
    public R<ParkAreaDTO> updateParkArea(@PathVariable Long id, @Valid @RequestBody ParkAreaDTO parkAreaDTO) {
        ParkAreaDTO updatedParkArea = parkAreaService.updateParkArea(id, parkAreaDTO);
        if (updatedParkArea == null) {
            return R.error(ReturnCode.RC601); // 园区不存在或更新失败
        }
        return R.success(updatedParkArea);
    }

    /**
     * 删除园区
     *
     * @param id 园区ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteParkArea(@PathVariable Long id) {
        boolean success = parkAreaService.deleteParkArea(id);
        if (!success) {
            return R.error(ReturnCode.RC601); // 园区不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 根据状态获取园区列表
     *
     * @param status 园区状态：0-关闭，1-开放
     * @return 园区列表
     */
    @GetMapping("/status/{status}")
    public R<List<ParkAreaDTO>> getParkAreasByStatus(@PathVariable Integer status) {
        List<ParkAreaDTO> parkAreas = parkAreaService.getParkAreasByStatus(status);
        return R.success(parkAreas);
    }

    /**
     * 搜索园区（按名称或地址）
     *
     * @param keyword 搜索关键词
     * @return 园区列表
     */
    @GetMapping("/search")
    public R<List<ParkAreaDTO>> searchParkAreas(@RequestParam String keyword) {
        // 这里需要实现搜索逻辑，暂时返回所有园区
        // 实际项目中应该实现具体的搜索逻辑
        List<ParkAreaDTO> parkAreas = parkAreaService.getAllParkAreas();
        return R.success(parkAreas);
    }
}
