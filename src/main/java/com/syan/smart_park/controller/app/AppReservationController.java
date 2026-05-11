package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ReservationDTO;
import com.syan.smart_park.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/app/reservation")
@RequiredArgsConstructor
public class AppReservationController {

    private final ReservationService reservationService;

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

        ReservationDTO result = reservationService.createReservation(dto);
        if (result == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(result);
    }

    @GetMapping("/my")
    public R<List<ReservationDTO>> getMyReservations() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        List<ReservationDTO> reservations = reservationService.getReservationsByUserId(userId);
        return R.success(reservations);
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
