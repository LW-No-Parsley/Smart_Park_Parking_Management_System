package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Data
@TableName("sys_captcha")
public class Captcha {
    
    /**
     * 验证码ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 验证码标识（UUID）
     */
    @TableField("captcha_id")
    private String captchaKey;
    
    /**
     * 验证码值
     */
    @TableField("captcha_code")
    private String captchaValue;
    
    /**
     * 验证码类型：1-登录，2-注册，3-重置密码
     */
    private Integer captchaType;
    
    /**
     * 过期时间
     */
    @TableField("expiration_time")
    private LocalDateTime expireTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 使用状态：0-未使用，1-已使用
     */
    private Integer used;
}
