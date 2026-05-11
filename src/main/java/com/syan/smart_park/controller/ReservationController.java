package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.FeeCalculationResult;
import com.syan.smart_park.entity.ReservationDTO;
import com.syan.smart_park.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * 获取所有预约列表
     */
    @GetMapping("/list")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> reservations = reservationService.getAllReservations();
        return R.success(reservations);
    }
    
    /**
     * 根据ID获取预约详情
     */
    @GetMapping("/{id}")
    @RequirePermission("reservation:list")
    public R<ReservationDTO> getReservationById(@PathVariable Long id) {
        ReservationDTO reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(reservation);
    }
    
    /**
     * 创建预约
     */
    @PostMapping
    @RequirePermission("reservation:list")
    public R<ReservationDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        // 覆盖客户端传入的业务敏感字段，防止越权操作
        reservationDTO.setApprovalStatus(null);
        reservationDTO.setPayStatus(null);
        reservationDTO.setTotalFee(null);
        reservationDTO.setPaidAmount(null);
        reservationDTO.setStatus(null);
        ReservationDTO createdReservation = reservationService.createReservation(reservationDTO);
        if (createdReservation == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdReservation);
    }

    /**
     * 更新预约
     */
    @PutMapping("/{id}")
    @RequirePermission("reservation:list")
    public R<ReservationDTO> updateReservation(@PathVariable Long id, @Valid @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO updatedReservation = reservationService.updateReservation(id, reservationDTO);
        if (updatedReservation == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(updatedReservation);
    }
    
    /**
     * 删除预约
     */
    @DeleteMapping("/{id}")
    @RequirePermission("reservation:cancel")
    public R<Boolean> deleteReservation(@PathVariable Long id) {
        boolean result = reservationService.deleteReservation(id);
        if (!result) {
            return R.error(ReturnCode.RC1300); // 数据不存在或删除失败
        }
        return R.success(true);
    }
    
    /**
     * 根据用户ID获取预约列表
     */
    @GetMapping("/user/{userId}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByUserId(@PathVariable Long userId) {
        List<ReservationDTO> reservations = reservationService.getReservationsByUserId(userId);
        return R.success(reservations);
    }
    
    /**
     * 根据车辆ID获取预约列表
     */
    @GetMapping("/vehicle/{vehicleId}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByVehicleId(@PathVariable Long vehicleId) {
        List<ReservationDTO> reservations = reservationService.getReservationsByVehicleId(vehicleId);
        return R.success(reservations);
    }
    
    /**
     * 根据车位ID获取预约列表
     */
    @GetMapping("/space/{spaceId}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsBySpaceId(@PathVariable Long spaceId) {
        List<ReservationDTO> reservations = reservationService.getReservationsBySpaceId(spaceId);
        return R.success(reservations);
    }
    
    /**
     * 根据预约类型获取预约列表
     */
    @GetMapping("/type/{reservationType}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByType(@PathVariable Integer reservationType) {
        List<ReservationDTO> reservations = reservationService.getReservationsByType(reservationType);
        return R.success(reservations);
    }
    
    /**
     * 根据审批状态获取预约列表
     */
    @GetMapping("/approval-status/{approvalStatus}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByApprovalStatus(@PathVariable Integer approvalStatus) {
        List<ReservationDTO> reservations = reservationService.getReservationsByApprovalStatus(approvalStatus);
        return R.success(reservations);
    }
    
    /**
     * 根据预约状态获取预约列表
     */
    @GetMapping("/status/{status}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByStatus(@PathVariable Integer status) {
        List<ReservationDTO> reservations = reservationService.getReservationsByStatus(status);
        return R.success(reservations);
    }
    
    /**
     * 根据支付状态获取预约列表
     */
    @GetMapping("/pay-status/{payStatus}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByPayStatus(@PathVariable Integer payStatus) {
        List<ReservationDTO> reservations = reservationService.getReservationsByPayStatus(payStatus);
        return R.success(reservations);
    }
    
    /**
     * 根据创建来源获取预约列表
     */
    @GetMapping("/source/{source}")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsBySource(@PathVariable Integer source) {
        List<ReservationDTO> reservations = reservationService.getReservationsBySource(source);
        return R.success(reservations);
    }
    
    /**
     * 获取指定时间范围内的预约列表
     */
    @GetMapping("/time-range")
    @RequirePermission("reservation:list")
    public R<List<ReservationDTO>> getReservationsByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<ReservationDTO> reservations = reservationService.getReservationsByTimeRange(startTime, endTime);
        return R.success(reservations);
    }
    
    /**
     * 审批预约（从Token中获取当前用户作为审批人）
     */
    @PutMapping("/{id}/approve")
    @RequirePermission("reservation:approve")
    public R<Boolean> approveReservation(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean result = reservationService.approveReservation(id, currentUserId, null);
        if (!result) {
            return R.error(ReturnCode.RC500); // 审批失败
        }
        return R.success(true);
    }
    
    /**
     * 拒绝预约（从Token中获取当前用户作为审批人）
     */
    @PutMapping("/{id}/reject")
    @RequirePermission("reservation:approve")
    public R<Boolean> rejectReservation(@PathVariable Long id,
                                        @RequestParam String rejectReason,
                                        Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean result = reservationService.rejectReservation(id, currentUserId, rejectReason);
        if (!result) {
            return R.error(ReturnCode.RC500); // 拒绝失败
        }
        return R.success(true);
    }
    
    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    @RequirePermission("reservation:list")
    public R<Boolean> updateReservationStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = reservationService.updateReservationStatus(id, status);
        if (!result) {
            return R.error(ReturnCode.RC500); // 更新失败
        }
        return R.success(true);
    }
    
    /**
     * 更新支付状态和金额
     */
    @PutMapping("/{id}/payment")
    @RequirePermission("reservation:list")
    public R<Boolean> updatePaymentStatus(@PathVariable Long id,
                                          @RequestParam Integer payStatus,
                                          @Positive @RequestParam BigDecimal paidAmount) {
        boolean result = reservationService.updatePaymentStatus(id, payStatus, paidAmount);
        if (!result) {
            return R.error(ReturnCode.RC500); // 更新失败
        }
        return R.success(true);
    }
    
    /**
     * 记录到达时间
     */
    @PutMapping("/{id}/arrive")
    @RequirePermission("reservation:list")
    public R<Boolean> recordArrival(@PathVariable Long id, @RequestParam LocalDateTime arriveTime) {
        boolean result = reservationService.recordArrival(id, arriveTime);
        if (!result) {
            return R.error(ReturnCode.RC500); // 记录失败
        }
        return R.success(true);
    }
    
    /**
     * 记录离开时间
     */
    @PutMapping("/{id}/leave")
    @RequirePermission("reservation:list")
    public R<Boolean> recordDeparture(@PathVariable Long id,
                                      @RequestParam LocalDateTime leaveTime,
                                      @Positive @RequestParam BigDecimal totalFee) {
        boolean result = reservationService.recordDeparture(id, leaveTime, totalFee);
        if (!result) {
            return R.error(ReturnCode.RC500); // 记录失败
        }
        return R.success(true);
    }
    
    /**
     * 批量更新预约状态
     */
    @PutMapping("/batch/update-status")
    @RequirePermission("reservation:list")
    public R<Boolean> batchUpdateReservationStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean result = reservationService.batchUpdateReservationStatus(ids, status);
        if (!result) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }
    
    /**
     * 获取待审批的预约列表
     */
    @GetMapping("/pending-approval")
    @RequirePermission("reservation:approve")
    public R<List<ReservationDTO>> getPendingApprovalReservations() {
        List<ReservationDTO> reservations = reservationService.getPendingApprovalReservations();
        return R.success(reservations);
    }
    
    /**
     * 获取用户当前有效的预约
     */
    @GetMapping("/user/{userId}/current-valid")
    @RequirePermission("reservation:list")
    public R<ReservationDTO> getUserCurrentValidReservation(@PathVariable Long userId) {
        ReservationDTO reservation = reservationService.getUserCurrentValidReservation(userId);
        if (reservation == null) {
            return R.error(ReturnCode.RC1300); // 数据不存在
        }
        return R.success(reservation);
    }
    
    /**
     * 检查车位在指定时间是否可用
     */
    @GetMapping("/check-availability")
    @RequirePermission("reservation:list")
    public R<Boolean> checkSpaceAvailability(@RequestParam Long spaceId,
                                             @RequestParam LocalDateTime startTime,
                                             @RequestParam LocalDateTime endTime,
                                             @RequestParam(required = false) Long excludeReservationId) {
        boolean isAvailable = reservationService.isSpaceAvailable(spaceId, startTime, endTime, excludeReservationId);
        return R.success(isAvailable);
    }

    /**
     * 记录离开时间（自动计算费用）
     * 根据到达时间/预约开始时间和离开时间，自动匹配计费规则计算停车费用
     */
    @PutMapping("/{id}/leave-auto-fee")
    @RequirePermission("reservation:list")
    public R<FeeCalculationResult> recordDepartureWithAutoFee(
            @PathVariable Long id,
            @RequestParam LocalDateTime leaveTime) {
        FeeCalculationResult feeResult = reservationService.recordDepartureWithAutoFee(id, leaveTime);
        return R.success(feeResult);
    }
}
