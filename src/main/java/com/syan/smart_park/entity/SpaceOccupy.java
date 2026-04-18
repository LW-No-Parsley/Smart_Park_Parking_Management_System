package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 占用开始时间
     */
    private LocalDateTime startTime;

    /**
     * 占用结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
