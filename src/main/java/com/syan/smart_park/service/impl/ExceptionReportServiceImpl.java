package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ExceptionReportMapper;
import com.syan.smart_park.entity.ExceptionReport;
import com.syan.smart_park.entity.ExceptionReportDTO;
import com.syan.smart_park.service.ExceptionReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常上报服务实现类
 */
@Service
public class ExceptionReportServiceImpl extends ServiceImpl<ExceptionReportMapper, ExceptionReport> implements ExceptionReportService {
    
    @Override
    public List<ExceptionReportDTO> getAllExceptionReports() {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public ExceptionReportDTO getExceptionReportById(Long id) {
        ExceptionReport exceptionReport = this.getById(id);
        if (exceptionReport == null || exceptionReport.getDeleted() == 1) {
            return null;
        }
        return ExceptionReportDTO.fromExceptionReport(exceptionReport);
    }
    
    @Override
    public ExceptionReportDTO createExceptionReport(ExceptionReportDTO exceptionReportDTO) {
        ExceptionReport exceptionReport = exceptionReportDTO.toExceptionReport();
        // 设置默认状态为未处理
        if (exceptionReport.getStatus() == null) {
            exceptionReport.setStatus(0);
        }
        this.save(exceptionReport);
        return ExceptionReportDTO.fromExceptionReport(exceptionReport);
    }
    
    @Override
    public ExceptionReportDTO updateExceptionReport(Long id, ExceptionReportDTO exceptionReportDTO) {
        ExceptionReport existingReport = this.getById(id);
        if (existingReport == null || existingReport.getDeleted() == 1) {
            return null;
        }
        
        ExceptionReport updatedReport = exceptionReportDTO.toExceptionReport();
        updatedReport.setId(id);
        // 保留原有的创建时间
        updatedReport.setCreateTime(existingReport.getCreateTime());
        this.updateById(updatedReport);
        
        return ExceptionReportDTO.fromExceptionReport(updatedReport);
    }
    
    @Override
    public boolean deleteExceptionReport(Long id) {
        ExceptionReport exceptionReport = this.getById(id);
        if (exceptionReport == null || exceptionReport.getDeleted() == 1) {
            return false;
        }
        
        exceptionReport.setDeleted(1);
        return this.updateById(exceptionReport);
    }
    
    @Override
    public List<ExceptionReportDTO> getExceptionReportsByUserId(Long userId) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getUserId, userId)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> getExceptionReportsBySpaceId(Long spaceId) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getSpaceId, spaceId)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> getExceptionReportsByType(Integer reportType) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getReportType, reportType)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> getExceptionReportsByStatus(Integer status) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getStatus, status)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public ExceptionReportDTO handleExceptionReport(Long id, Long handledBy, String handleResult) {
        ExceptionReport exceptionReport = this.getById(id);
        if (exceptionReport == null || exceptionReport.getDeleted() == 1) {
            return null;
        }
        
        // 更新处理信息
        exceptionReport.setStatus(2); // 已处理
        exceptionReport.setHandledBy(handledBy);
        exceptionReport.setHandleTime(LocalDateTime.now());
        exceptionReport.setHandleResult(handleResult);
        
        this.updateById(exceptionReport);
        return ExceptionReportDTO.fromExceptionReport(exceptionReport);
    }
    
    @Override
    public List<ExceptionReportDTO> getUnhandledExceptionReports() {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getStatus, 0) // 未处理
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> getHandledExceptionReports() {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExceptionReport::getStatus, 1, 2, 3) // 已受理、已处理、已关闭
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> getExceptionReportsByHandler(Long handledBy) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExceptionReport::getHandledBy, handledBy)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getHandleTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExceptionReportDTO> searchExceptionReports(String keyword) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ExceptionReport::getDescription, keyword)
                   .eq(ExceptionReport::getDeleted, 0)
                   .orderByDesc(ExceptionReport::getCreateTime);
        List<ExceptionReport> exceptionReports = this.list(queryWrapper);
        return exceptionReports.stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());
    }
}
