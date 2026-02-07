package com.syan.smart_park.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录DTO
 */
@Data
public class PaymentRecordDTO {
    
    /**
     * 支付记录ID
     */
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
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 静态方法：从PaymentRecord实体转换为PaymentRecordDTO
     */
    public static PaymentRecordDTO fromPaymentRecord(PaymentRecord paymentRecord) {
        if (paymentRecord == null) {
            return null;
        }
        
        PaymentRecordDTO dto = new PaymentRecordDTO();
        dto.setId(paymentRecord.getId());
        dto.setReservationId(paymentRecord.getReservationId());
        dto.setUserId(paymentRecord.getUserId());
        dto.setAmount(paymentRecord.getAmount());
        dto.setPaymentMethod(paymentRecord.getPaymentMethod());
        dto.setTransactionId(paymentRecord.getTransactionId());
        dto.setPaymentStatus(paymentRecord.getPaymentStatus());
        dto.setPaymentTime(paymentRecord.getPaymentTime());
        dto.setCreateTime(paymentRecord.getCreateTime());
        dto.setUpdateTime(paymentRecord.getUpdateTime());
        dto.setDeleted(paymentRecord.getDeleted());
        
        return dto;
    }
    
    /**
     * 转换为PaymentRecord实体
     */
    public PaymentRecord toPaymentRecord() {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setId(this.id);
        paymentRecord.setReservationId(this.reservationId);
        paymentRecord.setUserId(this.userId);
        paymentRecord.setAmount(this.amount);
        paymentRecord.setPaymentMethod(this.paymentMethod);
        paymentRecord.setTransactionId(this.transactionId);
        paymentRecord.setPaymentStatus(this.paymentStatus);
        paymentRecord.setPaymentTime(this.paymentTime);
        paymentRecord.setDeleted(this.deleted);
        
        return paymentRecord;
    }
}
