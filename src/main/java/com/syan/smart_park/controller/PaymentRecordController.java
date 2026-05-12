package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
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
     * 统一分页查询支付记录列表（支持多条件组合筛选）
     * 合并了原 /reservation/* /user/* /payment-method/* /payment-status/* /time-range 等路由
     */
    @GetMapping("/list")
    @RequirePermission("payment:list")
    public R<PageResult<PaymentRecordDTO>> getPaymentRecordList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer paymentMethod,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        PageResult<PaymentRecordDTO> result = paymentRecordService.pagePaymentRecords(
                current, size, reservationId, userId, paymentMethod, paymentStatus,
                startTime, endTime);
        return R.success(result);
    }
    
    /**
     * 根据ID获取支付记录详情
     */
    @GetMapping("/{id}")
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:list")
    public R<PaymentRecordDTO> updatePaymentRecord(@PathVariable Long id, @Valid @RequestBody PaymentRecordDTO paymentRecordDTO) {
        PaymentRecordDTO updatedPaymentRecord = paymentRecordService.updatePaymentRecord(id, paymentRecordDTO);
        if (updatedPaymentRecord == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedPaymentRecord);
    }
    
    /**
     * 获取指定时间范围内的支付总额
     */
    @GetMapping("/total-amount/time-range")
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:list")
    public R<BigDecimal> getUserTotalPaymentAmount(@PathVariable Long userId) {
        BigDecimal totalAmount = paymentRecordService.getUserTotalPaymentAmount(userId);
        return R.success(totalAmount);
    }
    
    /**
     * 更新支付状态
     */
    @PutMapping("/{id}/payment-status")
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:list")
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
    @RequirePermission("payment:refund")
    public R<Boolean> handleRefund(@PathVariable Long id,
                                   @RequestParam String refundTransactionId) {
        boolean result = paymentRecordService.handleRefund(id, refundTransactionId);
        if (!result) {
            return R.error(ReturnCode.RC500); // 处理失败
        }
        return R.success(true);
    }
    
    /**
     * 批量更新支付状态
     */
    @PutMapping("/batch-update-payment-status")
    @RequirePermission("payment:list")
    public R<Boolean> batchUpdatePaymentStatus(@RequestParam List<Long> ids,
                                               @RequestParam Integer paymentStatus) {
        boolean result = paymentRecordService.batchUpdatePaymentStatus(ids, paymentStatus);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
}
