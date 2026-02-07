package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.BlacklistDTO;
import com.syan.smart_park.service.BlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 黑名单控制器
 */
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 获取所有黑名单记录
     *
     * @return 黑名单记录列表
     */
    @GetMapping("/list")
    public R<List<BlacklistDTO>> getAllBlacklists() {
        List<BlacklistDTO> blacklists = blacklistService.getAllBlacklists();
        return R.success(blacklists);
    }

    /**
     * 根据ID获取黑名单记录详情
     *
     * @param id 黑名单ID
     * @return 黑名单记录详情
     */
    @GetMapping("/{id}")
    public R<BlacklistDTO> getBlacklistById(@PathVariable Long id) {
        BlacklistDTO blacklistDTO = blacklistService.getBlacklistById(id);
        if (blacklistDTO == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(blacklistDTO);
    }

    /**
     * 创建黑名单记录
     *
     * @param blacklistDTO 黑名单记录信息
     * @return 创建的黑名单记录
     */
    @PostMapping
    public R<BlacklistDTO> createBlacklist(@Valid @RequestBody BlacklistDTO blacklistDTO) {
        BlacklistDTO createdBlacklist = blacklistService.createBlacklist(blacklistDTO);
        if (createdBlacklist == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdBlacklist);
    }

    /**
     * 更新黑名单记录信息
     *
     * @param id 黑名单ID
     * @param blacklistDTO 黑名单记录信息
     * @return 更新后的黑名单记录
     */
    @PutMapping("/{id}")
    public R<BlacklistDTO> updateBlacklist(@PathVariable Long id, @Valid @RequestBody BlacklistDTO blacklistDTO) {
        BlacklistDTO updatedBlacklist = blacklistService.updateBlacklist(id, blacklistDTO);
        if (updatedBlacklist == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在或更新失败
        }
        return R.success(updatedBlacklist);
    }

    /**
     * 删除黑名单记录
     *
     * @param id 黑名单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteBlacklist(@PathVariable Long id) {
        boolean success = blacklistService.deleteBlacklist(id);
        if (!success) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 根据车牌号查询黑名单记录
     *
     * @param plateNumber 车牌号
     * @return 黑名单记录列表
     */
    @GetMapping("/plate-number/{plateNumber}")
    public R<List<BlacklistDTO>> getBlacklistsByPlateNumber(@PathVariable String plateNumber) {
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistsByPlateNumber(plateNumber);
        return R.success(blacklists);
    }

    /**
     * 根据状态查询黑名单记录
     *
     * @param status 状态：0-禁用，1-生效
     * @return 黑名单记录列表
     */
    @GetMapping("/status/{status}")
    public R<List<BlacklistDTO>> getBlacklistsByStatus(@PathVariable Integer status) {
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistsByStatus(status);
        return R.success(blacklists);
    }

    /**
     * 检查车牌号是否在黑名单中
     *
     * @param plateNumber 车牌号
     * @return 检查结果
     */
    @GetMapping("/check/{plateNumber}")
    public R<Boolean> isPlateNumberInBlacklist(@PathVariable String plateNumber) {
        boolean isInBlacklist = blacklistService.isPlateNumberInBlacklist(plateNumber);
        return R.success(isInBlacklist);
    }

    /**
     * 获取当前生效的黑名单记录
     *
     * @return 生效的黑名单记录列表
     */
    @GetMapping("/active")
    public R<List<BlacklistDTO>> getActiveBlacklists() {
        List<BlacklistDTO> blacklists = blacklistService.getActiveBlacklists();
        return R.success(blacklists);
    }

    /**
     * 获取已过期的黑名单记录
     *
     * @return 已过期的黑名单记录列表
     */
    @GetMapping("/expired")
    public R<List<BlacklistDTO>> getExpiredBlacklists() {
        List<BlacklistDTO> blacklists = blacklistService.getExpiredBlacklists();
        return R.success(blacklists);
    }

    /**
     * 批量更新黑名单状态
     *
     * @param ids 黑名单ID列表
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    public R<Boolean> batchUpdateStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean success = blacklistService.batchUpdateStatus(ids, status);
        if (!success) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }

    /**
     * 根据创建人查询黑名单记录
     *
     * @param createdBy 创建人ID
     * @return 黑名单记录列表
     */
    @GetMapping("/created-by/{createdBy}")
    public R<List<BlacklistDTO>> getBlacklistsByCreatedBy(@PathVariable Long createdBy) {
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistsByCreatedBy(createdBy);
        return R.success(blacklists);
    }

    /**
     * 搜索黑名单记录
     *
     * @param keyword 搜索关键词
     * @return 黑名单记录列表
     */
    @GetMapping("/search")
    public R<List<BlacklistDTO>> searchBlacklists(@RequestParam String keyword) {
        List<BlacklistDTO> blacklists = blacklistService.searchBlacklists(keyword);
        return R.success(blacklists);
    }
}
