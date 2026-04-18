package com.syan.smart_park.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车位DTO
 */
@Data
public class ParkingSpaceDTO {
    
    /**
     * 车位ID
     */
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
    /**
     * 分区ID（parking_zone.id）
     */
    private Long zoneId;
    
    /**
     * 车位编号（如A-101）
     */
    private String spaceNumber;
    
    /**
     * 车位类型：1-固定，2-临时，3-访客，4-残障专用
     */
    private Integer spaceType;
    
    /**
     * 车位状态：0-禁用，1-正常，4-故障
     */
    private Integer status;
    
    /**
     * 车位纬度坐标（用于导航）
     */
    private BigDecimal latitude;
    
    /**
     * 车位经度坐标（用于导航）
     */
    private BigDecimal longitude;
    
    /**
     * 绑定用户ID（固定车位，关联 park_user.id）
     */
    private Long bindUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 当前占用状态：0-未占用，1-占用中（不写入数据库，动态计算）
     */
    private Integer currentOccupiedStatus;
    
    /**
     * 静态方法：从ParkingSpace实体转换为ParkingSpaceDTO
     */
    public static ParkingSpaceDTO fromParkingSpace(ParkingSpace parkingSpace) {
        if (parkingSpace == null) {
            return null;
        }
        
        ParkingSpaceDTO dto = new ParkingSpaceDTO();
        dto.setId(parkingSpace.getId());
        dto.setParkAreaId(parkingSpace.getParkAreaId());
        dto.setZoneId(parkingSpace.getZoneId());
        dto.setSpaceNumber(parkingSpace.getSpaceNumber());
        dto.setSpaceType(parkingSpace.getSpaceType());
        dto.setStatus(parkingSpace.getStatus());
        dto.setLatitude(parkingSpace.getLatitude());
        dto.setLongitude(parkingSpace.getLongitude());
        dto.setBindUserId(parkingSpace.getBindUserId());
        dto.setCreateTime(parkingSpace.getCreateTime());
        dto.setUpdateTime(parkingSpace.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为ParkingSpace实体
     */
    public ParkingSpace toParkingSpace() {
        ParkingSpace parkingSpace = new ParkingSpace();
        parkingSpace.setId(this.id);
        parkingSpace.setParkAreaId(this.parkAreaId);
        parkingSpace.setZoneId(this.zoneId);
        parkingSpace.setSpaceNumber(this.spaceNumber);
        parkingSpace.setSpaceType(this.spaceType);
        parkingSpace.setStatus(this.status);
        parkingSpace.setLatitude(this.latitude);
        parkingSpace.setLongitude(this.longitude);
        parkingSpace.setBindUserId(this.bindUserId);
        
        return parkingSpace;
    }
    
    /**
     * 转换为ParkingSpace实体（包含版本号）
     */
    public ParkingSpace toParkingSpaceWithVersion(Integer version) {
        ParkingSpace parkingSpace = this.toParkingSpace();
        parkingSpace.setVersion(version);
        return parkingSpace;
    }
}
