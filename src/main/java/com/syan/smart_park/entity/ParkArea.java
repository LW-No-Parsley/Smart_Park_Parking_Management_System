package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 园区实体类
 * 对应数据库表：park_area
 */
@Data
@TableName("park_area")
public class ParkArea {
    
    /**
     * 园区ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 园区名称
     */
    private String name;
    
    /**
     * 园区地址
     */
    private String address;
    
    /**
     * 总车位数量
     */
    private Integer totalSpaces;
    
    /**
     * 主园区管理员ID（sys_user.id）
     */
    private Long primaryAdminUserId;
    
    /**
     * 园区纬度坐标
     */
    private BigDecimal latitude;
    
    /**
     * 园区经度坐标
     */
    private BigDecimal longitude;
    
    /**
     * 营业开始时间
     */
    private LocalTime businessHoursStart;
    
    /**
     * 营业结束时间
     */
    private LocalTime businessHoursEnd;
    
    /**
     * 园区状态：0-关闭，1-开放
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
