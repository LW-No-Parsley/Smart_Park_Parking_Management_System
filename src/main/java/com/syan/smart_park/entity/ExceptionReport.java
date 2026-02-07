package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常上报实体类
 * 对应数据库表：exception_report
 */
@Data
@TableName("exception_report")
public class ExceptionReport {
    
    /**
     * 异常ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 上报用户ID
     */
    private Long userId;
    
    /**
     * 关联车位ID
     */
    private Long spaceId;
    
    /**
     * 异常类型：1-车位被占，2-设备故障，3-违停，4-其他
     */
    private Integer reportType;
    
    /**
     * 异常描述
     */
    private String description;
    
    /**
     * 图片地址
     */
    private String imageUrl;
    
    /**
     * 处理状态：0-未处理，1-已受理，2-已处理，3-已关闭
     */
    private Integer status;
    
    /**
     * 处理人员ID（后台 sys_user.id）
     */
    private Long handledBy;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
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
