package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.ExceptionReport;
import com.syan.smart_park.entity.ExceptionReportDTO;

/**
 * 异常上报服务接口
 */
public interface ExceptionReportService extends IService<ExceptionReport> {

    /**
     * 统一分页查询异常上报记录（支持多条件组合筛选）
     *
     * @param current      当前页码
     * @param size         每页大小
     * @param userId       用户ID（可选）
     * @param spaceId      车位ID（可选）
     * @param reportType   异常类型（可选）
     * @param status       处理状态：0-未处理 1-已处理（可选）
     * @param handledBy    处理人员ID（可选）
     * @param keyword      关键词（可选，模糊搜索 description）
     * @return 分页结果
     */
    PageResult<ExceptionReportDTO> pageExceptionReports(long current, long size,
                                                        Long userId, Long spaceId,
                                                        Integer reportType, Integer status,
                                                        Long handledBy, String keyword);
    
    /**
     * 根据ID获取异常上报记录
     */
    ExceptionReportDTO getExceptionReportById(Long id);
    
    /**
     * 创建异常上报记录
     */
    ExceptionReportDTO createExceptionReport(ExceptionReportDTO exceptionReportDTO);
    
    /**
     * 更新异常上报记录
     */
    ExceptionReportDTO updateExceptionReport(Long id, ExceptionReportDTO exceptionReportDTO);
    
    /**
     * 处理异常上报（更新处理状态、处理人员、处理时间和处理结果）
     */
    ExceptionReportDTO handleExceptionReport(Long id, Long handledBy, String handleResult);
}
