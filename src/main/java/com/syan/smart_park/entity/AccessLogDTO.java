package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 进出记录DTO
 */
@Data
public class AccessLogDTO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
    /**
     * 道闸ID
     */
    private Long gateId;
    
    /**
     * 识别车牌号
     */
    private String plateNumber;
    
    /**
     * 关联车辆ID（可为空）
     */
    private Long vehicleId;
    
    /**
     * 进出类型：1-入场，2-出场
     */
    private Integer accessType;
    
    /**
     * 抓拍图片地址
     */
    private String imageUrl;
    
    /**
     * 识别结果：0-失败，1-成功，2-黑名单
     */
    private Integer recognitionResult;
    
    /**
     * 通行时间
     */
    private LocalDateTime accessTime;
    
    /**
     * 处理人员ID（后台 sys_user.id，用于手动干预）
     */
    private Long handledBy;
    
    /**
     * 备注（手动放行原因等）
     */
    private String remark;
    
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
     * 静态方法：从AccessLog实体转换为AccessLogDTO
     */
    public static AccessLogDTO fromAccessLog(AccessLog accessLog) {
        if (accessLog == null) {
            return null;
        }
        
        AccessLogDTO dto = new AccessLogDTO();
        dto.setId(accessLog.getId());
        dto.setParkAreaId(accessLog.getParkAreaId());
        dto.setGateId(accessLog.getGateId());
        dto.setPlateNumber(accessLog.getPlateNumber());
        dto.setVehicleId(accessLog.getVehicleId());
        dto.setAccessType(accessLog.getAccessType());
        dto.setImageUrl(accessLog.getImageUrl());
        dto.setRecognitionResult(accessLog.getRecognitionResult());
        dto.setAccessTime(accessLog.getAccessTime());
        dto.setHandledBy(accessLog.getHandledBy());
        dto.setRemark(accessLog.getRemark());
        dto.setCreateTime(accessLog.getCreateTime());
        dto.setUpdateTime(accessLog.getUpdateTime());
        dto.setDeleted(accessLog.getDeleted());
        
        return dto;
    }
    
    /**
     * 转换为AccessLog实体
     */
    public AccessLog toAccessLog() {
        AccessLog accessLog = new AccessLog();
        accessLog.setId(this.id);
        accessLog.setParkAreaId(this.parkAreaId);
        accessLog.setGateId(this.gateId);
        accessLog.setPlateNumber(this.plateNumber);
        accessLog.setVehicleId(this.vehicleId);
        accessLog.setAccessType(this.accessType);
        accessLog.setImageUrl(this.imageUrl);
        accessLog.setRecognitionResult(this.recognitionResult);
        accessLog.setAccessTime(this.accessTime);
        accessLog.setHandledBy(this.handledBy);
        accessLog.setRemark(this.remark);
        accessLog.setDeleted(this.deleted);
        
        return accessLog;
    }
}
