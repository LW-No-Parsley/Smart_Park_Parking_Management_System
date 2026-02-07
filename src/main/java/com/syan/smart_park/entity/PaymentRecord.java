package com.syan.smart_park.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 对应数据库表：payment_record
 */
@Data
@TableName("payment_record")
public class PaymentRecord {
    
    /**
     * 支付记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 预约ID
     */
    private Long reservationId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式：1-微信支付，2-支付宝，3-余额支付
     */
    private Integer paymentMethod;
    
    /**
     * 第三方支付交易ID
     */
    private String transactionId;
    
    /**
     * 支付状态：0-未支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
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
