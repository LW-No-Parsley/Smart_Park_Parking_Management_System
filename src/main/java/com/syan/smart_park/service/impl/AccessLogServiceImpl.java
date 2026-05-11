package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.dao.*;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.service.AccessLogService;
import com.syan.smart_park.service.FeeRuleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 进出记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl extends ServiceImpl<AccessLogMapper, AccessLog> implements AccessLogService {

    private static final Logger log = LoggerFactory.getLogger(AccessLogServiceImpl.class);

    private final AccessLogMapper accessLogMapper;
    private final GateDeviceMapper gateDeviceMapper;
    private final VehicleMapper vehicleMapper;
    private final BlacklistMapper blacklistMapper;
    private final ReservationMapper reservationMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final SpaceOccupyMapper spaceOccupyMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final FeeRuleService feeRuleService;

    // ======================== 闸机入场逻辑 ========================

    @Override
    @Transactional
    public GateAccessResult handleEntry(GateAccessDTO dto) {
        GateAccessResult result = createBaseResult(dto);

        // 1. 校验基础参数
        if (dto.getParkAreaId() == null || dto.getDeviceSn() == null) {
            result.setMessage("园区ID和设备编号不能为空");
            return result;
        }
        if (dto.getPlateNumber() == null || dto.getPlateNumber().isEmpty()) {
            result.setMessage("未识别到车牌号");
            return result;
        }

        // 2. 通过设备编号查找道闸设备
        GateDevice gate = findGateByDeviceSn(dto.getParkAreaId(), dto.getDeviceSn());
        if (gate == null) {
            result.setMessage("未找到该园区下的道闸设备，设备编号：" + dto.getDeviceSn());
            return result;
        }
        dto.setGateId(gate.getId());

        // 3. 检查黑名单
        boolean isBlacklisted = checkBlacklist(dto.getPlateNumber(), dto.getParkAreaId());
        if (isBlacklisted) {
            result.setAllowed(false);
            result.setRecognitionResult(2);
            result.setMessage("该车辆在黑名单中，禁止入场");
            AccessLog logEntry = buildAccessLog(dto, null, null, null, 2);
            this.save(logEntry);
            result.setAccessLogId(logEntry.getId());
            return result;
        }

        // 4. 通过车牌号查车辆信息（是否是业主默认车辆——有userId的）
        Vehicle vehicle = findDefaultVehicle(dto.getPlateNumber());
        if (vehicle != null) {
            result.setAllowed(true);
            result.setVehicleId(vehicle.getId());
            result.setVehicleType(vehicle.getVehicleType());
            result.setUserId(vehicle.getUserId());
            result.setIsDefaultVehicle(true);
            result.setRecognitionResult(1);
            result.setMessage("欢迎业主入场");
            AccessLog logEntry = buildAccessLog(dto, vehicle.getId(), vehicle.getUserId(), null, 1);
            this.save(logEntry);
            result.setAccessLogId(logEntry.getId());
            return result;
        }

        // 5. 不是默认车辆，查当天有效预约（通过车牌号对应车辆ID查）
        Vehicle anyVehicle = findVehicleByPlate(dto.getPlateNumber());
        Reservation reservation = findValidReservation(
                anyVehicle != null ? anyVehicle.getId() : null,
                dto.getParkAreaId());
        if (reservation != null) {
            result.setAllowed(true);
            result.setVehicleId(anyVehicle != null ? anyVehicle.getId() : null);
            result.setVehicleType(anyVehicle != null ? anyVehicle.getVehicleType() : null);
            result.setUserId(reservation.getUserId());
            result.setIsDefaultVehicle(false);
            result.setHasReservation(true);
            result.setRecognitionResult(1);

            // 更新预约状态为"已使用"，记录到达时间
            reservation.setStatus(2); // 2-已使用
            reservation.setArriveTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);

            // 生成车位占用记录
            createSpaceOccupy(reservation.getSpaceId(), reservation.getId(),
                    anyVehicle != null ? anyVehicle.getId() : null);

            result.setMessage("欢迎入场，已为您分配预约车位");
            AccessLog logEntry = buildAccessLog(dto,
                    anyVehicle != null ? anyVehicle.getId() : null,
                    reservation.getUserId(), reservation.getId(), 1);
            this.save(logEntry);
            result.setAccessLogId(logEntry.getId());
            return result;
        }

        // 6. 临时车处理（没有预约的普通车辆）
        result.setAllowed(true);
        result.setVehicleId(anyVehicle != null ? anyVehicle.getId() : null);
        result.setVehicleType(anyVehicle != null ? anyVehicle.getVehicleType() : null);
        result.setIsDefaultVehicle(false);
        result.setHasReservation(false);
        result.setRecognitionResult(1);
        result.setMessage("欢迎临时车辆入场");
        AccessLog logEntry = buildAccessLog(dto,
                anyVehicle != null ? anyVehicle.getId() : null, null, null, 1);
        this.save(logEntry);
        result.setAccessLogId(logEntry.getId());
        return result;
    }

    // ======================== 闸机出场逻辑 ========================

    @Override
    @Transactional
    public GateAccessResult handleExit(GateAccessDTO dto) {
        GateAccessResult result = createBaseResult(dto);

        // 1. 校验基础参数
        if (dto.getParkAreaId() == null || dto.getDeviceSn() == null) {
            result.setMessage("园区ID和设备编号不能为空");
            return result;
        }
        if (dto.getPlateNumber() == null || dto.getPlateNumber().isEmpty()) {
            result.setMessage("未识别到车牌号");
            return result;
        }

        // 2. 通过设备编号查找道闸设备
        GateDevice gate = findGateByDeviceSn(dto.getParkAreaId(), dto.getDeviceSn());
        if (gate == null) {
            result.setMessage("未找到该园区下的道闸设备，设备编号：" + dto.getDeviceSn());
            return result;
        }
        dto.setGateId(gate.getId());

        // 3. 查入场记录（同一辆车在该园区最近一次入场且未出场）
        AccessLog entryLog = findLastEntryLog(dto.getPlateNumber(), dto.getParkAreaId());
        if (entryLog == null) {
            // 没有入场记录，但可以放行（异常出场）
            result.setAllowed(true);
            result.setRecognitionResult(1);
            result.setMessage("未找到入场记录，已放行");
            AccessLog exitLog = buildAccessLog(dto, null, null, null, 1);
            this.save(exitLog);
            result.setAccessLogId(exitLog.getId());
            log.warn("车辆 {} 在园区 {} 无入场记录直接出场", dto.getPlateNumber(), dto.getParkAreaId());
            return result;
        }

        // 记录出场日志（携带入场记录的userId以便后续生成支付记录）
        AccessLog exitLog = buildAccessLog(dto, entryLog.getVehicleId(), entryLog.getUserId(), entryLog.getReservationId(), 1);
        this.save(exitLog);
        result.setAccessLogId(exitLog.getId());
        result.setVehicleId(entryLog.getVehicleId());

        // 查找入场车辆的类型（通过 vehicleId 取 vehicleType）
        Integer vehicleType = null;
        if (entryLog.getVehicleId() != null) {
            Vehicle entryVehicle = vehicleMapper.selectById(entryLog.getVehicleId());
            if (entryVehicle != null) {
                vehicleType = entryVehicle.getVehicleType();
            }
        }
        result.setUserId(entryLog.getUserId());

        // 4. 计算停车费用
        LocalDateTime entryTime = entryLog.getAccessTime();
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(entryTime, now);
        if (minutes < 1) minutes = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        try {
            totalAmount = feeRuleService.calculateFee(
                    dto.getParkAreaId(),
                    vehicleType != null ? vehicleType : 1,
                    entryTime,
                    now);
        } catch (Exception e) {
            log.error("计算停车费用失败", e);
        }
        result.setRecognitionResult(1);

        // 5. 处理预约相关（如果有预约则释放占位、更新预约记录、生成支付记录）
        Long reservationId = entryLog.getReservationId();
        if (reservationId != null) {
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation != null) {
                // 更新预约：离开时间、结算费用、支付状态
                reservation.setLeaveTime(now);
                reservation.setSettlementTime(now);
                reservation.setTotalFee(totalAmount);
                if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    reservation.setPayStatus(1); // 免费，视为已支付
                    reservation.setPaidAmount(BigDecimal.ZERO);
                } else {
                    reservation.setPayStatus(0); // 未支付
                }
                reservation.setStatus(3); // 3-已过期/已完成
                reservationMapper.updateById(reservation);

                // 释放预约车位占用
                releaseSpaceOccupy(reservation.getSpaceId(), reservationId);

                // 生成支付记录（有费用才生成）
                if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    createPaymentRecord(reservation.getUserId(), reservationId,
                            totalAmount, dto.getPlateNumber());
                }
            }
        } else {
            // 非预约车辆出场（临时车）：直接生成支付记录
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                createPaymentRecord(entryLog.getUserId(), null, totalAmount, dto.getPlateNumber());
            }
        }

        // 6. 组装返回结果
        result.setAllowed(true);
        result.setMessage("出场成功");
        FeeCalculationResult feeInfo = new FeeCalculationResult();
        feeInfo.setStartTime(entryTime);
        feeInfo.setEndTime(now);
        feeInfo.setTotalMinutes(minutes);
        feeInfo.setTotalFee(totalAmount);
        if (vehicleType != null) {
            feeInfo.setVehicleType(vehicleType);
        }
        result.setFeeInfo(feeInfo);

        return result;
    }

    // ======================== 原有旧方法兼容保留 ========================

    @Override
    public PageResult<AccessLogDTO> pageAccessLogs(long current, long size,
                                                    Long parkAreaId, Long gateId,
                                                    String plateNumber, Long vehicleId,
                                                    Integer accessType, Integer recognitionResult,
                                                    Long handledBy,
                                                    LocalDateTime startTime, LocalDateTime endTime,
                                                    Boolean exceptionOnly) {
        Page<AccessLog> page = new Page<>(current, size);
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();

        if (parkAreaId != null) wrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        if (gateId != null) wrapper.eq(AccessLog::getGateId, gateId);
        if (plateNumber != null && !plateNumber.isEmpty()) wrapper.like(AccessLog::getPlateNumber, plateNumber);
        if (vehicleId != null) wrapper.eq(AccessLog::getVehicleId, vehicleId);
        if (accessType != null) wrapper.eq(AccessLog::getAccessType, accessType);
        if (recognitionResult != null) wrapper.eq(AccessLog::getRecognitionResult, recognitionResult);
        if (handledBy != null) wrapper.eq(AccessLog::getHandledBy, handledBy);
        if (startTime != null) wrapper.ge(AccessLog::getAccessTime, startTime);
        if (endTime != null) wrapper.le(AccessLog::getAccessTime, endTime);
        if (exceptionOnly != null && exceptionOnly) {
            wrapper.in(AccessLog::getRecognitionResult, 0, 2);
        }

        wrapper.orderByDesc(AccessLog::getAccessTime);
        IPage<AccessLog> result = accessLogMapper.selectPage(page, wrapper);

        List<AccessLogDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public AccessLogDTO getAccessLogById(Long id) {
        AccessLog log = accessLogMapper.selectById(id);
        return log != null ? convertToDTO(log) : null;
    }

    @Override
    public AccessLogDTO createAccessLog(AccessLogDTO dto) {
        AccessLog log = convertToEntity(dto);
        accessLogMapper.insert(log);
        dto.setId(log.getId());
        return dto;
    }

    @Override
    public AccessLogDTO updateAccessLog(Long id, AccessLogDTO dto) {
        AccessLog log = convertToEntity(dto);
        log.setId(id);
        accessLogMapper.updateById(log);
        return dto;
    }

    @Override
    public AccessLogStatistics getTodayAccessLogStatistics(Long parkAreaId) {
        AccessLogStatistics stats = new AccessLogStatistics();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        LambdaQueryWrapper<AccessLog> entryWrapper = new LambdaQueryWrapper<>();
        entryWrapper.eq(AccessLog::getAccessType, 1);
        entryWrapper.ge(AccessLog::getAccessTime, todayStart);
        entryWrapper.le(AccessLog::getAccessTime, todayEnd);
        if (parkAreaId != null) entryWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        stats.setTotalEntries(accessLogMapper.selectCount(entryWrapper));

        LambdaQueryWrapper<AccessLog> exitWrapper = new LambdaQueryWrapper<>();
        exitWrapper.eq(AccessLog::getAccessType, 2);
        exitWrapper.ge(AccessLog::getAccessTime, todayStart);
        exitWrapper.le(AccessLog::getAccessTime, todayEnd);
        if (parkAreaId != null) exitWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        stats.setTotalExits(accessLogMapper.selectCount(exitWrapper));

        LambdaQueryWrapper<AccessLog> excWrapper = new LambdaQueryWrapper<>();
        excWrapper.in(AccessLog::getRecognitionResult, 0, 2);
        excWrapper.ge(AccessLog::getAccessTime, todayStart);
        excWrapper.le(AccessLog::getAccessTime, todayEnd);
        if (parkAreaId != null) excWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        stats.setExceptionCount(accessLogMapper.selectCount(excWrapper));

        return stats;
    }

    @Override
    public List<AccessLogTrend> getAccessLogTrend(LocalDateTime startTime, LocalDateTime endTime, Long parkAreaId) {
        // 简单的按天分组统计
        List<AccessLogTrend> trends = new ArrayList<>();
        LocalDateTime dayStart = startTime.withHour(0).withMinute(0).withSecond(0);
        while (dayStart.isBefore(endTime)) {
            LocalDateTime dayEnd = dayStart.plusDays(1);

            LambdaQueryWrapper<AccessLog> entryWrapper = new LambdaQueryWrapper<>();
            entryWrapper.eq(AccessLog::getAccessType, 1);
            entryWrapper.ge(AccessLog::getAccessTime, dayStart);
            entryWrapper.lt(AccessLog::getAccessTime, dayEnd);
            if (parkAreaId != null) entryWrapper.eq(AccessLog::getParkAreaId, parkAreaId);

            LambdaQueryWrapper<AccessLog> exitWrapper = new LambdaQueryWrapper<>();
            exitWrapper.eq(AccessLog::getAccessType, 2);
            exitWrapper.ge(AccessLog::getAccessTime, dayStart);
            exitWrapper.lt(AccessLog::getAccessTime, dayEnd);
            if (parkAreaId != null) exitWrapper.eq(AccessLog::getParkAreaId, parkAreaId);

            AccessLogTrend trend = new AccessLogTrend();
            trend.setDate(dayStart);
            trend.setEntryCount(accessLogMapper.selectCount(entryWrapper));
            trend.setExitCount(accessLogMapper.selectCount(exitWrapper));
            trends.add(trend);
            dayStart = dayEnd;
        }
        return trends;
    }

    @Override
    public boolean batchCreateAccessLogs(List<AccessLogDTO> accessLogDTOs) {
        List<AccessLog> logs = accessLogDTOs.stream().map(this::convertToEntity).collect(Collectors.toList());
        return this.saveBatch(logs);
    }

    @Override
    public boolean batchUpdateAccessLogs(List<AccessLogDTO> accessLogDTOs) {
        List<AccessLog> logs = accessLogDTOs.stream().map(dto -> {
            AccessLog log = convertToEntity(dto);
            log.setId(dto.getId());
            return log;
        }).collect(Collectors.toList());
        return this.updateBatchById(logs);
    }

    @Override
    public List<AccessLogDTO> getAccessLogsByPlateNumberAndTimeRange(String plateNumber, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessLog::getPlateNumber, plateNumber);
        wrapper.ge(AccessLog::getAccessTime, startTime);
        wrapper.le(AccessLog::getAccessTime, endTime);
        wrapper.orderByDesc(AccessLog::getAccessTime);
        return accessLogMapper.selectList(wrapper).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getRecentAccessLogs(Integer limit) {
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AccessLog::getAccessTime);
        wrapper.last("LIMIT " + limit);
        return accessLogMapper.selectList(wrapper).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<AccessLogDTO> getExceptionAccessLogs() {
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AccessLog::getRecognitionResult, 0, 2);
        wrapper.orderByDesc(AccessLog::getAccessTime);
        return accessLogMapper.selectList(wrapper).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public boolean updateRecognitionResult(Long id, Integer recognitionResult, String remark) {
        AccessLog log = accessLogMapper.selectById(id);
        if (log == null) return false;
        log.setRecognitionResult(recognitionResult);
        if (remark != null) log.setRemark(remark);
        return accessLogMapper.updateById(log) > 0;
    }

    @Override
    public boolean handleAccessLogManually(Long id, Long handledBy, String remark) {
        AccessLog log = accessLogMapper.selectById(id);
        if (log == null) return false;
        log.setHandledBy(handledBy);
        if (remark != null) log.setRemark(remark);
        return accessLogMapper.updateById(log) > 0;
    }

    // ======================== 私有辅助方法 ========================

    /**
     * 创建基础返回结果
     */
    private GateAccessResult createBaseResult(GateAccessDTO dto) {
        GateAccessResult result = new GateAccessResult();
        result.setAllowed(false);
        result.setIsDefaultVehicle(false);
        result.setHasReservation(false);
        if (dto.getAccessTime() == null) {
            dto.setAccessTime(LocalDateTime.now());
        }
        return result;
    }

    /**
     * 根据设备编号查找道闸
     */
    private GateDevice findGateByDeviceSn(Long parkAreaId, String deviceSn) {
        LambdaQueryWrapper<GateDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GateDevice::getParkAreaId, parkAreaId);
        wrapper.eq(GateDevice::getDeviceSn, deviceSn);
        return gateDeviceMapper.selectOne(wrapper);
    }

    /**
     * 检查车牌是否在黑名单中
     */
    private boolean checkBlacklist(String plateNumber, Long parkAreaId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getPlateNumber, plateNumber);
        wrapper.eq(Blacklist::getParkAreaId, parkAreaId);
        wrapper.eq(Blacklist::getStatus, 1);
        return blacklistMapper.selectCount(wrapper) > 0;
    }

    /**
     * 查找该车牌的默认车辆（有userId的业主车辆）
     */
    private Vehicle findDefaultVehicle(String plateNumber) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getPlateNumber, plateNumber);
        List<Vehicle> vehicles = vehicleMapper.selectList(wrapper);
        if (vehicles.isEmpty()) {
            return null;
        }
        return vehicles.stream()
                .filter(v -> v.getUserId() != null)
                .findFirst()
                .orElse(null);
    }

    /**
     * 仅通过车牌号查找车辆
     */
    private Vehicle findVehicleByPlate(String plateNumber) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getPlateNumber, plateNumber);
        List<Vehicle> list = vehicleMapper.selectList(wrapper);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查找当天有效的预约记录
     * 通过 vehicleId 关联车牌，再通过该预约绑定车位的园区ID判断是否属于该园区
     */
    private Reservation findValidReservation(Long vehicleId, Long parkAreaId) {
        if (vehicleId == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getVehicleId, vehicleId);
        wrapper.ge(Reservation::getStartTime, todayStart);
        wrapper.in(Reservation::getStatus, 1, 2);
        wrapper.orderByAsc(Reservation::getEndTime);
        wrapper.last("LIMIT 50"); // 取最多50条，然后内存过滤

        List<Reservation> list = reservationMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return null;
        }

        // 过滤：预约绑定车位所属的园区ID需匹配
        for (Reservation reservation : list) {
            if (reservation.getSpaceId() != null) {
                ParkingSpace space = parkingSpaceMapper.selectById(reservation.getSpaceId());
                if (space != null && space.getParkAreaId() != null
                        && space.getParkAreaId().equals(parkAreaId)) {
                    // 检查是否仍在有效期内（允许超时15分钟）
                    LocalDateTime deadline = reservation.getEndTime().plusMinutes(15);
                    if (!now.isAfter(deadline)) {
                        return reservation;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查找最近一次入场且未出场的记录
     */
    private AccessLog findLastEntryLog(String plateNumber, Long parkAreaId) {
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessLog::getPlateNumber, plateNumber);
        wrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        wrapper.eq(AccessLog::getAccessType, 1);
        wrapper.orderByDesc(AccessLog::getAccessTime);
        wrapper.last("LIMIT 1");
        List<AccessLog> list = accessLogMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return null;
        }
        AccessLog entryLog = list.get(0);

        // 检查是否有之后的出场记录
        LambdaQueryWrapper<AccessLog> exitWrapper = new LambdaQueryWrapper<>();
        exitWrapper.eq(AccessLog::getPlateNumber, plateNumber);
        exitWrapper.eq(AccessLog::getParkAreaId, parkAreaId);
        exitWrapper.eq(AccessLog::getAccessType, 2);
        exitWrapper.gt(AccessLog::getAccessTime, entryLog.getAccessTime());
        exitWrapper.last("LIMIT 1");
        List<AccessLog> exitList = accessLogMapper.selectList(exitWrapper);
        if (!exitList.isEmpty()) {
            return null;
        }
        return entryLog;
    }

    /**
     * 构建通行记录实体
     * @param dto 闸机请求
     * @param vehicleId 关联车辆ID（可为null）
     * @param userId 关联业主用户ID（可为null）
     * @param reservationId 关联预约ID（可为null）
     * @param recognitionResult 识别结果
     */
    private AccessLog buildAccessLog(GateAccessDTO dto, Long vehicleId, Long userId, Long reservationId, Integer recognitionResult) {
        AccessLog log = new AccessLog();
        log.setParkAreaId(dto.getParkAreaId());
        log.setGateId(dto.getGateId());
        log.setPlateNumber(dto.getPlateNumber());
        log.setVehicleId(vehicleId);
        log.setUserId(userId);
        log.setReservationId(reservationId);
        log.setAccessType(dto.getAccessType());
        log.setImageUrl(dto.getImageUrl());
        log.setRecognitionResult(recognitionResult);
        log.setAccessTime(dto.getAccessTime() != null ? dto.getAccessTime() : LocalDateTime.now());
        log.setHandledBy(dto.getHandledBy());
        log.setRemark(dto.getRemark());
        return log;
    }

    /**
     * 创建车位占用记录
     */
    private void createSpaceOccupy(Long spaceId, Long reservationId, Long vehicleId) {
        try {
            SpaceOccupy occupy = new SpaceOccupy();
            occupy.setSpaceId(spaceId);
            occupy.setReservationId(reservationId);
            occupy.setVehicleId(vehicleId);
            occupy.setStartTime(LocalDateTime.now());
            spaceOccupyMapper.insert(occupy);

            // 更新车位状态（使用正确的字段名 space_occupy 没有 status 字段）
            // ParkingSpace 的 status: 0-禁用, 1-正常, 4-故障
            // 没有"占用"状态，所以只记录占用记录不修改车位状态
        } catch (Exception e) {
            log.error("创建车位占用记录失败，spaceId={}", spaceId, e);
        }
    }

    /**
     * 释放车位占用（出场时调用）
     */
    private void releaseSpaceOccupy(Long spaceId, Long reservationId) {
        try {
            LambdaQueryWrapper<SpaceOccupy> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpaceOccupy::getSpaceId, spaceId);
            wrapper.eq(SpaceOccupy::getReservationId, reservationId);
            wrapper.isNull(SpaceOccupy::getEndTime);
            List<SpaceOccupy> occupies = spaceOccupyMapper.selectList(wrapper);
            for (SpaceOccupy occupy : occupies) {
                occupy.setEndTime(LocalDateTime.now());
                spaceOccupyMapper.updateById(occupy);
            }
        } catch (Exception e) {
            log.error("释放车位占用失败，spaceId={}", spaceId, e);
        }
    }

    /**
     * 生成支付记录
     */
    private void createPaymentRecord(Long userId, Long reservationId, BigDecimal amount, String plateNumber) {
        try {
            PaymentRecord record = new PaymentRecord();
            record.setUserId(userId);
            record.setReservationId(reservationId);
            record.setAmount(amount);
            record.setPaymentStatus(0); // 0-未支付
            paymentRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("生成支付记录失败，plateNumber={}", plateNumber, e);
        }
    }

    /**
     * 将 AccessLog 实体转换为 DTO
     */
    private AccessLogDTO convertToDTO(AccessLog log) {
        if (log == null) return null;
        AccessLogDTO dto = new AccessLogDTO();
        dto.setId(log.getId());
        dto.setParkAreaId(log.getParkAreaId());
        dto.setGateId(log.getGateId());
        dto.setPlateNumber(log.getPlateNumber());
        dto.setVehicleId(log.getVehicleId());
        dto.setAccessType(log.getAccessType());
        dto.setImageUrl(log.getImageUrl());
        dto.setRecognitionResult(log.getRecognitionResult());
        dto.setAccessTime(log.getAccessTime());
        dto.setHandledBy(log.getHandledBy());
        dto.setRemark(log.getRemark());
        dto.setCreateTime(log.getCreateTime());
        dto.setUpdateTime(log.getUpdateTime());
        return dto;
    }

    /**
     * 将 AccessLogDTO 转换为实体
     */
    private AccessLog convertToEntity(AccessLogDTO dto) {
        if (dto == null) return null;
        AccessLog log = new AccessLog();
        log.setId(dto.getId());
        log.setParkAreaId(dto.getParkAreaId());
        log.setGateId(dto.getGateId());
        log.setPlateNumber(dto.getPlateNumber());
        log.setVehicleId(dto.getVehicleId());
        log.setAccessType(dto.getAccessType());
        log.setImageUrl(dto.getImageUrl());
        log.setRecognitionResult(dto.getRecognitionResult());
        log.setAccessTime(dto.getAccessTime());
        log.setHandledBy(dto.getHandledBy());
        log.setRemark(dto.getRemark());
        return log;
    }
}
