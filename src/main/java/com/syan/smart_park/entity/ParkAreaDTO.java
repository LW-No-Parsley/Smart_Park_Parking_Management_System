package com.syan.smart_park.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 园区DTO
 */
@Data
public class ParkAreaDTO {
    
    /**
     * 园区ID
     */
    private Long id;
    
    /**
     * 园区名称
     */
    private String name;
    
    /**
     * 园区地址
     */
    private String address;
    
    /**
     * 总车位数量
     */
    private Integer totalSpaces;
    
    /**
     * 主园区管理员ID（sys_user.id）
     */
    private Long primaryAdminUserId;
    
    /**
     * 园区纬度坐标
     */
    private BigDecimal latitude;
    
    /**
     * 园区经度坐标
     */
    private BigDecimal longitude;
    
    /**
     * 营业开始时间
     */
    private LocalTime businessHoursStart;
    
    /**
     * 营业结束时间
     */
    private LocalTime businessHoursEnd;
    
    /**
     * 园区状态：0-关闭，1-开放
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 静态方法：从ParkArea实体转换为ParkAreaDTO
     */
    public static ParkAreaDTO fromParkArea(ParkArea parkArea) {
        if (parkArea == null) {
            return null;
        }
        
        ParkAreaDTO dto = new ParkAreaDTO();
        dto.setId(parkArea.getId());
        dto.setName(parkArea.getName());
        dto.setAddress(parkArea.getAddress());
        dto.setTotalSpaces(parkArea.getTotalSpaces());
        dto.setPrimaryAdminUserId(parkArea.getPrimaryAdminUserId());
        dto.setLatitude(parkArea.getLatitude());
        dto.setLongitude(parkArea.getLongitude());
        dto.setBusinessHoursStart(parkArea.getBusinessHoursStart());
        dto.setBusinessHoursEnd(parkArea.getBusinessHoursEnd());
        dto.setStatus(parkArea.getStatus());
        dto.setCreateTime(parkArea.getCreateTime());
        dto.setUpdateTime(parkArea.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为ParkArea实体
     */
    public ParkArea toParkArea() {
        ParkArea parkArea = new ParkArea();
        parkArea.setId(this.id);
        parkArea.setName(this.name);
        parkArea.setAddress(this.address);
        parkArea.setTotalSpaces(this.totalSpaces);
        parkArea.setPrimaryAdminUserId(this.primaryAdminUserId);
        parkArea.setLatitude(this.latitude);
        parkArea.setLongitude(this.longitude);
        parkArea.setBusinessHoursStart(this.businessHoursStart);
        parkArea.setBusinessHoursEnd(this.businessHoursEnd);
        parkArea.setStatus(this.status);
        
        return parkArea;
    }
}
