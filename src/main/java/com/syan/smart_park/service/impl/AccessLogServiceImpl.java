package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.AccessLogMapper;
import com.syan.smart_park.entity.AccessLog;
import com.syan.smart_park.entity.AccessLogDTO;
import com.syan.smart_park.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 进出记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl extends ServiceImpl<AccessLogMapper, AccessLog> implements AccessLogService {

    private final AccessLogMapper accessLogMapper;

    @Override
    public List<AccessLogDTO> getAllAccessLogs() {
        List<AccessLog> accessLogs = this.list();
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public AccessLogDTO getAccessLogById(Long id) {
        AccessLog accessLog = this.getById(id);
        return AccessLogDTO.fromAccessLog(accessLog);
    }

    @Override
    @Transactional
    public AccessLogDTO createAccessLog(AccessLogDTO accessLogDTO) {
        AccessLog accessLog = accessLogDTO.toAccessLog();
        if (accessLog.getAccessTime() == null) {
            accessLog.setAccessTime(LocalDateTime.now());
        }
        this.save(accessLog);
        return AccessLogDTO.fromAccessLog(accessLog);
    }

    @Override
    @Transactional
    public AccessLogDTO updateAccessLog(Long id, AccessLogDTO accessLogDTO) {
        AccessLog existingAccessLog = this.getById(id);
        if (existingAccessLog == null) {
            return null;
        }
        
        AccessLog accessLog = accessLogDTO.toAccessLog();
        accessLog.setId(id);
        this.updateById(accessLog);
        
        return AccessLogDTO.fromAccessLog(accessLog);
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByGateId(Long gateId) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getGateId, gateId);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByPlateNumber(String plateNumber) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getPlateNumber, plateNumber);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getVehicleId, vehicleId);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByAccessType(Integer accessType) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getAccessType, accessType);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByRecognitionResult(Integer recognitionResult) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getRecognitionResult, recognitionResult);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByHandledBy(Long handledBy) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getHandledBy, handledBy);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AccessLog::getAccessTime, startTime)
                   .le(AccessLog::getAccessTime, endTime);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public AccessLogStatistics getTodayAccessLogStatistics(Long parkAreaId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AccessLog::getAccessTime, startOfDay)
                   .lt(AccessLog::getAccessTime, endOfDay);
        
        if (parkAreaId != null) {
            queryWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        }
        
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        long totalCount = accessLogs.size();
        long entryCount = accessLogs.stream().filter(log -> log.getAccessType() == 1).count();
        long exitCount = accessLogs.stream().filter(log -> log.getAccessType() == 2).count();
        long successCount = accessLogs.stream().filter(log -> log.getRecognitionResult() == 1).count();
        long failureCount = accessLogs.stream().filter(log -> log.getRecognitionResult() == 0).count();
        long blacklistCount = accessLogs.stream().filter(log -> log.getRecognitionResult() == 2).count();
        
        return new AccessLogStatistics(totalCount, entryCount, exitCount, successCount, failureCount, blacklistCount);
    }

    @Override
    public List<AccessLogTrend> getAccessLogTrend(LocalDateTime startTime, LocalDateTime endTime, Long parkAreaId) {
        // 按小时分组统计
        List<AccessLogTrend> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:00");
        
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalDateTime nextHour = current.plusHours(1);
            
            LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(AccessLog::getAccessTime, current)
                       .lt(AccessLog::getAccessTime, nextHour);
            
            if (parkAreaId != null) {
                queryWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
            }
            
            List<AccessLog> hourLogs = this.list(queryWrapper);
            long entryCount = hourLogs.stream().filter(log -> log.getAccessType() == 1).count();
            long exitCount = hourLogs.stream().filter(log -> log.getAccessType() == 2).count();
            
            trends.add(new AccessLogTrend(current.format(formatter), entryCount, exitCount));
            current = nextHour;
        }
        
        return trends;
    }

    @Override
    @Transactional
    public boolean batchCreateAccessLogs(List<AccessLogDTO> accessLogDTOs) {
        if (accessLogDTOs == null || accessLogDTOs.isEmpty()) {
            return false;
        }
        
        List<AccessLog> accessLogs = accessLogDTOs.stream()
                .map(AccessLogDTO::toAccessLog)
                .collect(Collectors.toList());
        
        return this.saveBatch(accessLogs);
    }

    @Override
    @Transactional
    public boolean batchUpdateAccessLogs(List<AccessLogDTO> accessLogDTOs) {
        if (accessLogDTOs == null || accessLogDTOs.isEmpty()) {
            return false;
        }
        
        List<AccessLog> accessLogs = accessLogDTOs.stream()
                .map(dto -> {
                    AccessLog accessLog = dto.toAccessLog();
                    return accessLog;
                })
                .collect(Collectors.toList());
        
        return this.updateBatchById(accessLogs);
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByPlateNumberAndTimeRange(String plateNumber, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLog::getPlateNumber, plateNumber)
                   .ge(AccessLog::getAccessTime, startTime)
                   .le(AccessLog::getAccessTime, endTime);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getRecentAccessLogs(Integer limit) {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AccessLog::getAccessTime);
        queryWrapper.last("LIMIT " + (limit != null ? limit : 10));
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getExceptionAccessLogs() {
        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AccessLog::getRecognitionResult, 0, 2); // 失败或黑名单
        queryWrapper.orderByDesc(AccessLog::getAccessTime);
        List<AccessLog> accessLogs = this.list(queryWrapper);
        
        return accessLogs.stream()
                .map(AccessLogDTO::fromAccessLog)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateRecognitionResult(Long id, Integer recognitionResult, String remark) {
        AccessLog accessLog = this.getById(id);
        if (accessLog == null) {
            return false;
        }
        
        accessLog.setRecognitionResult(recognitionResult);
        if (remark != null) {
            accessLog.setRemark(remark);
        }
        
        return this.updateById(accessLog);
    }

    @Override
    @Transactional
    public boolean handleAccessLogManually(Long id, Long handledBy, String remark) {
        AccessLog accessLog = this.getById(id);
        if (accessLog == null) {
            return false;
        }
        
        accessLog.setHandledBy(handledBy);
        accessLog.setRecognitionResult(1); // 手动处理设为成功
        if (remark != null) {
            accessLog.setRemark(remark);
        }
        
        return this.updateById(accessLog);
    }
}
