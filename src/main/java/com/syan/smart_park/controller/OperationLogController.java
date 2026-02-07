package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.OperationLogDTO;
import com.syan.smart_park.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 获取所有操作日志记录
     */
    @GetMapping("/list")
    public R<List<OperationLogDTO>> getAllOperationLogs() {
        List<OperationLogDTO> operationLogs = operationLogService.getAllOperationLogs();
        return R.success(operationLogs);
    }
    
    /**
     * 根据ID获取操作日志记录
     */
    @GetMapping("/{id}")
    public R<OperationLogDTO> getOperationLogById(@PathVariable Long id) {
        OperationLogDTO operationLog = operationLogService.getOperationLogById(id);
        if (operationLog == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(operationLog);
    }
    
    /**
     * 创建操作日志记录
     */
    @PostMapping
    public R<OperationLogDTO> createOperationLog(@Valid @RequestBody OperationLogDTO operationLogDTO) {
        OperationLogDTO createdLog = operationLogService.createOperationLog(operationLogDTO);
        return R.success(createdLog);
    }
    
    /**
     * 删除操作日志记录
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteOperationLog(@PathVariable Long id) {
        boolean success = operationLogService.deleteOperationLog(id);
        if (!success) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success();
    }
    
    /**
     * 根据用户ID获取操作日志记录
     */
    @GetMapping("/user/{userId}")
    public R<List<OperationLogDTO>> getOperationLogsByUserId(@PathVariable Long userId) {
        List<OperationLogDTO> operationLogs = operationLogService.getOperationLogsByUserId(userId);
        return R.success(operationLogs);
    }
    
    /**
     * 根据模块名称获取操作日志记录
     */
    @GetMapping("/module/{module}")
    public R<List<OperationLogDTO>> getOperationLogsByModule(@PathVariable String module) {
        List<OperationLogDTO> operationLogs = operationLogService.getOperationLogsByModule(module);
        return R.success(operationLogs);
    }
    
    /**
     * 根据操作动作获取操作日志记录
     */
    @GetMapping("/action/{action}")
    public R<List<OperationLogDTO>> getOperationLogsByAction(@PathVariable String action) {
        List<OperationLogDTO> operationLogs = operationLogService.getOperationLogsByAction(action);
        return R.success(operationLogs);
    }
    
    /**
     * 根据IP地址获取操作日志记录
     */
    @GetMapping("/ip/{ip}")
    public R<List<OperationLogDTO>> getOperationLogsByIp(@PathVariable String ip) {
        List<OperationLogDTO> operationLogs = operationLogService.getOperationLogsByIp(ip);
        return R.success(operationLogs);
    }
    
    /**
     * 根据时间范围获取操作日志记录
     */
    @GetMapping("/time-range")
    public R<List<OperationLogDTO>> getOperationLogsByTimeRange(@RequestParam String startTime,
                                                               @RequestParam String endTime) {
        List<OperationLogDTO> operationLogs = operationLogService.getOperationLogsByTimeRange(startTime, endTime);
        return R.success(operationLogs);
    }
    
    /**
     * 搜索操作日志记录
     */
    @GetMapping("/search")
    public R<List<OperationLogDTO>> searchOperationLogs(@RequestParam String keyword) {
        List<OperationLogDTO> operationLogs = operationLogService.searchOperationLogs(keyword);
        return R.success(operationLogs);
    }
    
    /**
     * 获取最近的操作日志记录
     */
    @GetMapping("/recent")
    public R<List<OperationLogDTO>> getRecentOperationLogs(@RequestParam(defaultValue = "10") int limit) {
        List<OperationLogDTO> operationLogs = operationLogService.getRecentOperationLogs(limit);
        return R.success(operationLogs);
    }
}
