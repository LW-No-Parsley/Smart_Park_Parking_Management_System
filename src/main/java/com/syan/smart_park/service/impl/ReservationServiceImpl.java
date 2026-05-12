package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.exception.ConcurrentModificationException;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationMapper reservationMapper;
    private final ParkUserMapper parkUserMapper;
    private final VehicleMapper vehicleMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final SpaceOccupyMapper spaceOccupyMapper;
    private final FeeRuleService feeRuleService;
    private final ParkingSpaceService parkingSpaceService;
    private final PaymentRecordService paymentRecordService;
    private final OperationLogService operationLogService;

    @Override
    public PageResult<ReservationDTO> listReservations(Long userId, Long vehicleId, Long spaceId,
                                                       Integer reservationType, Integer approvalStatus,
                                                       Integer status, Integer payStatus, Integer source,
                                                       LocalDateTime startTime, LocalDateTime endTime,
                                                       Integer page, Integer size) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Reservation::getUserId, userId);
        }
        if (vehicleId != null) {
            queryWrapper.eq(Reservation::getVehicleId, vehicleId);
        }
        if (spaceId != null) {
            queryWrapper.eq(Reservation::getSpaceId, spaceId);
        }
        if (reservationType != null) {
            queryWrapper.eq(Reservation::getReservationType, reservationType);
        }
        if (approvalStatus != null) {
            queryWrapper.eq(Reservation::getApprovalStatus, approvalStatus);
        }
        if (status != null) {
            queryWrapper.eq(Reservation::getStatus, status);
        }
        if (payStatus != null) {
            queryWrapper.eq(Reservation::getPayStatus, payStatus);
        }
        if (source != null) {
            queryWrapper.eq(Reservation::getSource, source);
        }
        if (startTime != null) {
            queryWrapper.ge(Reservation::getStartTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(Reservation::getEndTime, endTime);
        }
        queryWrapper.orderByDesc(Reservation::getCreateTime);

        Page<Reservation> mpPage = new Page<>(page, size);
        Page<Reservation> resultPage = this.page(mpPage, queryWrapper);

        List<ReservationDTO> dtos = resultPage.getRecords().stream()
                .map(ReservationDTO::fromReservation)
                .collect(Collectors.toList());

        // 批量填充关联数据
        fillReservationDTOs(dtos);

        return PageResult.of(dtos, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 批量填充预约DTO中的用户名、车牌号、车位编号
     */
    private void fillReservationDTOs(List<ReservationDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        List<Long> userIds = dtos.stream().map(ReservationDTO::getUserId).distinct().collect(Collectors.toList());
        List<Long> vehicleIds = dtos.stream().map(ReservationDTO::getVehicleId).distinct().collect(Collectors.toList());
        List<Long> spaceIds = dtos.stream().map(ReservationDTO::getSpaceId).distinct().collect(Collectors.toList());

        Map<Long, String> userMap = userIds.isEmpty() ? Map.of() :
            parkUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(ParkUser::getId, ParkUser::getUsername));
        Map<Long, String> vehicleMap = vehicleIds.isEmpty() ? Map.of() :
            vehicleMapper.selectBatchIds(vehicleIds).stream()
                .collect(Collectors.toMap(Vehicle::getId, Vehicle::getPlateNumber));
        Map<Long, String> spaceMap = spaceIds.isEmpty() ? Map.of() :
            parkingSpaceMapper.selectBatchIds(spaceIds).stream()
                .collect(Collectors.toMap(ParkingSpace::getId, ParkingSpace::getSpaceNumber));

        for (ReservationDTO dto : dtos) {
            dto.setUsername(userMap.get(dto.getUserId()));
            dto.setPlateNumber(vehicleMap.get(dto.getVehicleId()));
            dto.setSpaceNumber(spaceMap.get(dto.getSpaceId()));
        }
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
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC400,
                "车位在指定时间不可用"
            );
        }
        
        Reservation reservation = reservationDTO.toReservation();
        this.save(reservation);
        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    @Transactional
    @Retryable(retryFor = ConcurrentModificationException.class, maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 100))
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
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC400,
                "车位在指定时间不可用"
            );
        }
        
        // 保存原始版本号
        Integer originalVersion = existingReservation.getVersion();
        
        Reservation reservation = reservationDTO.toReservation();
        reservation.setId(id);
        reservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean updated = this.update(reservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );
        
        if (!updated) {
            throw new ConcurrentModificationException("更新失败，可能已被其他用户修改");
        }

        return ReservationDTO.fromReservation(reservation);
    }

    @Override
    @Transactional
    public boolean deleteReservation(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean approveReservation(Long id, Long approvedBy, String rejectReason) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            return false;
        }

        // 审批前检查：该预约的时间段是否与已有已通过预约冲突
        if (!isSpaceAvailable(reservation.getSpaceId(), reservation.getStartTime(),
                reservation.getEndTime(), id)) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1300,
                "该时间段车位已被占用，无法审批通过"
            );
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
        updateReservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean approved = this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (approved) {
            logAfterCommit(approvedBy, "预约管理", "审批通过",
                    "预约ID:" + id + "，审批通过");
        }

        return approved;
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
        updateReservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean rejected = this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (rejected) {
            logAfterCommit(approvedBy, "预约管理", "审批拒绝",
                    "预约ID:" + id + "，拒绝原因:" + rejectReason);
        }

        return rejected;
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
        updateReservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean statusUpdated = this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (statusUpdated) {
            String statusText = switch (status) {
                case 0 -> "已取消";
                case 1 -> "已预约";
                case 2 -> "已使用";
                case 3 -> "已完成";
                default -> "未知";
            };
            logAfterCommit(null, "预约管理", "更新状态",
                    "预约ID:" + id + "，状态更新为:" + statusText);
        }

        return statusUpdated;
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
        updateReservation.setVersion(originalVersion + 1);
        
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
        updateReservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean arrived = this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (arrived) {
            logAfterCommit(reservation.getUserId(), "预约管理", "车辆到达",
                    "预约ID:" + id + "，到达时间:" + arriveTime);
        }

        return arrived;
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
        updateReservation.setVersion(originalVersion + 1);
        
        // 使用update方法并指定版本号条件
        boolean updated = this.update(updateReservation, 
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (!updated) {
            return false;
        }

        // 自动生成支付记录（未支付状态）
        PaymentRecordDTO paymentRecordDTO = new PaymentRecordDTO();
        paymentRecordDTO.setReservationId(id);
        paymentRecordDTO.setUserId(reservation.getUserId());
        paymentRecordDTO.setAmount(totalFee);
        paymentRecordDTO.setPaymentMethod(0); // 未指定支付方式
        paymentRecordDTO.setPaymentStatus(0); // 未支付
        paymentRecordService.createPaymentRecord(paymentRecordDTO);

        logAfterCommit(reservation.getUserId(), "预约管理", "车辆离开",
                "预约ID:" + id + "，离开时间:" + leaveTime + "，费用:" + totalFee + "元，已自动生成支付记录");

        return true;
    }

    @Override
    @Transactional
    @Retryable(retryFor = ConcurrentModificationException.class, maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 100))
    public FeeCalculationResult recordDepartureWithAutoFee(Long id, LocalDateTime leaveTime) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        Reservation reservation = reservationMapper.selectForUpdate(id);
        if (reservation == null) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1300,
                "预约不存在或已被删除"
            );
        }

        // 获取到达时间（如果没有到达时间，使用预约开始时间）
        LocalDateTime arriveTime = reservation.getArriveTime();
        if (arriveTime == null) {
            arriveTime = reservation.getStartTime();
        }

        // 获取车辆信息以确定车辆类型
        Integer vehicleType = 1; // 默认小型车
        Vehicle vehicle = vehicleMapper.selectById(reservation.getVehicleId());
        if (vehicle != null && vehicle.getVehicleType() != null) {
            vehicleType = vehicle.getVehicleType();
        }

        // 获取车位信息以确定园区ID
        Long parkAreaId = null;
        ParkingSpace parkingSpace = parkingSpaceMapper.selectById(reservation.getSpaceId());
        if (parkingSpace != null) {
            parkAreaId = parkingSpace.getParkAreaId();
        }

        // 自动计算费用
        FeeCalculationResult feeResult = feeRuleService.calculateFeeWithDetail(
                parkAreaId, vehicleType, arriveTime, leaveTime);

        // 保存原始版本号
        Integer originalVersion = reservation.getVersion();

        // 创建更新对象
        Reservation updateReservation = new Reservation();
        updateReservation.setId(id);
        updateReservation.setLeaveTime(leaveTime);
        updateReservation.setTotalFee(feeResult.getTotalFee());
        updateReservation.setSettlementTime(LocalDateTime.now());
        updateReservation.setVersion(originalVersion + 1);

        // 使用update方法并指定版本号条件
        boolean updated = this.update(updateReservation,
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getVersion, originalVersion)
        );

        if (!updated) {
            throw new ConcurrentModificationException("更新失败，可能已被其他用户修改");
        }

        // 自动生成支付记录（未支付状态）
        PaymentRecordDTO paymentRecordDTO = new PaymentRecordDTO();
        paymentRecordDTO.setReservationId(id);
        paymentRecordDTO.setUserId(reservation.getUserId());
        paymentRecordDTO.setAmount(feeResult.getTotalFee());
        paymentRecordDTO.setPaymentMethod(0); // 未指定支付方式
        paymentRecordDTO.setPaymentStatus(0); // 未支付
        paymentRecordService.createPaymentRecord(paymentRecordDTO);

        logAfterCommit(reservation.getUserId(), "预约管理", "车辆离开（自动计费）",
                "预约ID:" + id + "，到达时间:" + arriveTime + "，离开时间:" + leaveTime + 
                "，停车时长:" + feeResult.getTotalMinutes() + "分钟，费用:" + feeResult.getTotalFee() + 
                "元，已自动生成支付记录");

        return feeResult;
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
        LocalDateTime now = LocalDateTime.now();

        // 1. 检查 space_occupy 实际占用表：是否有车辆正在占用该车位（车还停在里面）
        // 判断"正在占用"的标准：endTime 被设为默认大值（9999-12-31），表示车还没开走
        // 如果车已离场，endTime 会被更新为实际离场时间（如 01:10:31），
        // 即使新预约的 startTime 比离场时间早，该记录也不应被视为"正在占用"
        // 所以用 endTime >= OCCUPY_END_DEFAULT 来判断，而不是 endTime > startTime
        LambdaQueryWrapper<SpaceOccupy> occupyQuery = new LambdaQueryWrapper<>();
        occupyQuery.eq(SpaceOccupy::getSpaceId, spaceId)
                   .ge(SpaceOccupy::getEndTime, LocalDateTime.of(9999, 12, 31, 0, 0)); // 只有endTime="还在占用中"的大值才算被占用
        
        if (excludeReservationId != null) {
            occupyQuery.ne(SpaceOccupy::getReservationId, excludeReservationId);
        }
        
        long occupyCount = spaceOccupyMapper.selectCount(occupyQuery);
        if (occupyCount > 0) {
            return false; // 车位实际被占用（车仍停在里面）
        }
        
        // 2. 检查 reservation 预约表：是否有已通过但还未入场的预约冲突
        // 只查 status=1（已预约，还没入场）的预约，因为已入场(status=2)的已在 space_occupy 中体现
        // 且新预约的 startTime/endTime 范围不能与已有未入场预约的冲突
        LambdaQueryWrapper<Reservation> reservationQuery = new LambdaQueryWrapper<>();
        reservationQuery.eq(Reservation::getSpaceId, spaceId)
                        .eq(Reservation::getApprovalStatus, 1)  // 已审批通过
                        .eq(Reservation::getStatus, 1)          // 已预约（尚未入场）
                        .and(wrapper -> wrapper
                            .le(Reservation::getStartTime, endTime)
                            .ge(Reservation::getEndTime, startTime));
        
        if (excludeReservationId != null) {
            reservationQuery.ne(Reservation::getId, excludeReservationId);
        }
        
        long reservationCount = this.count(reservationQuery);
        return reservationCount == 0;
    }

    @Override
    public List<Long> expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 查询所有过期的预约ID（用于日志记录）
        List<Long> expiredIds = reservationMapper.selectOverdueReservationIds(now);

        if (expiredIds.isEmpty()) {
            log.debug("没有需要过期的预约");
            return List.of();
        }

        // 2. 批量更新：将 status 设为 3（已过期）
        int updatedRows = reservationMapper.batchExpireOverdueReservations(now);
        log.info("预约自动过期处理完成：共处理 " + updatedRows + " 条过期预约，IDs=" + expiredIds);

        // 3. 批量记录操作日志（系统操作，userId 设为 0）
        for (Long id : expiredIds) {
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setUserId(0L);
            logDTO.setModule("预约管理");
            logDTO.setAction("自动过期");
            logDTO.setDetail("预约ID:" + id + "，预约结束时间已过，系统自动标记为已过期");
            operationLogService.createOperationLog(logDTO);
        }

        return expiredIds;
    }

    /**
     * 在事务提交后异步记录操作日志，避免延长数据库行锁持有时间
     */
    private void logAfterCommit(Long userId, String module, String action, String detail) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                OperationLogDTO logDTO = new OperationLogDTO();
                logDTO.setUserId(userId);
                logDTO.setModule(module);
                logDTO.setAction(action);
                logDTO.setDetail(detail);
                operationLogService.createOperationLog(logDTO);
            }
        });
    }
}
