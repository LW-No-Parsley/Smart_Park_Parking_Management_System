package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.PaymentRecordDTO;
import com.syan.smart_park.entity.ReservationDTO;
import com.syan.smart_park.service.PaymentRecordService;
import com.syan.smart_park.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/reservation")
@RequiredArgsConstructor
public class AppReservationController {

    private final ReservationService reservationService;
    private final PaymentRecordService paymentRecordService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }

    @PostMapping
    public R<ReservationDTO> createReservation(@RequestBody CreateReservationRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setUserId(userId);
        dto.setVehicleId(request.getVehicleId());
        dto.setSpaceId(request.getSpaceId());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setReservationType(1);
        dto.setSource(1);
        dto.setApprovalStatus(0); // 小程序提交默认为待审批

        ReservationDTO result = reservationService.createReservation(dto);
        if (result == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(result);
    }

    @GetMapping("/my")
    public R<PageResult<ReservationDTO>> getMyReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        PageResult<ReservationDTO> pageResult = reservationService.listReservations(
                userId, null, null, null, null, null, null, null, null, null, page, size);
        return R.success(pageResult);
    }

    @GetMapping("/{id}")
    public R<ReservationDTO> getReservationById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        ReservationDTO reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return R.error(ReturnCode.RC500);
        }
        if (!reservation.getUserId().equals(userId)) {
            return R.error(ReturnCode.RC403);
        }
        return R.success(reservation);
    }

    @PostMapping("/{id}/cancel")
    public R<Boolean> cancelReservation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        ReservationDTO reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return R.error(ReturnCode.RC500);
        }
        if (!reservation.getUserId().equals(userId)) {
            return R.error(ReturnCode.RC403);
        }

        boolean success = reservationService.updateReservationStatus(id, 0);
        return R.success(success);
    }

    /**
     * 获取当前用户的订单统计数据
     */
    @GetMapping("/stats")
    public R<Map<String, Integer>> getOrderStats() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        // 获取所有订单并按状态分类统计
        PageResult<ReservationDTO> all = reservationService.listReservations(
                userId, null, null, null, null, null, null, null, null, null, 1, Integer.MAX_VALUE);
        List<ReservationDTO> list = all.getRecords();
        int pending = 0, active = 0, used = 0, cancelled = 0, expired = 0;
        for (ReservationDTO r : list) {
            if (r.getApprovalStatus() != null && r.getApprovalStatus() == 0) pending++;
            if (r.getStatus() != null) {
                switch (r.getStatus()) {
                    case 1 -> active++;
                    case 2 -> used++;
                    case 0 -> cancelled++;
                    case 3 -> expired++;
                }
            }
        }
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", list.size());
        stats.put("pending", pending);
        stats.put("active", active);
        stats.put("used", used);
        stats.put("cancelled", cancelled);
        stats.put("expired", expired);
        return R.success(stats);
    }

    /**
     * 获取当前用户某订单的支付记录
     */
    @GetMapping("/{id}/payments")
    public R<List<PaymentRecordDTO>> getOrderPayments(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        // 校验订单归属
        ReservationDTO reservation = reservationService.getReservationById(id);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            return R.error(ReturnCode.RC403);
        }
        PageResult<PaymentRecordDTO> pageResult = paymentRecordService.pagePaymentRecords(
                1, Integer.MAX_VALUE, id, null, null, null, null, null);
        return R.success(pageResult.getRecords());
    }

    @Data
    public static class CreateReservationRequest {
        private Long vehicleId;
        private Long spaceId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }
}
