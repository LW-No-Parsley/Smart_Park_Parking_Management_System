package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车位实体类
 * 对应数据库表：parking_space
 */
@Data
@TableName("parking_space")
public class ParkingSpace {
    
    /**
     * 车位ID
     */
    @TableId(type = IdType.AUTO)
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
     * 车位状态：0-禁用，1-空闲，2-已预约，3-已占用，4-故障
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
    
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
