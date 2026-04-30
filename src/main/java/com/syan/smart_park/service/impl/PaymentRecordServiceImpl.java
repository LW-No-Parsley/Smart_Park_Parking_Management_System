package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.PaymentRecordMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.PaymentRecord;
import com.syan.smart_park.entity.PaymentRecordDTO;
import com.syan.smart_park.service.OperationLogService;
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
    private final OperationLogService operationLogService;
    
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
        
        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("支付记录管理");
        logDTO.setAction("创建支付记录");
        logDTO.setDetail("支付记录ID:" + paymentRecord.getId() + "，金额:" + paymentRecord.getAmount() + "，用户ID:" + paymentRecord.getUserId());
        operationLogService.createOperationLog(logDTO);
        
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
        
        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("支付记录管理");
        logDTO.setAction("更新支付记录");
        logDTO.setDetail("支付记录ID:" + id + "，金额:" + paymentRecord.getAmount());
        operationLogService.createOperationLog(logDTO);
        
        return PaymentRecordDTO.fromPaymentRecord(paymentRecord);
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
        
        boolean result = updateById(paymentRecord);
        
        if (result) {
            // 记录操作日志
            String statusText = paymentStatus == 0 ? "未支付" : paymentStatus == 1 ? "支付成功" : paymentStatus == 2 ? "支付失败" : "已退款";
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("支付记录管理");
            logDTO.setAction("更新支付状态");
            logDTO.setDetail("支付记录ID:" + id + "，状态:" + statusText + "，交易号:" + transactionId);
            operationLogService.createOperationLog(logDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean handlePaymentSuccess(Long id, String transactionId, LocalDateTime paymentTime) {
        boolean result = updatePaymentStatus(id, 1, transactionId, paymentTime);
        if (result) {
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("支付记录管理");
            logDTO.setAction("支付成功");
            logDTO.setDetail("支付记录ID:" + id + "，交易号:" + transactionId + "，支付时间:" + paymentTime);
            operationLogService.createOperationLog(logDTO);
        }
        return result;
    }
    
    @Override
    @Transactional
    public boolean handlePaymentFailure(Long id, String transactionId) {
        boolean result = updatePaymentStatus(id, 2, transactionId, LocalDateTime.now());
        if (result) {
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("支付记录管理");
            logDTO.setAction("支付失败");
            logDTO.setDetail("支付记录ID:" + id + "，交易号:" + transactionId);
            operationLogService.createOperationLog(logDTO);
        }
        return result;
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
        
        boolean result = updateById(paymentRecord);
        
        if (result) {
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("支付记录管理");
            logDTO.setAction("退款处理");
            logDTO.setDetail("支付记录ID:" + id + "，退款交易号:" + refundTransactionId);
            operationLogService.createOperationLog(logDTO);
        }
        
        return result;
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
