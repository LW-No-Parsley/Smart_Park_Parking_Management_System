package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
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
     * 统一分页查询支付记录（支持多条件组合筛选）
     *
     * @param current        当前页码
     * @param size           每页大小
     * @param reservationId  预约ID（可选）
     * @param userId         用户ID（可选）
     * @param paymentMethod  支付方式（可选）
     * @param paymentStatus  支付状态：0-未支付 1-成功 2-失败 3-已退款（可选）
     * @param startTime      支付开始时间（可选）
     * @param endTime        支付结束时间（可选）
     * @return 分页结果
     */
    PageResult<PaymentRecordDTO> pagePaymentRecords(long current, long size,
                                                    Long reservationId, Long userId,
                                                    Integer paymentMethod, Integer paymentStatus,
                                                    LocalDateTime startTime, LocalDateTime endTime);
    
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
