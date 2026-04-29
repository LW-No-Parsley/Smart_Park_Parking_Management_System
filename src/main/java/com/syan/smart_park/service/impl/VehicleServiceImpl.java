package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkUserMapper;
import com.syan.smart_park.dao.VehicleMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.Vehicle;
import com.syan.smart_park.entity.VehicleDTO;
import com.syan.smart_park.service.OperationLogService;
import com.syan.smart_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public List<VehicleDTO> getAllVehicles() {
        List<Vehicle> vehicles = this.list();
        List<VehicleDTO> dtos = vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
        fillUsernames(dtos);
        return dtos;
    }

    @Override
    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = this.getById(id);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);
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
            // 检查是否已存在相同车牌号的记录（排除当前记录）
            LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Vehicle::getPlateNumber, vehicleDTO.getPlateNumber())
                       .ne(Vehicle::getId, id); // 排除当前记录
            Long count = this.count(queryWrapper);
            
            if (count > 0) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC1301, 
                    "该车牌号已存在"
                );
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
        
        return dto;
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long id) {
        Vehicle existingVehicle = this.getById(id);
        if (existingVehicle == null) {
            return false;
        }
        
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
    public List<VehicleDTO> getVehiclesByUserId(Long userId) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getUserId, userId);
        List<Vehicle> vehicles = this.list(queryWrapper);
        List<VehicleDTO> dtos = vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
        fillUsernames(dtos);
        return dtos;
    }

    @Override
    public VehicleDTO getVehicleByPlateNumber(String plateNumber) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getPlateNumber, plateNumber);
        Vehicle vehicle = this.getOne(queryWrapper);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);
        return dto;
    }

    @Override
    public List<VehicleDTO> getVehiclesByStatus(Integer status) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getStatus, status);
        List<Vehicle> vehicles = this.list(queryWrapper);
        List<VehicleDTO> dtos = vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
        fillUsernames(dtos);
        return dtos;
    }

    @Override
    public List<VehicleDTO> getVehiclesByType(Integer vehicleType) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getVehicleType, vehicleType);
        List<Vehicle> vehicles = this.list(queryWrapper);
        List<VehicleDTO> dtos = vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
        fillUsernames(dtos);
        return dtos;
    }

    @Override
    public VehicleDTO getDefaultVehicleByUserId(Long userId) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getUserId, userId)
                   .eq(Vehicle::getIsDefault, 1);
        Vehicle vehicle = this.getOne(queryWrapper);
        VehicleDTO dto = VehicleDTO.fromVehicle(vehicle);
        fillUsername(dto);
        return dto;
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
