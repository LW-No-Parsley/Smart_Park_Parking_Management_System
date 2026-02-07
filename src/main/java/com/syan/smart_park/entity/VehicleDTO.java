package com.syan.smart_park.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆DTO
 */
@Data
public class VehicleDTO {
    
    /**
     * 车辆ID
     */
    private Long id;
    
    /**
     * 用户ID（关联 park_user.id）
     */
    private Long userId;
    
    /**
     * 车牌号
     */
    private String plateNumber;
    
    /**
     * 是否默认车牌：0-否，1-是
     */
    private Integer isDefault;
    
    /**
     * 车辆类型：1-小车，2-大车，3-新能源车
     */
    private Integer vehicleType;
    
    /**
     * 车辆品牌
     */
    private String brand;
    
    /**
     * 车辆颜色
     */
    private String color;
    
    /**
     * 车辆状态：0-禁用，1-正常
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
     * 静态方法：从Vehicle实体转换为VehicleDTO
     */
    public static VehicleDTO fromVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setUserId(vehicle.getUserId());
        dto.setPlateNumber(vehicle.getPlateNumber());
        dto.setIsDefault(vehicle.getIsDefault());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setBrand(vehicle.getBrand());
        dto.setColor(vehicle.getColor());
        dto.setStatus(vehicle.getStatus());
        dto.setCreateTime(vehicle.getCreateTime());
        dto.setUpdateTime(vehicle.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 转换为Vehicle实体
     */
    public Vehicle toVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(this.id);
        vehicle.setUserId(this.userId);
        vehicle.setPlateNumber(this.plateNumber);
        vehicle.setIsDefault(this.isDefault);
        vehicle.setVehicleType(this.vehicleType);
        vehicle.setBrand(this.brand);
        vehicle.setColor(this.color);
        vehicle.setStatus(this.status);
        
        return vehicle;
    }
}
