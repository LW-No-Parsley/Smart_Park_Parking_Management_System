package com.syan.smart_park.controller;

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
     * 获取所有进出记录列表
     */
    @GetMapping("/list")
    public R<List<AccessLogDTO>> getAllAccessLogs() {
        List<AccessLogDTO> accessLogs = accessLogService.getAllAccessLogs();
        return R.success(accessLogs);
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
     * 删除进出记录
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteAccessLog(@PathVariable Long id) {
        boolean result = accessLogService.deleteAccessLog(id);
        if (!result) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }
    
    /**
     * 根据园区ID获取进出记录列表
     */
    @GetMapping("/park-area/{parkAreaId}")
    public R<List<AccessLogDTO>> getAccessLogsByParkAreaId(@PathVariable Long parkAreaId) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByParkAreaId(parkAreaId);
        return R.success(accessLogs);
    }
    
    /**
     * 根据道闸ID获取进出记录列表
     */
    @GetMapping("/gate/{gateId}")
    public R<List<AccessLogDTO>> getAccessLogsByGateId(@PathVariable Long gateId) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByGateId(gateId);
        return R.success(accessLogs);
    }
    
    /**
     * 根据车牌号获取进出记录列表
     */
    @GetMapping("/plate-number/{plateNumber}")
    public R<List<AccessLogDTO>> getAccessLogsByPlateNumber(@PathVariable String plateNumber) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByPlateNumber(plateNumber);
        return R.success(accessLogs);
    }
    
    /**
     * 根据车辆ID获取进出记录列表
     */
    @GetMapping("/vehicle/{vehicleId}")
    public R<List<AccessLogDTO>> getAccessLogsByVehicleId(@PathVariable Long vehicleId) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByVehicleId(vehicleId);
        return R.success(accessLogs);
    }
    
    /**
     * 根据进出类型获取进出记录列表
     */
    @GetMapping("/access-type/{accessType}")
    public R<List<AccessLogDTO>> getAccessLogsByAccessType(@PathVariable Integer accessType) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByAccessType(accessType);
        return R.success(accessLogs);
    }
    
    /**
     * 根据识别结果获取进出记录列表
     */
    @GetMapping("/recognition-result/{recognitionResult}")
    public R<List<AccessLogDTO>> getAccessLogsByRecognitionResult(@PathVariable Integer recognitionResult) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByRecognitionResult(recognitionResult);
        return R.success(accessLogs);
    }
    
    /**
     * 根据处理人员ID获取进出记录列表
     */
    @GetMapping("/handled-by/{handledBy}")
    public R<List<AccessLogDTO>> getAccessLogsByHandledBy(@PathVariable Long handledBy) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByHandledBy(handledBy);
        return R.success(accessLogs);
    }
    
    /**
     * 获取指定时间范围内的进出记录列表
     */
    @GetMapping("/time-range")
    public R<List<AccessLogDTO>> getAccessLogsByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<AccessLogDTO> accessLogs = accessLogService.getAccessLogsByTimeRange(startTime, endTime);
        return R.success(accessLogs);
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
     * 批量删除进出记录
     */
    @DeleteMapping("/batch")
    public R<Boolean> batchDeleteAccessLogs(@RequestParam List<Long> ids) {
        boolean result = accessLogService.batchDeleteAccessLogs(ids);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量删除失败
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
