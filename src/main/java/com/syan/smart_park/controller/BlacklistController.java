package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
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
     * 统一查询黑名单列表（支持多条件筛选 + 分页）
     *
     * @param plateNumber 车牌号（可选，精确匹配）
     * @param status      状态：0-禁用，1-生效（可选）
     * @param createdBy   创建人ID（可选）
     * @param keyword     搜索关键词，按车牌号/原因模糊搜索（可选）
     * @param expired     是否已过期：true=已过期，false/null=不过滤（可选）
     * @param page        页码（默认1）
     * @param size        每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("blacklist:list")
    public R<PageResult<BlacklistDTO>> listBlacklists(
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean expired,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<BlacklistDTO> result = blacklistService.listBlacklists(plateNumber, status, createdBy, keyword, expired, page, size);
        return R.success(result);
    }

    /**
     * 根据ID获取黑名单记录详情
     */
    @GetMapping("/{id}")
    @RequirePermission("blacklist:list")
    public R<BlacklistDTO> getBlacklistById(@PathVariable Long id) {
        BlacklistDTO blacklistDTO = blacklistService.getBlacklistById(id);
        if (blacklistDTO == null) {
            return R.error(ReturnCode.RC1300);
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
    @RequirePermission("blacklist:create")
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
    @RequirePermission("blacklist:update")
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
    @RequirePermission("blacklist:delete")
    public R<Boolean> deleteBlacklist(@PathVariable Long id) {
        boolean success = blacklistService.deleteBlacklist(id);
        if (!success) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 检查车牌号是否在黑名单中
     *
     * @param plateNumber 车牌号
     * @return 检查结果
     */
    @GetMapping("/check/{plateNumber}")
    @RequirePermission("blacklist:list")
    public R<Boolean> isPlateNumberInBlacklist(@PathVariable String plateNumber) {
        boolean isInBlacklist = blacklistService.isPlateNumberInBlacklist(plateNumber);
        return R.success(isInBlacklist);
    }

    /**
     * 批量更新黑名单状态
     *
     * @param ids 黑名单ID列表
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    @RequirePermission("blacklist:update")
    public R<Boolean> batchUpdateStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean success = blacklistService.batchUpdateStatus(ids, status);
        if (!success) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }

}
