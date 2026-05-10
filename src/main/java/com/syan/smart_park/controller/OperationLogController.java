package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.OperationLogDTO;
import com.syan.smart_park.service.OperationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-log")
@RequiredArgsConstructor
public class OperationLogController {
    
    private final OperationLogService operationLogService;

    /**
     * 统一分页查询操作日志（支持多条件组合筛选）
     * 合并了原 /user/* /module/* /action/* /ip/* /time-range /search /recent 等路由
     */
    @GetMapping("/list")
    @RequirePermission("log:list")
    public R<PageResult<OperationLogDTO>> getOperationLogList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String keyword) {
        PageResult<OperationLogDTO> result = operationLogService.pageOperationLogs(
                current, size, userId, module, action, ip,
                startTime, endTime, keyword);
        return R.success(result);
    }
    
    /**
     * 根据ID获取操作日志记录
     */
    @GetMapping("/{id}")
    @RequirePermission("log:list")
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
    @RequirePermission("log:list")
    public R<OperationLogDTO> createOperationLog(@Valid @RequestBody OperationLogDTO operationLogDTO) {
        OperationLogDTO createdLog = operationLogService.createOperationLog(operationLogDTO);
        return R.success(createdLog);
    }
}
