package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.dao.ParkUserMapper;
import com.syan.smart_park.dao.VehicleMapper;
import com.syan.smart_park.dao.ParkingSpaceMapper;
import com.syan.smart_park.dao.SpaceOccupyMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.Vehicle;
import com.syan.smart_park.entity.VehicleDTO;
import com.syan.smart_park.entity.ParkingSpace;
import com.syan.smart_park.entity.SpaceOccupy;
import com.syan.smart_park.service.OperationLogService;
import com.syan.smart_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 车辆服务实现类
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private final VehicleMapper vehicleMapper;
    private final ParkUserMapper parkUserMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final SpaceOccupyMapper spaceOccupyMapper;
    private final OperationLogService operationLogService;

    /**
     * 填充车辆DTO中的用户名
     */
    private void fillUsername(VehicleDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            return;
        }
        ParkUser parkUser = parkUserMapper.selectById(dto.getUserId());
        if (parkUser != null) {
            dto.setUsername(parkUser.getUsername());
        }
    }

    /**
     * 批量填充车辆DTO列表中的用户名
     */
    private void fillUsernames(List<VehicleDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        // 收集所有用户ID
        List<Long> userIds = dtos.stream()
                .map(VehicleDTO::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        Map<Long, String> userMap = parkUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(ParkUser::getId, ParkUser::getUsername));

        // 填充用户名
        for (VehicleDTO dto : dtos) {
            dto.setUsername(userMap.get(dto.getUserId()));
        }
    }

    /**
     * 填充单个车辆的绑定车位ID
     * 只查询 reservationId=0 的长期绑定记录，排除预约产生的临时占用
     */
    private void fillSpaceId(VehicleDTO dto) {
        if (dto == null || dto.getId() == null) {
            return;
        }
        LambdaQueryWrapper<SpaceOccupy> query = new LambdaQueryWrapper<>();
        query.eq(SpaceOccupy::getVehicleId, dto.getId())
             .eq(SpaceOccupy::getReservationId, 0L)
             .last("LIMIT 1");
        SpaceOccupy occupy = spaceOccupyMapper.selectOne(query);
        if (occupy != null) {
            dto.setSpaceId(occupy.getSpaceId());
        }
    }

    /**
     * 批量填充车辆DTO列表中的绑定车位ID
     * 只查询 reservationId=0 的长期绑定记录，排除预约产生的临时占用
     */
    private void fillSpaceIds(List<VehicleDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        List<Long> vehicleIds = dtos.stream()
                .map(VehicleDTO::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (vehicleIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<SpaceOccupy> query = new LambdaQueryWrapper<>();
        query.in(SpaceOccupy::getVehicleId, vehicleIds)
             .eq(SpaceOccupy::getReservationId, 0L);
        List<SpaceOccupy> occupys = spaceOccupyMapper.selectList(query);

        Map<Long, Long> spaceIdMap = occupys.stream()
                .filter(o -> o.getVehicleId() != null)
                .collect(Collectors.toMap(SpaceOccupy::getVehicleId, SpaceOccupy::getSpaceId, (a, b) -> a));

        for (VehicleDTO dto : dtos) {
            Long spaceId = spaceIdMap.get(dto.getId());
            if (spaceId != null) {
                dto.setSpaceId(spaceId);
            }
        }
    }

    @Override
    public PageResult<VehicleDTO> listVehicles(Long userId, String plateNumber, Integer status,
                                               Integer vehicleType, Integer isDefault, Integer page, Integer size) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Vehicle::getUserId, userId);
        }
        if (plateNumber != null && !plateNumber.trim().isEmpty()) {
            queryWrapper.like(Vehicle::getPlateNumber, plateNumber.trim());
        }
        if (status != null) {
            queryWrapper.eq(Vehicle::getStatus, status);
        }
        if (vehicleType != null) {
            queryWrapper.eq(Vehicle::getVehicleType, vehicleType);
        }
        if (isDefault != null) {
            queryWrapper.eq(Vehicle::getIsDefault, isDefault);
        }
        queryWrapper.orderByDesc(Vehicle::getCreateTime);

        // 使用 MyBatis-Plus 分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Vehicle> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Vehicle> resultPage =
                this.page(mpPage, queryWrapper);

        List<VehicleDTO> dtos = resultPage.getRecords().stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
        fillUsernames(dtos);
        fillSpaceIds(dtos);

        return PageResult.of(dtos, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = this.getById(id);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);
        fillSpaceId(dto);
        return dto;
    }

    @Override
    @Transactional
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        // 检查是否已存在相同车牌号的记录
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getPlateNumber, vehicleDTO.getPlateNumber());
        Long count = this.count(queryWrapper);
        
        if (count > 0) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1301, 
                "该车牌号已存在"
            );
        }
        
        Vehicle vehicle = vehicleDTO.toVehicle();
        this.save(vehicle);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);
        
        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("车辆管理");
        logDTO.setAction("创建车辆");
        logDTO.setDetail("车辆ID:" + vehicle.getId() + "，车牌号:" + vehicle.getPlateNumber() + "，用户ID:" + vehicle.getUserId());
        operationLogService.createOperationLog(logDTO);

        // 车位绑定：只有前端明确传了spaceId才绑定，不自动绑定任何车位
        ParkingSpace targetSpace = null;

        if (vehicleDTO.getSpaceId() != null) {
            // 手动指定车位
            targetSpace = parkingSpaceMapper.selectById(vehicleDTO.getSpaceId());
            if (targetSpace == null) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC1404, "指定的车位不存在");
            }
            // 检查该车位是否已被其他车辆长期绑定
            LambdaQueryWrapper<SpaceOccupy> boundCheck = new LambdaQueryWrapper<>();
            boundCheck.eq(SpaceOccupy::getSpaceId, targetSpace.getId())
                      .eq(SpaceOccupy::getReservationId, 0L);
            Long boundCount = spaceOccupyMapper.selectCount(boundCheck);
            if (boundCount > 0) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC400, "该车位已被其他车辆绑定");
            }
        }

        if (targetSpace != null) {
            SpaceOccupy occupy = new SpaceOccupy();
            occupy.setSpaceId(targetSpace.getId());
            occupy.setVehicleId(vehicle.getId());
            occupy.setReservationId(0L);
            occupy.setStartTime(LocalDateTime.now());
            occupy.setEndTime(LocalDateTime.of(2999, 12, 31, 23, 59, 59));
            spaceOccupyMapper.insert(occupy);

            OperationLogDTO occupyLog = new OperationLogDTO();
            occupyLog.setModule("车辆管理");
            occupyLog.setAction("业主车位绑定");
            occupyLog.setDetail("车辆ID:" + vehicle.getId()
                    + "，用户ID:" + vehicleDTO.getUserId()
                + "，车位:" + targetSpace.getSpaceNumber()
                    + "，占用至2999年");
            operationLogService.createOperationLog(occupyLog);
            dto.setSpaceId(targetSpace.getId());
        }

        return dto;
    }

    @Override
    @Transactional
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO) {
        Vehicle existingVehicle = this.getById(id);
        if (existingVehicle == null) {
            return null;
        }

        // 如果车牌号有变化，需要检查是否会导致重复
        if (!existingVehicle.getPlateNumber().equals(vehicleDTO.getPlateNumber())) {
            LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Vehicle::getPlateNumber, vehicleDTO.getPlateNumber())
                       .ne(Vehicle::getId, id);
            Long count = this.count(queryWrapper);

            if (count > 0) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC1301,
                    "该车牌号已存在"
                );
            }
        }

        // 允许一个用户有多辆默认车辆，所以不需要取消其他默认车辆

        // 处理 spaceId：更新车位绑定
        Long oldSpaceId = null;
        // 查找当前已有长期占用的 spaceId（通过 vehicleId 精确匹配，reservationId=0 表示长期绑定）
        LambdaQueryWrapper<SpaceOccupy> occupyQuery = new LambdaQueryWrapper<>();
        occupyQuery.eq(SpaceOccupy::getVehicleId, id)
                   .eq(SpaceOccupy::getReservationId, 0L);
        SpaceOccupy existingOccupy = spaceOccupyMapper.selectOne(occupyQuery);

        if (vehicleDTO.getSpaceId() != null) {
            // 用户指定了新车位
            if (existingOccupy != null && existingOccupy.getSpaceId().equals(vehicleDTO.getSpaceId())) {
                // 同一个车位，不做变化
            } else {
                // 检查新车位是否已被其他车辆长期绑定
                LambdaQueryWrapper<SpaceOccupy> boundCheck = new LambdaQueryWrapper<>();
                boundCheck.eq(SpaceOccupy::getSpaceId, vehicleDTO.getSpaceId())
                          .eq(SpaceOccupy::getReservationId, 0L)
                          .ne(SpaceOccupy::getVehicleId, id);
                Long boundCount = spaceOccupyMapper.selectCount(boundCheck);
                if (boundCount > 0) {
                    throw new com.syan.smart_park.common.exception.BusinessException(
                        com.syan.smart_park.common.exception.ReturnCode.RC400, "该车位已被其他车辆绑定");
                }
                // 逻辑删除旧占用
                if (existingOccupy != null) {
                    spaceOccupyMapper.deleteById(existingOccupy.getId());
                }
                // 创建新占用
                ParkingSpace newSpace = parkingSpaceMapper.selectById(vehicleDTO.getSpaceId());
                if (newSpace == null) {
                    throw new com.syan.smart_park.common.exception.BusinessException(
                        com.syan.smart_park.common.exception.ReturnCode.RC1404, "指定的车位不存在");
                }
                SpaceOccupy newOccupy = new SpaceOccupy();
                newOccupy.setSpaceId(newSpace.getId());
                newOccupy.setVehicleId(id);
                newOccupy.setReservationId(0L);
                newOccupy.setStartTime(LocalDateTime.now());
                newOccupy.setEndTime(LocalDateTime.of(2999, 12, 31, 23, 59, 59));
                spaceOccupyMapper.insert(newOccupy);

                OperationLogDTO occupyLog = new OperationLogDTO();
                occupyLog.setModule("车辆管理");
                occupyLog.setAction("业主车位变更");
                occupyLog.setDetail("车辆ID:" + id + "，新车位:" + newSpace.getSpaceNumber());
                operationLogService.createOperationLog(occupyLog);
            }
        } else {
            // 没传 spaceId → 解除原有绑定（逻辑删除）
            if (existingOccupy != null) {
                spaceOccupyMapper.deleteById(existingOccupy.getId());

                OperationLogDTO occupyLog = new OperationLogDTO();
                occupyLog.setModule("车辆管理");
                occupyLog.setAction("业主车位解绑");
                occupyLog.setDetail("车辆ID:" + id + "，车位占用已释放");
                operationLogService.createOperationLog(occupyLog);
            }
        }

        Vehicle vehicle = vehicleDTO.toVehicle();
        vehicle.setId(id);
        this.updateById(vehicle);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);

        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("车辆管理");
        logDTO.setAction("更新车辆");
        logDTO.setDetail("车辆ID:" + id + "，车牌号:" + vehicle.getPlateNumber());
        operationLogService.createOperationLog(logDTO);

        fillSpaceId(dto);
        return dto;
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long id) {
        Vehicle existingVehicle = this.getById(id);
        if (existingVehicle == null) {
            return false;
        }

        // 逻辑删除关联的长期占用记录（只删除 reservationId=0 的长期绑定，不删除预约产生的临时占用）
        LambdaQueryWrapper<SpaceOccupy> occupyQuery = new LambdaQueryWrapper<>();
        occupyQuery.eq(SpaceOccupy::getVehicleId, id)
                   .eq(SpaceOccupy::getReservationId, 0L);
        spaceOccupyMapper.delete(occupyQuery);

        boolean result = this.removeById(id);

        if (result) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("车辆管理");
            logDTO.setAction("删除车辆");
            logDTO.setDetail("车辆ID:" + id + "，车牌号:" + existingVehicle.getPlateNumber());
            operationLogService.createOperationLog(logDTO);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean setDefaultVehicle(Long userId, Long vehicleId) {
        // 先取消该用户的所有默认车辆
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getUserId, userId)
                   .eq(Vehicle::getIsDefault, 1);
        List<Vehicle> defaultVehicles = this.list(queryWrapper);
        
        for (Vehicle vehicle : defaultVehicles) {
            vehicle.setIsDefault(0);
            this.updateById(vehicle);
        }
        
        // 设置新的默认车辆
        Vehicle newDefaultVehicle = this.getById(vehicleId);
        if (newDefaultVehicle == null) {
            return false;
        }
        
        newDefaultVehicle.setIsDefault(1);
        return this.updateById(newDefaultVehicle);
    }

    @Override
    @Transactional
    public boolean batchUpdateVehicleStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<Vehicle> vehicles = ids.stream()
                .map(id -> {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setId(id);
                    vehicle.setStatus(status);
                    return vehicle;
                })
                .collect(Collectors.toList());
        
        return this.updateBatchById(vehicles);
    }
}
