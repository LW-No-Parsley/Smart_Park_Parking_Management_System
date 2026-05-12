package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.Vehicle;
import com.syan.smart_park.entity.VehicleDTO;

import java.util.List;

/**
 * 车辆服务接口
 */
public interface VehicleService extends IService<Vehicle> {
    
    /**
     * 统一查询车辆列表（支持多条件筛选 + 分页）
     *
     * @param userId      用户ID（可选）
     * @param plateNumber 车牌号（可选，支持模糊匹配）
     * @param status      车辆状态（可选）
     * @param vehicleType 车辆类型（可选）
     * @param isDefault   是否默认车辆（可选）
     * @param page        页码
     * @param size        每页大小
     */
    PageResult<VehicleDTO> listVehicles(Long userId, String plateNumber, Integer status,
                                        Integer vehicleType, Integer isDefault, Integer page, Integer size);
    
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
     * 设置默认车辆
     */
    boolean setDefaultVehicle(Long userId, Long vehicleId);
    
    /**
     * 批量更新车辆状态
     */
    boolean batchUpdateVehicleStatus(List<Long> ids, Integer status);
}
