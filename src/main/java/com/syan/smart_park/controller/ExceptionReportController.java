package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ExceptionReportDTO;
import com.syan.smart_park.service.ExceptionReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 异常上报控制器
 */
@RestController
@RequestMapping("/api/exception-report")
@RequiredArgsConstructor
public class ExceptionReportController {
    
    private final ExceptionReportService exceptionReportService;

    /**
     * 统一分页查询异常上报记录（支持多条件组合筛选）
     * 合并了原 /user/* /space/* /type/* /status/* /unhandled /handled /handler/* /search 等路由
     */
    @GetMapping("/list")
    public R<PageResult<ExceptionReportDTO>> getExceptionReportList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long handledBy,
            @RequestParam(required = false) String keyword) {
        PageResult<ExceptionReportDTO> result = exceptionReportService.pageExceptionReports(
                current, size, userId, spaceId, reportType, status, handledBy, keyword);
        return R.success(result);
    }
    
    /**
     * 根据ID获取异常上报记录
     */
    @GetMapping("/{id}")
    public R<ExceptionReportDTO> getExceptionReportById(@PathVariable Long id) {
        ExceptionReportDTO exceptionReport = exceptionReportService.getExceptionReportById(id);
        if (exceptionReport == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(exceptionReport);
    }
    
    /**
     * 创建异常上报记录
     */
    @PostMapping
    public R<ExceptionReportDTO> createExceptionReport(@Valid @RequestBody ExceptionReportDTO exceptionReportDTO) {
        ExceptionReportDTO createdReport = exceptionReportService.createExceptionReport(exceptionReportDTO);
        return R.success(createdReport);
    }
    
    /**
     * 更新异常上报记录
     */
    @PutMapping("/{id}")
    public R<ExceptionReportDTO> updateExceptionReport(@PathVariable Long id, 
                                                      @Valid @RequestBody ExceptionReportDTO exceptionReportDTO) {
        ExceptionReportDTO updatedReport = exceptionReportService.updateExceptionReport(id, exceptionReportDTO);
        if (updatedReport == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(updatedReport);
    }
    
    /**
     * 处理异常上报
     */
    @PutMapping("/{id}/handle")
    public R<ExceptionReportDTO> handleExceptionReport(@PathVariable Long id,
                                                      @RequestParam Long handledBy,
                                                      @RequestParam String handleResult) {
        ExceptionReportDTO handledReport = exceptionReportService.handleExceptionReport(id, handledBy, handleResult);
        if (handledReport == null) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success(handledReport);
    }
}
