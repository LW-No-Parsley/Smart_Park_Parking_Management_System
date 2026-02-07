package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位分区实体类
 * 对应数据库表：parking_zone
 */
@Data
@TableName("parking_zone")
public class ParkingZone {
    
    /**
     * 分区ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
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
