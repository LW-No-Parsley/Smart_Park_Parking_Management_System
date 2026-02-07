package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.OperationLog;
import com.syan.smart_park.entity.OperationLogDTO;

import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {
    
    /**
     * 获取所有操作日志记录
     */
    List<OperationLogDTO> getAllOperationLogs();
    
    /**
     * 根据ID获取操作日志记录
     */
    OperationLogDTO getOperationLogById(Long id);
    
    /**
     * 创建操作日志记录
     */
    OperationLogDTO createOperationLog(OperationLogDTO operationLogDTO);
    
    /**
     * 删除操作日志记录（逻辑删除）
     */
    boolean deleteOperationLog(Long id);
    
    /**
     * 根据用户ID获取操作日志记录
     */
    List<OperationLogDTO> getOperationLogsByUserId(Long userId);
    
    /**
     * 根据模块名称获取操作日志记录
     */
    List<OperationLogDTO> getOperationLogsByModule(String module);
    
    /**
     * 根据操作动作获取操作日志记录
     */
    List<OperationLogDTO> getOperationLogsByAction(String action);
    
    /**
     * 根据IP地址获取操作日志记录
     */
    List<OperationLogDTO> getOperationLogsByIp(String ip);
    
    /**
     * 根据时间范围获取操作日志记录
     */
    List<OperationLogDTO> getOperationLogsByTimeRange(String startTime, String endTime);
    
    /**
     * 搜索操作日志记录（根据模块、动作、详情模糊搜索）
     */
    List<OperationLogDTO> searchOperationLogs(String keyword);
    
    /**
     * 获取最近的操作日志记录
     */
    List<OperationLogDTO> getRecentOperationLogs(int limit);
}
