package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
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
     * 统一查询车位列表（支持多条件筛选 + 分页）
     *
     * @param parkAreaId   园区ID（可选）
     * @param zoneId       分区ID（可选）
     * @param status       车位状态：0-禁用，1-正常，4-故障（可选）
     * @param spaceType    车位类型：1-固定，2-临时，3-访客，4-残障专用（可选）
     * @param bindUserId   绑定用户ID（可选）
     * @param available    是否仅查空闲车位（可选）
     * @param withOccupied 是否附带当前占用状态（默认true，传false可跳过以提升性能）
     * @param page         页码（默认1）
     * @param size         每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("park:space:list")
    public R<PageResult<ParkingSpaceDTO>> listParkingSpaces(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer spaceType,
            @RequestParam(required = false) Long bindUserId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Boolean withOccupied,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ParkingSpaceDTO> result = parkingSpaceService.listParkingSpaces(
                parkAreaId, zoneId, status, spaceType, bindUserId, available, withOccupied, page, size);
        return R.success(result);
    }
    
    /**
     * 根据ID获取车位详情
     */
    @GetMapping("/{id}")
    @RequirePermission("park:space:list")
    public R<ParkingSpaceDTO> getParkingSpaceById(@PathVariable Long id) {
        ParkingSpaceDTO parkingSpace = parkingSpaceService.getParkingSpaceById(id);
        if (parkingSpace == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(parkingSpace);
    }
    
    /**
     * 创建车位
     */
    @PostMapping
    @RequirePermission("park:space:create")
    public R<ParkingSpaceDTO> createParkingSpace(@Valid @RequestBody ParkingSpaceDTO parkingSpaceDTO) {
        ParkingSpaceDTO createdParkingSpace = parkingSpaceService.createParkingSpace(parkingSpaceDTO);
        if (createdParkingSpace == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdParkingSpace);
    }

    /**
     * 批量创建车位请求体
     */
    @lombok.Data
    public static class BatchCreateRequest {
        @jakarta.validation.constraints.NotNull(message = "车位模板不能为空")
        private ParkingSpaceDTO template;
        @jakarta.validation.constraints.NotBlank(message = "编号前缀不能为空")
        private String prefix;
        private int startNumber = 1;
        @jakarta.validation.constraints.Min(1)
        @jakarta.validation.constraints.Max(200)
        private int count = 10;
    }

    /**
     * 批量创建车位
     */
    @PostMapping("/batch")
    @RequirePermission("park:space:create")
    public R<List<ParkingSpaceDTO>> batchCreateParkingSpaces(@Valid @RequestBody BatchCreateRequest request) {
        List<ParkingSpaceDTO> result = parkingSpaceService.batchCreateParkingSpaces(
                request.getTemplate(), request.getPrefix(), request.getStartNumber(), request.getCount());
        return R.success(result);
    }

    /**
     * 更新车位
     */
    @PutMapping("/{id}")
    @RequirePermission("park:space:update")
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
    @RequirePermission("park:space:delete")
    public R<Boolean> deleteParkingSpace(@PathVariable Long id) {
        boolean result = parkingSpaceService.deleteParkingSpace(id);
        if (!result) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }
    
    /**
     * 批量更新车位状态
     */
    @PutMapping("/batch/update-status")
    @RequirePermission("park:space:update")
    public R<Boolean> batchUpdateParkingSpaceStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean result = parkingSpaceService.batchUpdateParkingSpaceStatus(ids, status);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
    
    /**
     * 获取车位详情（包含当前占用状态）
     */
    @GetMapping("/{id}/with-occupied-status")
    @RequirePermission("park:space:list")
    public R<ParkingSpaceDTO> getParkingSpaceWithOccupiedStatus(@PathVariable Long id) {
        ParkingSpaceDTO parkingSpace = parkingSpaceService.getParkingSpaceWithOccupiedStatus(id);
        if (parkingSpace == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(parkingSpace);
    }
    
    /**
     * 检查车位在当前时间是否被占用
     */
    @GetMapping("/{id}/is-occupied")
    @RequirePermission("park:space:list")
    public R<Boolean> isSpaceOccupied(@PathVariable Long id) {
        boolean isOccupied = parkingSpaceService.isSpaceOccupied(id);
        return R.success(isOccupied);
    }
    
    /**
     * 获取可用于车辆绑定（长期占用）的车位列表
     * 预约产生的临时占用不影响绑定选择
     */
    @GetMapping("/available-for-binding")
    @RequirePermission("park:space:list")
    public R<List<ParkingSpaceDTO>> getSpacesAvailableForBinding() {
        List<ParkingSpaceDTO> parkingSpaces = parkingSpaceService.getSpacesAvailableForBinding();
        return R.success(parkingSpaces);
    }
}
