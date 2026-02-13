package com.syan.smart_park.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位分区DTO
 */
@Data
public class ParkingZoneDTO {
    
    /**
     * 分区ID
     */
    private Long id;
    
    /**
     * 园区ID
     */
    @JsonIgnore
    private Long parkAreaId;
    
    /**
     * 园区名称
     */
    private String parkAreaName;
    
    /**
     * 分区名称（如A区、B区）
     */
    private String zoneName;
    
    /**
     * 分区描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
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
     * 静态方法：从ParkingZone实体转换为ParkingZoneDTO
     */
    public static ParkingZoneDTO fromParkingZone(ParkingZone parkingZone) {
        if (parkingZone == null) {
            return null;
        }
        
        ParkingZoneDTO dto = new ParkingZoneDTO();
        dto.setId(parkingZone.getId());
        dto.setParkAreaId(parkingZone.getParkAreaId());
        dto.setZoneName(parkingZone.getZoneName());
        dto.setDescription(parkingZone.getDescription());
        dto.setSortOrder(parkingZone.getSortOrder());
        dto.setStatus(parkingZone.getStatus());
        dto.setCreateTime(parkingZone.getCreateTime());
        dto.setUpdateTime(parkingZone.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为ParkingZone实体
     */
    public ParkingZone toParkingZone() {
        ParkingZone parkingZone = new ParkingZone();
        parkingZone.setId(this.id);
        parkingZone.setParkAreaId(this.parkAreaId);
        parkingZone.setZoneName(this.zoneName);
        parkingZone.setDescription(this.description);
        parkingZone.setSortOrder(this.sortOrder);
        parkingZone.setStatus(this.status);
        
        return parkingZone;
    }
}
