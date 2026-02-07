package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车场小程序用户实体类
 * 对应数据库表：park_user
 */
@Data
@TableName("park_user")
public class ParkUser {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 微信openid（用于微信登录）
     */
    private String openid;
    
    /**
     * 用户名（微信昵称）
     */
    private String username;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户类型：1-车主，2-访客
     */
    private Integer userType;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像地址
     */
    private String avatar;
    
    /**
     * 用户状态：0-禁用，1-正常
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
