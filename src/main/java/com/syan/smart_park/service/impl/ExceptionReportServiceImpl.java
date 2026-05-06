package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
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
    public PageResult<ExceptionReportDTO> pageExceptionReports(long current, long size,
                                                               Long userId, Long spaceId,
                                                               Integer reportType, Integer status,
                                                               Long handledBy, String keyword) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();

        // 动态条件：仅传入非 null 值时生效
        queryWrapper.eq(userId != null, ExceptionReport::getUserId, userId);
        queryWrapper.eq(spaceId != null, ExceptionReport::getSpaceId, spaceId);
        queryWrapper.eq(reportType != null, ExceptionReport::getReportType, reportType);
        // status: null=不过滤, 0=未处理, -1=已处理所有(ne 0), 其余精确匹配
        if (status != null) {
            if (status == -1) {
                queryWrapper.ne(ExceptionReport::getStatus, 0);
            } else {
                queryWrapper.eq(ExceptionReport::getStatus, status);
            }
        }
        queryWrapper.eq(handledBy != null, ExceptionReport::getHandledBy, handledBy);

        // 关键词模糊搜索描述
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(ExceptionReport::getDescription, keyword);
        }

        queryWrapper.orderByDesc(ExceptionReport::getCreateTime);

        IPage<ExceptionReport> page = this.page(new Page<>(current, size), queryWrapper);
        List<ExceptionReportDTO> dtoList = page.getRecords().stream()
                .map(ExceptionReportDTO::fromExceptionReport)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotal(), page.getCurrent(), page.getSize());
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
}
