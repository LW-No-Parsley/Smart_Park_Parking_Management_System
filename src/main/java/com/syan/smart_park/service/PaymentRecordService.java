package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.PaymentRecord;
import com.syan.smart_park.entity.PaymentRecordDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录服务接口
 */
public interface PaymentRecordService extends IService<PaymentRecord> {
    
    /**
     * 获取所有支付记录
     */
    List<PaymentRecordDTO> getAllPaymentRecords();
    
    /**
     * 根据ID获取支付记录
     */
    PaymentRecordDTO getPaymentRecordById(Long id);
    
    /**
     * 创建支付记录
     */
    PaymentRecordDTO createPaymentRecord(PaymentRecordDTO paymentRecordDTO);
    
    /**
     * 更新支付记录
     */
    PaymentRecordDTO updatePaymentRecord(Long id, PaymentRecordDTO paymentRecordDTO);
    
    /**
     * 根据预约ID获取支付记录列表
     */
    List<PaymentRecordDTO> getPaymentRecordsByReservationId(Long reservationId);
    
    /**
     * 根据用户ID获取支付记录列表
     */
    List<PaymentRecordDTO> getPaymentRecordsByUserId(Long userId);
    
    /**
     * 根据支付方式获取支付记录列表
     */
    List<PaymentRecordDTO> getPaymentRecordsByPaymentMethod(Integer paymentMethod);
    
    /**
     * 根据支付状态获取支付记录列表
     */
    List<PaymentRecordDTO> getPaymentRecordsByPaymentStatus(Integer paymentStatus);
    
    /**
     * 根据时间范围获取支付记录列表
     */
    List<PaymentRecordDTO> getPaymentRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取指定时间范围内的支付总额
     */
    BigDecimal getTotalPaymentAmountByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取用户的支付总额
     */
    BigDecimal getUserTotalPaymentAmount(Long userId);
    
    /**
     * 更新支付状态
     */
    boolean updatePaymentStatus(Long id, Integer paymentStatus, String transactionId, LocalDateTime paymentTime);
    
    /**
     * 处理支付成功回调
     */
    boolean handlePaymentSuccess(Long id, String transactionId, LocalDateTime paymentTime);
    
    /**
     * 处理支付失败回调
     */
    boolean handlePaymentFailure(Long id, String transactionId);
    
    /**
     * 处理退款
     */
    boolean handleRefund(Long id, String refundTransactionId);
    
    /**
     * 获取待处理的支付记录
     */
    List<PaymentRecordDTO> getPendingPaymentRecords();
    
    /**
     * 批量更新支付状态
     */
    boolean batchUpdatePaymentStatus(List<Long> ids, Integer paymentStatus);
}
