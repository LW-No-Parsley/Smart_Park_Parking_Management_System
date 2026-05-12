package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
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
     * 统一查询园区列表（支持多条件筛选 + 分页）
     *
     * @param status  园区状态：0-关闭，1-开放（可选）
     * @param keyword 搜索关键词，按名称或地址模糊搜索（可选）
     * @param page    页码（默认1）
     * @param size    每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("park:area:list")
    public R<PageResult<ParkAreaDTO>> listParkAreas(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ParkAreaDTO> result = parkAreaService.listParkAreas(status, keyword, page, size);
        return R.success(result);
    }

    /**
     * 根据ID获取园区详情
     *
     * @param id 园区ID
     * @return 园区详情
     */
    @GetMapping("/{id}")
    @RequirePermission("park:area:list")
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
    @RequirePermission("park:area:create")
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
    @RequirePermission("park:area:update")
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
    @RequirePermission("park:area:delete")
    public R<Boolean> deleteParkArea(@PathVariable Long id) {
        boolean success = parkAreaService.deleteParkArea(id);
        if (!success) {
            return R.error(ReturnCode.RC601); // 园区不存在或删除失败
        }
        return R.success();
    }

    /**
     * 手动触发更新所有园区的总车位数
     *
     * @return 更新的园区数量
     */
    @PostMapping("/update-all-total-spaces")
    @RequirePermission("park:area:update")
    public R<Integer> updateAllTotalSpaces() {
        int updatedCount = parkAreaService.updateAllTotalSpaces();
        return R.success(updatedCount);
    }

    /**
     * 更新指定园区的总车位数
     *
     * @param id 园区ID
     * @return 更新结果
     */
    @PostMapping("/{id}/update-total-spaces")
    @RequirePermission("park:area:update")
    public R<Boolean> updateTotalSpaces(@PathVariable Long id) {
        boolean success = parkAreaService.updateTotalSpaces(id);
        if (!success) {
            return R.error(ReturnCode.RC601); // 园区不存在或更新失败
        }
        return R.success();
    }
    
    /**
     * 获取园区占用统计信息
     *
     * @param id 园区ID
     * @return 占用统计信息
     */
    @GetMapping("/{id}/occupancy-stats")
    @RequirePermission("park:area:list")
    public R<com.syan.smart_park.entity.ParkAreaOccupancyStats> getParkAreaOccupancyStats(@PathVariable Long id) {
        com.syan.smart_park.entity.ParkAreaOccupancyStats stats = parkAreaService.getParkAreaOccupancyStats(id);
        if (stats == null) {
            return R.error(ReturnCode.RC601); // 园区不存在
        }
        return R.success(stats);
    }
    
    /**
     * 获取所有园区的占用统计信息
     *
     * @return 所有园区的占用统计信息列表
     */
    @GetMapping("/occupancy-stats/all")
    @RequirePermission("park:area:list")
    public R<List<com.syan.smart_park.entity.ParkAreaOccupancyStats>> getAllParkAreasOccupancyStats() {
        List<com.syan.smart_park.entity.ParkAreaOccupancyStats> statsList = parkAreaService.getAllParkAreasOccupancyStats();
        return R.success(statsList);
    }
}
