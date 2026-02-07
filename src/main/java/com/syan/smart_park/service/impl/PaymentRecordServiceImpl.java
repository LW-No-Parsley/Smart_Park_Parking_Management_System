package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.PaymentRecordMapper;
import com.syan.smart_park.entity.PaymentRecord;
import com.syan.smart_park.entity.PaymentRecordDTO;
import com.syan.smart_park.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {
    
    private final PaymentRecordMapper paymentRecordMapper;
    
    @Override
    public List<PaymentRecordDTO> getAllPaymentRecords() {
        List<PaymentRecord> paymentRecords = list();
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public PaymentRecordDTO getPaymentRecordById(Long id) {
        PaymentRecord paymentRecord = getById(id);
        return PaymentRecordDTO.fromPaymentRecord(paymentRecord);
    }
    
    @Override
    @Transactional
    public PaymentRecordDTO createPaymentRecord(PaymentRecordDTO paymentRecordDTO) {
        PaymentRecord paymentRecord = paymentRecordDTO.toPaymentRecord();
        save(paymentRecord);
        return PaymentRecordDTO.fromPaymentRecord(paymentRecord);
    }
    
    @Override
    @Transactional
    public PaymentRecordDTO updatePaymentRecord(Long id, PaymentRecordDTO paymentRecordDTO) {
        PaymentRecord existingPaymentRecord = getById(id);
        if (existingPaymentRecord == null) {
            return null;
        }
        
        PaymentRecord paymentRecord = paymentRecordDTO.toPaymentRecord();
        paymentRecord.setId(id);
        updateById(paymentRecord);
        
        return PaymentRecordDTO.fromPaymentRecord(paymentRecord);
    }
    
    @Override
    @Transactional
    public boolean deletePaymentRecord(Long id) {
        return removeById(id);
    }
    
    @Override
    public List<PaymentRecordDTO> getPaymentRecordsByReservationId(Long reservationId) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getReservationId, reservationId);
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRecordDTO> getPaymentRecordsByUserId(Long userId) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getUserId, userId);
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRecordDTO> getPaymentRecordsByPaymentMethod(Integer paymentMethod) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getPaymentMethod, paymentMethod);
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRecordDTO> getPaymentRecordsByPaymentStatus(Integer paymentStatus) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getPaymentStatus, paymentStatus);
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRecordDTO> getPaymentRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(PaymentRecord::getCreateTime, startTime)
                   .le(PaymentRecord::getCreateTime, endTime);
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal getTotalPaymentAmountByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(PaymentRecord::getPaymentTime, startTime)
                   .le(PaymentRecord::getPaymentTime, endTime)
                   .eq(PaymentRecord::getPaymentStatus, 1); // 只统计支付成功的
        
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal getUserTotalPaymentAmount(Long userId) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getUserId, userId)
                   .eq(PaymentRecord::getPaymentStatus, 1); // 只统计支付成功的
        
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    @Transactional
    public boolean updatePaymentStatus(Long id, Integer paymentStatus, String transactionId, LocalDateTime paymentTime) {
        PaymentRecord paymentRecord = getById(id);
        if (paymentRecord == null) {
            return false;
        }
        
        paymentRecord.setPaymentStatus(paymentStatus);
        if (transactionId != null) {
            paymentRecord.setTransactionId(transactionId);
        }
        if (paymentTime != null) {
            paymentRecord.setPaymentTime(paymentTime);
        }
        
        return updateById(paymentRecord);
    }
    
    @Override
    @Transactional
    public boolean handlePaymentSuccess(Long id, String transactionId, LocalDateTime paymentTime) {
        return updatePaymentStatus(id, 1, transactionId, paymentTime);
    }
    
    @Override
    @Transactional
    public boolean handlePaymentFailure(Long id, String transactionId) {
        return updatePaymentStatus(id, 2, transactionId, LocalDateTime.now());
    }
    
    @Override
    @Transactional
    public boolean handleRefund(Long id, String refundTransactionId) {
        PaymentRecord paymentRecord = getById(id);
        if (paymentRecord == null) {
            return false;
        }
        
        paymentRecord.setPaymentStatus(3); // 已退款
        paymentRecord.setTransactionId(refundTransactionId);
        
        return updateById(paymentRecord);
    }
    
    @Override
    public List<PaymentRecordDTO> getPendingPaymentRecords() {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getPaymentStatus, 0); // 未支付
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        
        return paymentRecords.stream()
                .map(PaymentRecordDTO::fromPaymentRecord)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean batchUpdatePaymentStatus(List<Long> ids, Integer paymentStatus) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PaymentRecord::getId, ids);
        
        List<PaymentRecord> paymentRecords = list(queryWrapper);
        if (paymentRecords.isEmpty()) {
            return false;
        }
        
        for (PaymentRecord paymentRecord : paymentRecords) {
            paymentRecord.setPaymentStatus(paymentStatus);
        }
        
        return updateBatchById(paymentRecords);
    }
}
