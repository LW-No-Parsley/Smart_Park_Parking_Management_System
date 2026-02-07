package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 道闸设备实体类
 * 对应数据库表：gate_device
 */
@Data
@TableName("gate_device")
public class GateDevice {
    
    /**
     * 设备ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
    /**
     * 道闸名称（如东门入口、西门出口等）
     */
    private String gateName;
    
    /**
     * 设备序列号
     */
    private String deviceSn;
    
    /**
     * 设备IP地址
     */
    private String ipAddress;
    
    /**
     * 设备类型：1-入口道闸，2-出口道闸
     */
    private Integer deviceType;
    
    /**
     * 设备状态：0-离线，1-在线，2-故障
     */
    private Integer status;
    
    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;
    
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
