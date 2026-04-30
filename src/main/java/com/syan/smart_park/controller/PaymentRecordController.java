package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.PaymentRecordDTO;
import com.syan.smart_park.service.PaymentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录控制器
 */
@RestController
@RequestMapping("/api/payment-record")
@RequiredArgsConstructor
public class PaymentRecordController {
    
    private final PaymentRecordService paymentRecordService;
    
    /**
     * 获取所有支付记录列表
     */
    @GetMapping("/list")
    public R<List<PaymentRecordDTO>> getAllPaymentRecords() {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getAllPaymentRecords();
        return R.success(paymentRecords);
    }
    
    /**
     * 根据ID获取支付记录详情
     */
    @GetMapping("/{id}")
    public R<PaymentRecordDTO> getPaymentRecordById(@PathVariable Long id) {
        PaymentRecordDTO paymentRecord = paymentRecordService.getPaymentRecordById(id);
        if (paymentRecord == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(paymentRecord);
    }
    
    /**
     * 创建支付记录
     */
    @PostMapping
    public R<PaymentRecordDTO> createPaymentRecord(@Valid @RequestBody PaymentRecordDTO paymentRecordDTO) {
        PaymentRecordDTO createdPaymentRecord = paymentRecordService.createPaymentRecord(paymentRecordDTO);
        if (createdPaymentRecord == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdPaymentRecord);
    }
    
    /**
     * 更新支付记录
     */
    @PutMapping("/{id}")
    public R<PaymentRecordDTO> updatePaymentRecord(@PathVariable Long id, @Valid @RequestBody PaymentRecordDTO paymentRecordDTO) {
        PaymentRecordDTO updatedPaymentRecord = paymentRecordService.updatePaymentRecord(id, paymentRecordDTO);
        if (updatedPaymentRecord == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedPaymentRecord);
    }
    
    /**
     * 根据预约ID获取支付记录列表
     */
    @GetMapping("/reservation/{reservationId}")
    public R<List<PaymentRecordDTO>> getPaymentRecordsByReservationId(@PathVariable Long reservationId) {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPaymentRecordsByReservationId(reservationId);
        return R.success(paymentRecords);
    }
    
    /**
     * 根据用户ID获取支付记录列表
     */
    @GetMapping("/user/{userId}")
    public R<List<PaymentRecordDTO>> getPaymentRecordsByUserId(@PathVariable Long userId) {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPaymentRecordsByUserId(userId);
        return R.success(paymentRecords);
    }
    
    /**
     * 根据支付方式获取支付记录列表
     */
    @GetMapping("/payment-method/{paymentMethod}")
    public R<List<PaymentRecordDTO>> getPaymentRecordsByPaymentMethod(@PathVariable Integer paymentMethod) {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPaymentRecordsByPaymentMethod(paymentMethod);
        return R.success(paymentRecords);
    }
    
    /**
     * 根据支付状态获取支付记录列表
     */
    @GetMapping("/payment-status/{paymentStatus}")
    public R<List<PaymentRecordDTO>> getPaymentRecordsByPaymentStatus(@PathVariable Integer paymentStatus) {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPaymentRecordsByPaymentStatus(paymentStatus);
        return R.success(paymentRecords);
    }
    
    /**
     * 根据时间范围获取支付记录列表
     */
    @GetMapping("/time-range")
    public R<List<PaymentRecordDTO>> getPaymentRecordsByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPaymentRecordsByTimeRange(startTime, endTime);
        return R.success(paymentRecords);
    }
    
    /**
     * 获取指定时间范围内的支付总额
     */
    @GetMapping("/total-amount/time-range")
    public R<BigDecimal> getTotalPaymentAmountByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        BigDecimal totalAmount = paymentRecordService.getTotalPaymentAmountByTimeRange(startTime, endTime);
        return R.success(totalAmount);
    }
    
    /**
     * 获取用户的支付总额
     */
    @GetMapping("/user/{userId}/total-amount")
    public R<BigDecimal> getUserTotalPaymentAmount(@PathVariable Long userId) {
        BigDecimal totalAmount = paymentRecordService.getUserTotalPaymentAmount(userId);
        return R.success(totalAmount);
    }
    
    /**
     * 更新支付状态
     */
    @PutMapping("/{id}/payment-status")
    public R<Boolean> updatePaymentStatus(@PathVariable Long id,
                                          @RequestParam Integer paymentStatus,
                                          @RequestParam(required = false) String transactionId,
                                          @RequestParam(required = false) LocalDateTime paymentTime) {
        boolean result = paymentRecordService.updatePaymentStatus(id, paymentStatus, transactionId, paymentTime);
        if (!result) {
            return R.error(ReturnCode.RC500); // 更新失败
        }
        return R.success(true);
    }
    
    /**
     * 处理支付成功回调
     */
    @PutMapping("/{id}/payment-success")
    public R<Boolean> handlePaymentSuccess(@PathVariable Long id,
                                           @RequestParam String transactionId,
                                           @RequestParam LocalDateTime paymentTime) {
        boolean result = paymentRecordService.handlePaymentSuccess(id, transactionId, paymentTime);
        if (!result) {
            return R.error(ReturnCode.RC500); // 处理失败
        }
        return R.success(true);
    }
    
    /**
     * 处理支付失败回调
     */
    @PutMapping("/{id}/payment-failure")
    public R<Boolean> handlePaymentFailure(@PathVariable Long id,
                                           @RequestParam String transactionId) {
        boolean result = paymentRecordService.handlePaymentFailure(id, transactionId);
        if (!result) {
            return R.error(ReturnCode.RC500); // 处理失败
        }
        return R.success(true);
    }
    
    /**
     * 处理退款
     */
    @PutMapping("/{id}/refund")
    public R<Boolean> handleRefund(@PathVariable Long id,
                                   @RequestParam String refundTransactionId) {
        boolean result = paymentRecordService.handleRefund(id, refundTransactionId);
        if (!result) {
            return R.error(ReturnCode.RC500); // 处理失败
        }
        return R.success(true);
    }
    
    /**
     * 获取待处理的支付记录
     */
    @GetMapping("/pending")
    public R<List<PaymentRecordDTO>> getPendingPaymentRecords() {
        List<PaymentRecordDTO> paymentRecords = paymentRecordService.getPendingPaymentRecords();
        return R.success(paymentRecords);
    }
    
    /**
     * 批量更新支付状态
     */
    @PutMapping("/batch-update-payment-status")
    public R<Boolean> batchUpdatePaymentStatus(@RequestParam List<Long> ids,
                                               @RequestParam Integer paymentStatus) {
        boolean result = paymentRecordService.batchUpdatePaymentStatus(ids, paymentStatus);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
}
