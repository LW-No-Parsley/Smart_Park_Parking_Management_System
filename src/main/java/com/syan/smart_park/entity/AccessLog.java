package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 进出记录实体类
 * 对应数据库表：access_log
 */
@Data
@TableName("access_log")
public class AccessLog {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 园区ID
     */
    private Long parkAreaId;
    
    /**
     * 道闸ID
     */
    private Long gateId;
    
    /**
     * 识别车牌号
     */
    private String plateNumber;
    
    /**
     * 关联车辆ID（可为空）
     */
    private Long vehicleId;
    
    /**
     * 进出类型：1-入场，2-出场
     */
    private Integer accessType;
    
    /**
     * 抓拍图片地址
     */
    private String imageUrl;
    
    /**
     * 识别结果：0-失败，1-成功，2-黑名单
     */
    private Integer recognitionResult;
    
    /**
     * 通行时间
     */
    private LocalDateTime accessTime;
    
    /**
     * 处理人员ID（后台 sys_user.id，用于手动干预）
     */
    private Long handledBy;
    
    /**
     * 备注（手动放行原因等）
     */
    private String remark;
    
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
