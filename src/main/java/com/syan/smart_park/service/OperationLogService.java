package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.OperationLog;
import com.syan.smart_park.entity.OperationLogDTO;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 统一分页查询操作日志（支持多条件组合筛选）
     *
     * @param current   当前页码
     * @param size      每页大小
     * @param userId    操作用户ID（可选）
     * @param module    模块名称（可选，模糊匹配）
     * @param action    操作动作（可选，模糊匹配）
     * @param ip        操作IP（可选，模糊匹配）
     * @param startTime 操作开始时间（可选）
     * @param endTime   操作结束时间（可选）
     * @param keyword   关键词（可选，模糊搜索 module/action/detail）
     * @return 分页结果
     */
    PageResult<OperationLogDTO> pageOperationLogs(long current, long size,
                                                  Long userId,
                                                  String module, String action, String ip,
                                                  LocalDateTime startTime, LocalDateTime endTime,
                                                  String keyword);

    /**
     * 根据ID获取操作日志记录
     */
    OperationLogDTO getOperationLogById(Long id);

    /**
     * 创建操作日志记录
     */
    OperationLogDTO createOperationLog(OperationLogDTO operationLogDTO);
}
