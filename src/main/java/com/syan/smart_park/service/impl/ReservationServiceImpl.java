package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final ParkUserMapper parkUserMapper;
    private final VehicleMapper vehicleMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = this.list();
        List<ReservationDTO> reservationDTOs = reservations.stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());
        
        // 获取所有用户ID、车辆ID、车位ID
        List<Long> userIds = reservationDTOs.stream()
                .map(ReservationDTO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> vehicleIds = reservationDTOs.stream()
                .map(ReservationDTO::getVehicleId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> spaceIds = reservationDTOs.stream()
                .map(ReservationDTO::getSpaceId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, String> userMap = userIds.isEmpty() ? Map.of() : 
            parkUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(ParkUser::getId, ParkUser::getUsername));
        
        // 批量查询车辆信息
        Map<Long, String> vehicleMap = vehicleIds.isEmpty() ? Map.of() :
            vehicleMapper.selectBatchIds(vehicleIds).stream()
                .collect(Collectors.toMap(Vehicle::getId, Vehicle::getPlateNumber));
        
        // 批量查询车位信息
        Map<Long, String> spaceMap = spaceIds.isEmpty() ? Map.of() :
            parkingSpaceMapper.selectBatchIds(spaceIds).stream()
                .collect(Collectors.toMap(ParkingSpace::getId, ParkingSpace::getSpaceNumber));
        
        // 填充DTO中的用户名、车牌号、车位编号
        for (ReservationDTO dto : reservationDTOs) {
            dto.setUsername(userMap.get(dto.getUserId()));
            dto.setPlateNumber(vehicleMap.get(dto.getVehicleId()));
            dto.setSpaceNumber(spaceMap.get(dto.getSpaceId()));
        }
        
        return reservationDTOs;
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
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation existingReservation = reservationMapper.selectForUpdate(id);
        if (existingReservation == null) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1300,
                "预约不存在或已被删除"
            );
        }
        
        // 检查车位是否可用（排除当前预约）
        if (!isSpaceAvailable(reservationDTO.getSpaceId(), reservationDTO.getStartTime(), 
                reservationDTO.getEndTime(), id)) {
            throw new RuntimeException("车位在指定时间不可用");
        }
        
        // 保存原始版本号
        Integer originalVersion = existingReservation.getVersion();
        
        Reservation reservation = reservationDTO.toReservation();
        reservation.setId(id);
        reservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        boolean updated = this.update(reservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
        
        if (!updated) {
            throw new RuntimeException("更新失败，可能已被其他用户修改");
        }
        
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
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 检查审批人是否存在（如果提供了审批人ID）
        if (approvedBy != null) {
            // 这里应该检查用户是否存在，但为了简化，我们暂时不检查
            // 在实际项目中，应该调用UserService检查用户是否存在
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setApprovalStatus(1); // 通过
        updateReservation.setApprovedBy(approvedBy);
        updateReservation.setApprovedTime(LocalDateTime.now());
        updateReservation.setRejectReason(rejectReason);
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
    }
    
    @Override
    @Transactional
    public boolean rejectReservation(Long id, Long approvedBy, String rejectReason) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setApprovalStatus(2); // 拒绝
        updateReservation.setApprovedBy(approvedBy);
        updateReservation.setApprovedTime(LocalDateTime.now());
        updateReservation.setRejectReason(rejectReason);
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
    }
    
    @Override
    @Transactional
    public boolean updateReservationStatus(Long id, Integer status) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setStatus(status);
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
    }
    
    @Override
    @Transactional
    public boolean updatePaymentStatus(Long id, Integer payStatus, BigDecimal paidAmount) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setPayStatus(payStatus);
        updateReservation.setPaidAmount(paidAmount);
        if (payStatus == 1) { // 已支付
            updateReservation.setSettlementTime(LocalDateTime.now());
        }
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
    }
    
    @Override
    @Transactional
    public boolean recordArrival(Long id, LocalDateTime arriveTime) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setArriveTime(arriveTime);
        updateReservation.setStatus(2); // 已使用
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
    }
    
    @Override
    @Transactional
    public boolean recordDeparture(Long id, LocalDateTime leaveTime, BigDecimal totalFee) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }
        
        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();
        
        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setLeaveTime(leaveTime);
        updateReservation.setTotalFee(totalFee);
        updateReservation.setSettlementTime(LocalDateTime.now());
        updateReservation.setVersion(originalVersion);
        
        // 使用update方法并指定版本号条件
        return this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
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
