package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.ExceptionReport;
import com.syan.smart_park.entity.ExceptionReportDTO;

import java.util.List;

/**
 * 异常上报服务接口
 */
public interface ExceptionReportService extends IService<ExceptionReport> {
    
    /**
     * 获取所有异常上报记录
     */
    List<ExceptionReportDTO> getAllExceptionReports();
    
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
     * 根据用户ID获取异常上报记录
     */
    List<ExceptionReportDTO> getExceptionReportsByUserId(Long userId);
    
    /**
     * 根据车位ID获取异常上报记录
     */
    List<ExceptionReportDTO> getExceptionReportsBySpaceId(Long spaceId);
    
    /**
     * 根据异常类型获取异常上报记录
     */
    List<ExceptionReportDTO> getExceptionReportsByType(Integer reportType);
    
    /**
     * 根据处理状态获取异常上报记录
     */
    List<ExceptionReportDTO> getExceptionReportsByStatus(Integer status);
    
    /**
     * 处理异常上报（更新处理状态、处理人员、处理时间和处理结果）
     */
    ExceptionReportDTO handleExceptionReport(Long id, Long handledBy, String handleResult);
    
    /**
     * 获取未处理的异常上报记录
     */
    List<ExceptionReportDTO> getUnhandledExceptionReports();
    
    /**
     * 获取已处理的异常上报记录
     */
    List<ExceptionReportDTO> getHandledExceptionReports();
    
    /**
     * 根据处理人员ID获取异常上报记录
     */
    List<ExceptionReportDTO> getExceptionReportsByHandler(Long handledBy);
    
    /**
     * 搜索异常上报记录（根据描述模糊搜索）
     */
    List<ExceptionReportDTO> searchExceptionReports(String keyword);
}
