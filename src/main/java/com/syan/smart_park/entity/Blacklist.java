package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单实体类
 * 对应数据库表：blacklist
 */
@Data
@TableName("blacklist")
public class Blacklist {
    
    /**
     * 黑名单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 车牌号
     */
    private String plateNumber;
    
    /**
     * 加入原因
     */
    private String reason;
    
    /**
     * 创建人（sys_user.id）
     */
    private Long createdBy;
    
    /**
     * 更新人（sys_user.id）
     */
    private Long updatedBy;
    
    /**
     * 生效时间
     */
    private LocalDateTime startTime;
    
    /**
     * 失效时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：0-禁用，1-生效
     */
    private Integer status;
    
    /**
     * 所属园区ID
     */
    private Long parkAreaId;
    
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
