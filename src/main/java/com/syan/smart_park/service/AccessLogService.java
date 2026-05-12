package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.AccessLog;
import com.syan.smart_park.entity.AccessLogDTO;
import com.syan.smart_park.entity.GateAccessDTO;
import com.syan.smart_park.entity.GateAccessResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 进出记录服务接口
 */
public interface AccessLogService extends IService<AccessLog> {

    /**
     * 闸机入场处理
     * 通过园区ID+设备编号识别设备，通过车牌号查询是否为默认车辆，
     * 不是则查当天有效预约，都没有则按临时车处理
     */
    GateAccessResult handleEntry(GateAccessDTO gateAccessDTO);

    /**
     * 闸机出场处理
     * 通过园区ID+设备编号识别设备，通过车牌号查找当前在场车辆
     * 执行：释放车位占用(space_occupy), 更新预约状态(如有), 生成支付记录
     */
    GateAccessResult handleExit(GateAccessDTO gateAccessDTO);

    /**
     * 统一分页查询进出记录列表（支持多条件组合筛选）
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
     * 更新识别结果
     */
    boolean updateRecognitionResult(Long id, Integer recognitionResult, String remark);

    /**
     * 手动处理进出记录
     */
    boolean handleAccessLogManually(Long id, Long handledBy, String remark);

    /**
     * 今日进出统计结果
     */
    class AccessLogStatistics {
        private long totalEntries;
        private long totalExits;
        private long exceptionCount;

        public long getTotalEntries() { return totalEntries; }
        public void setTotalEntries(long totalEntries) { this.totalEntries = totalEntries; }
        public long getTotalExits() { return totalExits; }
        public void setTotalExits(long totalExits) { this.totalExits = totalExits; }
        public long getExceptionCount() { return exceptionCount; }
        public void setExceptionCount(long exceptionCount) { this.exceptionCount = exceptionCount; }
    }

    /**
     * 进出趋势数据点
     */
    class AccessLogTrend {
        private LocalDateTime date;
        private long entryCount;
        private long exitCount;

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        public long getEntryCount() { return entryCount; }
        public void setEntryCount(long entryCount) { this.entryCount = entryCount; }
        public long getExitCount() { return exitCount; }
        public void setExitCount(long exitCount) { this.exitCount = exitCount; }
    }
}
