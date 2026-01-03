package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Token黑名单实体类
 */
@Data
@TableName("sys_token_blacklist")
public class TokenBlacklist {
    
    /**
     * 黑名单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * JWT token
     */
    private String token;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 加入黑名单原因：1-用户登出，2-密码修改，3-管理员强制下线
     */
    private Integer reason;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
