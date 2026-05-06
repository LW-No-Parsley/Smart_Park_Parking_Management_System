package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.AccessLogDTO;
import com.syan.smart_park.service.AccessLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 进出记录控制器
 */
@RestController
@RequestMapping("/api/access-log")
@RequiredArgsConstructor
public class AccessLogController {
    
    private final AccessLogService accessLogService;

    /**
     * 统一分页查询进出记录列表（支持多条件组合筛选）
     * 合并了原 /park-area/* /gate/* /plate-number/* /vehicle/* /access-type/*
     *       /recognition-result/* /handled-by/* /time-range /exception /recent 等路由
     */
    @GetMapping("/list")
    public R<PageResult<AccessLogDTO>> getAccessLogList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) Long gateId,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Integer accessType,
            @RequestParam(required = false) Integer recognitionResult,
            @RequestParam(required = false) Long handledBy,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Boolean exceptionOnly) {
        PageResult<AccessLogDTO> result = accessLogService.pageAccessLogs(
                current, size, parkAreaId, gateId, plateNumber, vehicleId,
                accessType, recognitionResult, handledBy,
                startTime, endTime, exceptionOnly);
        return R.success(result);
    }
    
    /**
     * 根据ID获取进出记录详情
     */
    @GetMapping("/{id}")
    public R<AccessLogDTO> getAccessLogById(@PathVariable Long id) {
        AccessLogDTO accessLog = accessLogService.getAccessLogById(id);
        if (accessLog == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(accessLog);
    }
    
    /**
     * 创建进出记录
     */
    @PostMapping
    public R<AccessLogDTO> createAccessLog(@Valid @RequestBody AccessLogDTO accessLogDTO) {
        AccessLogDTO createdAccessLog = accessLogService.createAccessLog(accessLogDTO);
        if (createdAccessLog == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdAccessLog);
    }
    
    /**
     * 更新进出记录
     */
    @PutMapping("/{id}")
    public R<AccessLogDTO> updateAccessLog(@PathVariable Long id, @Valid @RequestBody AccessLogDTO accessLogDTO) {
        AccessLogDTO updatedAccessLog = accessLogService.updateAccessLog(id, accessLogDTO);
        if (updatedAccessLog == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedAccessLog);
    }
    
    /**
     * 获取今日进出记录统计
     */
    @GetMapping("/today-statistics")
    public R<AccessLogService.AccessLogStatistics> getTodayAccessLogStatistics(@RequestParam(required = false) Long parkAreaId) {
        AccessLogService.AccessLogStatistics statistics = accessLogService.getTodayAccessLogStatistics(parkAreaId);
        return R.success(statistics);
    }
    
    /**
     * 获取进出记录趋势数据
     */
    @GetMapping("/trend")
    public R<List<AccessLogService.AccessLogTrend>> getAccessLogTrend(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) Long parkAreaId) {
        List<AccessLogService.AccessLogTrend> trends = accessLogService.getAccessLogTrend(startTime, endTime, parkAreaId);
        return R.success(trends);
    }
    
    /**
     * 批量创建进出记录
     */
    @PostMapping("/batch")
    public R<Boolean> batchCreateAccessLogs(@Valid @RequestBody List<AccessLogDTO> accessLogDTOs) {
        boolean result = accessLogService.batchCreateAccessLogs(accessLogDTOs);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量创建失败
        }
        return R.success(true);
    }
    
    /**
     * 批量更新进出记录
     */
    @PutMapping("/batch")
    public R<Boolean> batchUpdateAccessLogs(@Valid @RequestBody List<AccessLogDTO> accessLogDTOs) {
        boolean result = accessLogService.batchUpdateAccessLogs(accessLogDTOs);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
    
    /**
     * 根据车牌号和时间范围查询进出记录
     */
    @GetMapping("/plate-number-time-range")
    public R<List<AccessLogDTO>> getAccessLogsByPlateNumberAndTimeRange(
            @RequestParam String plateNumber,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByPlateNumberAndTimeRange(plateNumber, startTime, endTime);
        return R.success(accessLogs);
    }
    
    /**
     * 获取最近N条进出记录
     */
    @GetMapping("/recent")
    public R<List<AccessLogDTO>> getRecentAccessLogs(@RequestParam(defaultValue = "10") Integer limit) {
        List<AccessLogDTO> accessLogs = accessLogService.getRecentAccessLogs(limit);
        return R.success(accessLogs);
    }
    
    /**
     * 获取异常进出记录（识别失败或黑名单）
     */
    @GetMapping("/exception")
    public R<List<AccessLogDTO>> getExceptionAccessLogs() {
        List<AccessLogDTO> accessLogs = accessLogService.getExceptionAccessLogs();
        return R.success(accessLogs);
    }
    
    /**
     * 更新识别结果
     */
    @PutMapping("/{id}/recognition-result")
    public R<Boolean> updateRecognitionResult(@PathVariable Long id,
                                              @RequestParam Integer recognitionResult,
                                              @RequestParam(required = false) String remark) {
        boolean result = accessLogService.updateRecognitionResult(id, recognitionResult, remark);
        if (!result) {
            return R.error(ReturnCode.RC500); // 更新失败
        }
        return R.success(true);
    }
    
    /**
     * 手动处理进出记录（手动放行等）
     */
    @PutMapping("/{id}/handle-manually")
    public R<Boolean> handleAccessLogManually(@PathVariable Long id,
                                              @RequestParam Long handledBy,
                                              @RequestParam(required = false) String remark) {
        boolean result = accessLogService.handleAccessLogManually(id, handledBy, remark);
        if (!result) {
            return R.error(ReturnCode.RC500); // 处理失败
        }
        return R.success(true);
    }
}
