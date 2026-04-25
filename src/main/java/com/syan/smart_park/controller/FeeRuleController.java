package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
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
     * 获取所有计费规则列表
     */
    @GetMapping("/list")
    public R<List<FeeRuleDTO>> getAllFeeRules() {
        List<FeeRuleDTO> feeRules = feeRuleService.getAllFeeRules();
        return R.success(feeRules);
    }

    /**
     * 根据ID获取计费规则详情
     */
    @GetMapping("/{id}")
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
    public R<Boolean> deleteFeeRule(@PathVariable Long id) {
        boolean result = feeRuleService.deleteFeeRule(id);
        if (!result) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(true);
    }

    /**
     * 根据园区ID获取计费规则列表
     */
    @GetMapping("/park-area/{parkAreaId}")
    public R<List<FeeRuleDTO>> getFeeRulesByParkAreaId(@PathVariable Long parkAreaId) {
        List<FeeRuleDTO> feeRules = feeRuleService.getFeeRulesByParkAreaId(parkAreaId);
        return R.success(feeRules);
    }

    /**
     * 根据状态获取计费规则列表
     */
    @GetMapping("/status/{status}")
    public R<List<FeeRuleDTO>> getFeeRulesByStatus(@PathVariable Integer status) {
        List<FeeRuleDTO> feeRules = feeRuleService.getFeeRulesByStatus(status);
        return R.success(feeRules);
    }

    /**
     * 计算停车费用
     */
    @GetMapping("/calculate")
    public R<FeeCalculationResult> calculateFee(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false, defaultValue = "1") Integer vehicleType,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        FeeCalculationResult result = feeRuleService.calculateFeeWithDetail(parkAreaId, vehicleType, startTime, endTime);
        return R.success(result);
    }
}
