package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位占用记录实体类
 */
@Data
@TableName("space_occupy")
public class SpaceOccupy {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车位ID
     */
    private Long spaceId;

    /**
     * 预约ID
     */
    private Long reservationId;

    /**
     * 车辆ID（关联 vehicle.id，标记该占用由哪辆车产生）
     */
    private Long vehicleId;

    /**
     * 占用开始时间
     */
    private LocalDateTime startTime;

    /**
     * 占用结束时间（NULL表示仍在占用中）
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
