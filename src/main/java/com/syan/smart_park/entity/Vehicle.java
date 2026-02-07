package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆实体类
 * 对应数据库表：vehicle
 */
@Data
@TableName("vehicle")
public class Vehicle {
    
    /**
     * 车辆ID
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
