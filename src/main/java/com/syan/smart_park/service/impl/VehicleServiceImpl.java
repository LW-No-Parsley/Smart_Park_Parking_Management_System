package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.VehicleMapper;
import com.syan.smart_park.entity.Vehicle;
import com.syan.smart_park.entity.VehicleDTO;
import com.syan.smart_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆服务实现类
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private final VehicleMapper vehicleMapper;

    @Override
    public List<VehicleDTO> getAllVehicles() {
        List<Vehicle> vehicles = this.list();
        return vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = this.getById(id);
        return VehicleDTO.fromVehicle(vehicle);
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
        return VehicleDTO.fromVehicle(vehicle);
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
        
        return VehicleDTO.fromVehicle(vehicle);
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<VehicleDTO> getVehiclesByUserId(Long userId) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getUserId, userId);
        List<Vehicle> vehicles = this.list(queryWrapper);
        
        return vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDTO getVehicleByPlateNumber(String plateNumber) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getPlateNumber, plateNumber);
        Vehicle vehicle = this.getOne(queryWrapper);
        
        return VehicleDTO.fromVehicle(vehicle);
    }

    @Override
    public List<VehicleDTO> getVehiclesByStatus(Integer status) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getStatus, status);
        List<Vehicle> vehicles = this.list(queryWrapper);
        
        return vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleDTO> getVehiclesByType(Integer vehicleType) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getVehicleType, vehicleType);
        List<Vehicle> vehicles = this.list(queryWrapper);
        
        return vehicles.stream()
                .map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDTO getDefaultVehicleByUserId(Long userId) {
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getUserId, userId)
                   .eq(Vehicle::getIsDefault, 1);
        Vehicle vehicle = this.getOne(queryWrapper);
        
        return VehicleDTO.fromVehicle(vehicle);
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
