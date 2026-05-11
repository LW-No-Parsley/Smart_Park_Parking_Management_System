package com.syan.smart_park.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
     * 用户名（关联 park_user.username）
     */
    private String username;
    
    /**
     * 车牌号
     */
    @Size(min = 5, max = 10)
    private String plateNumber;
    
    /**
     * 是否默认车牌：0-否，1-是
     */
    @Min(0) @Max(1)
    private Integer isDefault;

    /**
     * 车辆类型：1-小车，2-大车，3-新能源车
     */
    @Min(1) @Max(3)
    private Integer vehicleType;
    
    /**
     * 车辆品牌
     */
    @Size(max = 50)
    private String brand;

    /**
     * 车辆颜色
     */
    @Size(max = 20)
    private String color;
    
    /**
     * 车辆状态：0-禁用，1-正常
     */
    @Min(0) @Max(1)
    private Integer status;

    /**
     * 绑定的固定车位ID（创建车辆时指定，不传则自动查找业主绑定的车位）
     */
    private Long spaceId;
    
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
