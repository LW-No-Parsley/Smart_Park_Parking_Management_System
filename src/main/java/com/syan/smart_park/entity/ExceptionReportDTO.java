package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常上报数据传输对象
 */
@Data
public class ExceptionReportDTO {
    
    /**
     * 异常ID
     */
    private Long id;
    
    /**
     * 上报用户ID
     */
    private Long userId;
    
    /**
     * 关联车位ID
     */
    private Long spaceId;
    
    /**
     * 异常类型：1-车位被占，2-设备故障，3-违停，4-其他
     */
    private Integer reportType;
    
    /**
     * 异常描述
     */
    private String description;
    
    /**
     * 图片地址
     */
    private String imageUrl;
    
    /**
     * 处理状态：0-未处理，1-已受理，2-已处理，3-已关闭
     */
    private Integer status;
    
    /**
     * 处理人员ID（后台 sys_user.id）
     */
    private Long handledBy;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 从ExceptionReport实体转换为DTO
     */
    public static ExceptionReportDTO fromExceptionReport(ExceptionReport exceptionReport) {
        if (exceptionReport == null) {
            return null;
        }
        
        ExceptionReportDTO dto = new ExceptionReportDTO();
        dto.setId(exceptionReport.getId());
        dto.setUserId(exceptionReport.getUserId());
        dto.setSpaceId(exceptionReport.getSpaceId());
        dto.setReportType(exceptionReport.getReportType());
        dto.setDescription(exceptionReport.getDescription());
        dto.setImageUrl(exceptionReport.getImageUrl());
        dto.setStatus(exceptionReport.getStatus());
        dto.setHandledBy(exceptionReport.getHandledBy());
        dto.setHandleTime(exceptionReport.getHandleTime());
        dto.setHandleResult(exceptionReport.getHandleResult());
        dto.setCreateTime(exceptionReport.getCreateTime());
        dto.setUpdateTime(exceptionReport.getUpdateTime());
        dto.setDeleted(exceptionReport.getDeleted());
        
        return dto;
    }
    
    /**
     * 从DTO转换为ExceptionReport实体
     */
    public ExceptionReport toExceptionReport() {
        ExceptionReport exceptionReport = new ExceptionReport();
        exceptionReport.setId(this.getId());
        exceptionReport.setUserId(this.getUserId());
        exceptionReport.setSpaceId(this.getSpaceId());
        exceptionReport.setReportType(this.getReportType());
        exceptionReport.setDescription(this.getDescription());
        exceptionReport.setImageUrl(this.getImageUrl());
        exceptionReport.setStatus(this.getStatus());
        exceptionReport.setHandledBy(this.getHandledBy());
        exceptionReport.setHandleTime(this.getHandleTime());
        exceptionReport.setHandleResult(this.getHandleResult());
        exceptionReport.setCreateTime(this.getCreateTime());
        exceptionReport.setUpdateTime(this.getUpdateTime());
        exceptionReport.setDeleted(this.getDeleted());
        
        return exceptionReport;
    }
}
