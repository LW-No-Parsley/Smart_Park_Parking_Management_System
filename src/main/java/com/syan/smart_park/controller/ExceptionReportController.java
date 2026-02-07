package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ExceptionReportDTO;
import com.syan.smart_park.service.ExceptionReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 异常上报控制器
 */
@RestController
@RequestMapping("/api/exception-report")
public class ExceptionReportController {
    
    @Autowired
    private ExceptionReportService exceptionReportService;
    
    /**
     * 获取所有异常上报记录
     */
    @GetMapping("/list")
    public R<List<ExceptionReportDTO>> getAllExceptionReports() {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getAllExceptionReports();
        return R.success(exceptionReports);
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
     * 删除异常上报记录
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteExceptionReport(@PathVariable Long id) {
        boolean success = exceptionReportService.deleteExceptionReport(id);
        if (!success) {
            return R.error(ReturnCode.RC1300);
        }
        return R.success();
    }
    
    /**
     * 根据用户ID获取异常上报记录
     */
    @GetMapping("/user/{userId}")
    public R<List<ExceptionReportDTO>> getExceptionReportsByUserId(@PathVariable Long userId) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getExceptionReportsByUserId(userId);
        return R.success(exceptionReports);
    }
    
    /**
     * 根据车位ID获取异常上报记录
     */
    @GetMapping("/space/{spaceId}")
    public R<List<ExceptionReportDTO>> getExceptionReportsBySpaceId(@PathVariable Long spaceId) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getExceptionReportsBySpaceId(spaceId);
        return R.success(exceptionReports);
    }
    
    /**
     * 根据异常类型获取异常上报记录
     */
    @GetMapping("/type/{reportType}")
    public R<List<ExceptionReportDTO>> getExceptionReportsByType(@PathVariable Integer reportType) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getExceptionReportsByType(reportType);
        return R.success(exceptionReports);
    }
    
    /**
     * 根据处理状态获取异常上报记录
     */
    @GetMapping("/status/{status}")
    public R<List<ExceptionReportDTO>> getExceptionReportsByStatus(@PathVariable Integer status) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getExceptionReportsByStatus(status);
        return R.success(exceptionReports);
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
    
    /**
     * 获取未处理的异常上报记录
     */
    @GetMapping("/unhandled")
    public R<List<ExceptionReportDTO>> getUnhandledExceptionReports() {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getUnhandledExceptionReports();
        return R.success(exceptionReports);
    }
    
    /**
     * 获取已处理的异常上报记录
     */
    @GetMapping("/handled")
    public R<List<ExceptionReportDTO>> getHandledExceptionReports() {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getHandledExceptionReports();
        return R.success(exceptionReports);
    }
    
    /**
     * 根据处理人员ID获取异常上报记录
     */
    @GetMapping("/handler/{handledBy}")
    public R<List<ExceptionReportDTO>> getExceptionReportsByHandler(@PathVariable Long handledBy) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.getExceptionReportsByHandler(handledBy);
        return R.success(exceptionReports);
    }
    
    /**
     * 搜索异常上报记录
     */
    @GetMapping("/search")
    public R<List<ExceptionReportDTO>> searchExceptionReports(@RequestParam String keyword) {
        List<ExceptionReportDTO> exceptionReports = exceptionReportService.searchExceptionReports(keyword);
        return R.success(exceptionReports);
    }
}
