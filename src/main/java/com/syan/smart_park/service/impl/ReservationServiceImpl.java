package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ReservationMapper;
import com.syan.smart_park.entity.Reservation;
import com.syan.smart_park.entity.ReservationDTO;
import com.syan.smart_park.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final ReservationMapper reservationMapper;

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = this.list();
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = this.getById(id);
        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    @Transactional
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        // 检查车位是否可用
        if (!isSpaceAvailable(reservationDTO.getSpaceId(), reservationDTO.getStartTime(), 
                reservationDTO.getEndTime(), null)) {
            throw new RuntimeException("车位在指定时间不可用");
        }
        
        Reservation reservation = reservationDTO.toReservation();
        this.save(reservation);
        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) {
        Reservation existingReservation = this.getById(id);
        if (existingReservation == null) {
            return null;
        }
        
        // 检查车位是否可用（排除当前预约）
        if (!isSpaceAvailable(reservationDTO.getSpaceId(), reservationDTO.getStartTime(), 
                reservationDTO.getEndTime(), id)) {
            throw new RuntimeException("车位在指定时间不可用");
        }
        
        Reservation reservation = reservationDTO.toReservation();
        reservation.setId(id);
        this.updateById(reservation);
        
        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    @Transactional
    public boolean deleteReservation(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<ReservationDTO> getReservationsByUserId(Long userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getVehicleId, vehicleId);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsBySpaceId(Long spaceId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getSpaceId, spaceId);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByType(Integer reservationType) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getReservationType, reservationType);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByApprovalStatus(Integer approvalStatus) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getApprovalStatus, approvalStatus);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByStatus(Integer status) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getStatus, status);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByPayStatus(Integer payStatus) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getPayStatus, payStatus);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsBySource(Integer source) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getSource, source);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Reservation::getStartTime, startTime)
                   .le(Reservation::getEndTime, endTime);
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean approveReservation(Long id, Long approvedBy, String rejectReason) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setApprovalStatus(1); // 通过
        reservation.setApprovedBy(approvedBy);
        reservation.setApprovedTime(LocalDateTime.now());
        reservation.setRejectReason(rejectReason);
        
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean rejectReservation(Long id, Long approvedBy, String rejectReason) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setApprovalStatus(2); // 拒绝
        reservation.setApprovedBy(approvedBy);
        reservation.setApprovedTime(LocalDateTime.now());
        reservation.setRejectReason(rejectReason);
        
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean updateReservationStatus(Long id, Integer status) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setStatus(status);
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(Long id, Integer payStatus, BigDecimal paidAmount) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setPayStatus(payStatus);
        reservation.setPaidAmount(paidAmount);
        if (payStatus == 1) { // 已支付
            reservation.setSettlementTime(LocalDateTime.now());
        }
        
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean recordArrival(Long id, LocalDateTime arriveTime) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setArriveTime(arriveTime);
        reservation.setStatus(2); // 已使用
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean recordDeparture(Long id, LocalDateTime leaveTime, BigDecimal totalFee) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return false;
        }
        
        reservation.setLeaveTime(leaveTime);
        reservation.setTotalFee(totalFee);
        reservation.setSettlementTime(LocalDateTime.now());
        return this.updateById(reservation);
    }

    @Override
    @Transactional
    public boolean batchUpdateReservationStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<Reservation> reservations = ids.stream()
                .map(id -> {
                    Reservation reservation = new Reservation();
                    reservation.setId(id);
                    reservation.setStatus(status);
                    return reservation;
                })
                .collect(Collectors.toList());
        
        return this.updateBatchById(reservations);
    }

    @Override
    public List<ReservationDTO> getPendingApprovalReservations() {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getApprovalStatus, 0); // 待审批
        List<Reservation> reservations = this.list(queryWrapper);
        
        return reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDTO getUserCurrentValidReservation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                   .eq(Reservation::getApprovalStatus, 1) // 已通过
                   .eq(Reservation::getStatus, 1) // 已预约
                   .le(Reservation::getStartTime, now)
                   .ge(Reservation::getEndTime, now);
        
        Reservation reservation = this.getOne(queryWrapper);
        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    public boolean isSpaceAvailable(Long spaceId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getSpaceId, spaceId)
                   .eq(Reservation::getApprovalStatus, 1) // 已通过
                   .ne(Reservation::getStatus, 0) // 不是已取消
                   .and(wrapper -> wrapper
                       .and(inner -> inner
                           .le(Reservation::getStartTime, endTime)
                           .ge(Reservation::getEndTime, startTime))
                   );
        
        if (excludeReservationId != null) {
            queryWrapper.ne(Reservation::getId, excludeReservationId);
        }
        
        long count = this.count(queryWrapper);
        return count == 0;
    }
}
