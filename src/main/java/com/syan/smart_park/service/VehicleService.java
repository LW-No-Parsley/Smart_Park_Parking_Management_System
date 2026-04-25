package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.Vehicle;
import com.syan.smart_park.entity.VehicleDTO;

import java.util.List;

/**
 * 车辆服务接口
 */
public interface VehicleService extends IService<Vehicle> {
    
    /**
     * 获取所有车辆
     */
    List<VehicleDTO> getAllVehicles();
    
    /**
     * 根据ID获取车辆
     */
    VehicleDTO getVehicleById(Long id);
    
    /**
     * 创建车辆
     */
    VehicleDTO createVehicle(VehicleDTO vehicleDTO);
    
    /**
     * 更新车辆
     */
    VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO);
    
    /**
     * 删除车辆
     */
    boolean deleteVehicle(Long id);
    
    /**
     * 根据用户ID获取车辆列表
     */
    List<VehicleDTO> getVehiclesByUserId(Long userId);

    
    /**
     * 根据车牌号获取车辆
     */
    VehicleDTO getVehicleByPlateNumber(String plateNumber);
    
    /**
     * 根据车辆状态获取车辆列表
     */
    List<VehicleDTO> getVehiclesByStatus(Integer status);
    
    /**
     * 根据车辆类型获取车辆列表
     */
    List<VehicleDTO> getVehiclesByType(Integer vehicleType);
    
    /**
     * 获取用户的默认车辆
     */
    VehicleDTO getDefaultVehicleByUserId(Long userId);
    
    /**
     * 设置默认车辆
     */
    boolean setDefaultVehicle(Long userId, Long vehicleId);
    
    /**
     * 批量更新车辆状态
     */
    boolean batchUpdateVehicleStatus(List<Long> ids, Integer status);
}
