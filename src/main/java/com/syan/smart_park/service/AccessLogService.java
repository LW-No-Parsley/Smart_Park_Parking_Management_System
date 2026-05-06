package com.syan.smart_park.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.AccessLog;
import com.syan.smart_park.entity.AccessLogDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 进出记录服务接口
 */
public interface AccessLogService extends IService<AccessLog> {

    /**
     * 统一分页查询进出记录（支持多条件组合筛选）
     *
     * @param current          当前页码
     * @param size             每页大小
     * @param parkAreaId       园区ID（可选）
     * @param gateId           道闸ID（可选）
     * @param plateNumber      车牌号（可选，模糊匹配）
     * @param vehicleId        车辆ID（可选）
     * @param accessType       进出类型：1-入场 2-出场（可选）
     * @param recognitionResult 识别结果：0-失败 1-成功 2-黑名单（可选）
     * @param handledBy        处理人员ID（可选）
     * @param startTime        通行开始时间（可选）
     * @param endTime          通行结束时间（可选）
     * @param exceptionOnly    是否仅查异常记录（可选）
     * @return 分页结果
     */
    PageResult<AccessLogDTO> pageAccessLogs(long current, long size,
                                            Long parkAreaId, Long gateId,
                                            String plateNumber, Long vehicleId,
                                            Integer accessType, Integer recognitionResult,
                                            Long handledBy,
                                            LocalDateTime startTime, LocalDateTime endTime,
                                            Boolean exceptionOnly);

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
