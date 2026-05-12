package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.FeeCalculationResult;
import com.syan.smart_park.entity.FeeRuleDTO;
import com.syan.smart_park.service.FeeRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车计费规则控制器
 */
@RestController
@RequestMapping("/api/fee-rule")
@RequiredArgsConstructor
public class FeeRuleController {

    private final FeeRuleService feeRuleService;

    /**
     * 统一查询计费规则列表（支持多条件筛选 + 分页）
     *
     * @param parkAreaId 园区ID（可选）
     * @param status     状态：0-禁用，1-启用（可选）
     * @param page       页码（默认1）
     * @param size       每页大小（默认10）
     */
    @GetMapping("/list")
    @RequirePermission("fee:list")
    public R<PageResult<FeeRuleDTO>> listFeeRules(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<FeeRuleDTO> result = feeRuleService.listFeeRules(parkAreaId, status, page, size);
        return R.success(result);
    }

    /**
     * 根据ID获取计费规则详情
     */
    @GetMapping("/{id}")
    @RequirePermission("fee:list")
    public R<FeeRuleDTO> getFeeRuleById(@PathVariable Long id) {
        FeeRuleDTO feeRule = feeRuleService.getFeeRuleById(id);
        if (feeRule == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(feeRule);
    }

    /**
     * 创建计费规则
     */
    @PostMapping
    @RequirePermission("fee:create")
    public R<FeeRuleDTO> createFeeRule(@Valid @RequestBody FeeRuleDTO feeRuleDTO) {
        FeeRuleDTO created = feeRuleService.createFeeRule(feeRuleDTO);
        if (created == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(created);
    }

    /**
     * 更新计费规则
     */
    @PutMapping("/{id}")
    @RequirePermission("fee:update")
    public R<FeeRuleDTO> updateFeeRule(@PathVariable Long id, @Valid @RequestBody FeeRuleDTO feeRuleDTO) {
        FeeRuleDTO updated = feeRuleService.updateFeeRule(id, feeRuleDTO);
        if (updated == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(updated);
    }

    /**
     * 删除计费规则
     */
    @DeleteMapping("/{id}")
    @RequirePermission("fee:delete")
    public R<Boolean> deleteFeeRule(@PathVariable Long id) {
        boolean result = feeRuleService.deleteFeeRule(id);
        if (!result) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(true);
    }

    /**
     * 计算停车费用
     */
    @GetMapping("/calculate")
    @RequirePermission("fee:list")
    public R<FeeCalculationResult> calculateFee(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false, defaultValue = "1") Integer vehicleType,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        FeeCalculationResult result = feeRuleService.calculateFeeWithDetail(parkAreaId, vehicleType, startTime, endTime);
        return R.success(result);
    }
}
