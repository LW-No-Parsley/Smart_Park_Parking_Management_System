package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.OperationLogMapper;
import com.syan.smart_park.entity.OperationLog;
import com.syan.smart_park.entity.OperationLogDTO;
import com.syan.smart_park.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<OperationLogDTO> getAllOperationLogs() {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public OperationLogDTO getOperationLogById(Long id) {
        OperationLog operationLog = this.getById(id);
        if (operationLog == null || operationLog.getDeleted() == 1) {
            return null;
        }
        return OperationLogDTO.fromOperationLog(operationLog);
    }
    
    @Override
    public OperationLogDTO createOperationLog(OperationLogDTO operationLogDTO) {
        OperationLog operationLog = operationLogDTO.toOperationLog();
        this.save(operationLog);
        return OperationLogDTO.fromOperationLog(operationLog);
    }
    
    @Override
    public boolean deleteOperationLog(Long id) {
        OperationLog operationLog = this.getById(id);
        if (operationLog == null || operationLog.getDeleted() == 1) {
            return false;
        }
        
        operationLog.setDeleted(1);
        return this.updateById(operationLog);
    }
    
    @Override
    public List<OperationLogDTO> getOperationLogsByUserId(Long userId) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getUserId, userId)
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> getOperationLogsByModule(String module) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getModule, module)
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> getOperationLogsByAction(String action) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getAction, action)
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> getOperationLogsByIp(String ip) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getIp, ip)
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> getOperationLogsByTimeRange(String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
        
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(OperationLog::getCreateTime, start)
                   .le(OperationLog::getCreateTime, end)
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> searchOperationLogs(String keyword) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                    .like(OperationLog::getModule, keyword)
                    .or()
                    .like(OperationLog::getAction, keyword)
                    .or()
                    .like(OperationLog::getDetail, keyword))
                   .eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OperationLogDTO> getRecentOperationLogs(int limit) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OperationLog::getDeleted, 0)
                   .orderByDesc(OperationLog::getCreateTime)
                   .last("LIMIT " + limit);
        List<OperationLog> operationLogs = this.list(queryWrapper);
        return operationLogs.stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());
    }
}
