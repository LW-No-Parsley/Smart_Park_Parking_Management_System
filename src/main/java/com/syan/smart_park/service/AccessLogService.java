package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.AccessLog;
import com.syan.smart_park.entity.AccessLogDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 进出记录服务接口
 */
public interface AccessLogService extends IService<AccessLog> {
    
    /**
     * 获取所有进出记录
     */
    List<AccessLogDTO> getAllAccessLogs();
    
    /**
     * 根据ID获取进出记录
     */
    AccessLogDTO getAccessLogById(Long id);
    
    /**
     * 创建进出记录
     */
    AccessLogDTO createAccessLog(AccessLogDTO accessLogDTO);
    
    /**
     * 更新进出记录
     */
    AccessLogDTO updateAccessLog(Long id, AccessLogDTO accessLogDTO);
    
    /**
     * 删除进出记录
     */
    boolean deleteAccessLog(Long id);
    
    /**
     * 根据园区ID获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByParkAreaId(Long parkAreaId);
    
    /**
     * 根据道闸ID获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByGateId(Long gateId);
    
    /**
     * 根据车牌号获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByPlateNumber(String plateNumber);
    
    /**
     * 根据车辆ID获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByVehicleId(Long vehicleId);
    
    /**
     * 根据进出类型获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByAccessType(Integer accessType);
    
    /**
     * 根据识别结果获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByRecognitionResult(Integer recognitionResult);
    
    /**
     * 根据处理人员ID获取进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByHandledBy(Long handledBy);
    
    /**
     * 获取指定时间范围内的进出记录列表
     */
    List<AccessLogDTO> getAccessLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取今日进出记录统计
     */
    AccessLogStatistics getTodayAccessLogStatistics(Long parkAreaId);
    
    /**
     * 获取进出记录趋势数据
     */
    List<AccessLogTrend> getAccessLogTrend(LocalDateTime startTime, LocalDateTime endTime, Long parkAreaId);
    
    /**
     * 批量创建进出记录
     */
    boolean batchCreateAccessLogs(List<AccessLogDTO> accessLogDTOs);
    
    /**
     * 批量更新进出记录
     */
    boolean batchUpdateAccessLogs(List<AccessLogDTO> accessLogDTOs);
    
    /**
     * 批量删除进出记录
     */
    boolean batchDeleteAccessLogs(List<Long> ids);
    
    /**
     * 根据车牌号和时间范围查询进出记录
     */
    List<AccessLogDTO> getAccessLogsByPlateNumberAndTimeRange(String plateNumber, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取最近N条进出记录
     */
    List<AccessLogDTO> getRecentAccessLogs(Integer limit);
    
    /**
     * 获取异常进出记录（识别失败或黑名单）
     */
    List<AccessLogDTO> getExceptionAccessLogs();
    
    /**
     * 更新识别结果
     */
    boolean updateRecognitionResult(Long id, Integer recognitionResult, String remark);
    
    /**
     * 手动处理进出记录（手动放行等）
     */
    boolean handleAccessLogManually(Long id, Long handledBy, String remark);
    
    /**
     * 进出记录统计类
     */
    class AccessLogStatistics {
        private Long totalCount;
        private Long entryCount;
        private Long exitCount;
        private Long successCount;
        private Long failureCount;
        private Long blacklistCount;
        
        // 构造函数、getter和setter
        public AccessLogStatistics() {}
        
        public AccessLogStatistics(Long totalCount, Long entryCount, Long exitCount, 
                                  Long successCount, Long failureCount, Long blacklistCount) {
            this.totalCount = totalCount;
            this.entryCount = entryCount;
            this.exitCount = exitCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.blacklistCount = blacklistCount;
        }
        
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        
        public Long getEntryCount() { return entryCount; }
        public void setEntryCount(Long entryCount) { this.entryCount = entryCount; }
        
        public Long getExitCount() { return exitCount; }
        public void setExitCount(Long exitCount) { this.exitCount = exitCount; }
        
        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }
        
        public Long getFailureCount() { return failureCount; }
        public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
        
        public Long getBlacklistCount() { return blacklistCount; }
        public void setBlacklistCount(Long blacklistCount) { this.blacklistCount = blacklistCount; }
    }
    
    /**
     * 进出记录趋势类
     */
    class AccessLogTrend {
        private String timeLabel;
        private Long entryCount;
        private Long exitCount;
        
        // 构造函数、getter和setter
        public AccessLogTrend() {}
        
        public AccessLogTrend(String timeLabel, Long entryCount, Long exitCount) {
            this.timeLabel = timeLabel;
            this.entryCount = entryCount;
            this.exitCount = exitCount;
        }
        
        public String getTimeLabel() { return timeLabel; }
        public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
        
        public Long getEntryCount() { return entryCount; }
        public void setEntryCount(Long entryCount) { this.entryCount = entryCount; }
        
        public Long getExitCount() { return exitCount; }
        public void setExitCount(Long exitCount) { this.exitCount = exitCount; }
    }
}
