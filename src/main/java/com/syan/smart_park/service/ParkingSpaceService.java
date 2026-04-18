package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.ParkingSpace;
import com.syan.smart_park.entity.ParkingSpaceDTO;

import java.util.List;

/**
 * 车位服务接口
 */
public interface ParkingSpaceService extends IService<ParkingSpace> {
    
    /**
     * 获取车位详情（包含当前占用状态）
     *
     * @param id 车位ID
     * @return 车位详情（包含占用状态）
     */
    ParkingSpaceDTO getParkingSpaceWithOccupiedStatus(Long id);
    
    /**
     * 根据ID获取车位
     */
    ParkingSpaceDTO getParkingSpaceById(Long id);
    
    /**
     * 创建车位
     */
    ParkingSpaceDTO createParkingSpace(ParkingSpaceDTO parkingSpaceDTO);
    
    /**
     * 更新车位
     */
    ParkingSpaceDTO updateParkingSpace(Long id, ParkingSpaceDTO parkingSpaceDTO);
    
    /**
     * 删除车位
     */
    boolean deleteParkingSpace(Long id);
    
    /**
     * 根据园区ID获取车位列表
     */
    List<ParkingSpaceDTO> getParkingSpacesByParkAreaId(Long parkAreaId);
    
    /**
     * 根据分区ID获取车位列表
     */
    List<ParkingSpaceDTO> getParkingSpacesByZoneId(Long zoneId);
    
    /**
     * 根据车位状态获取车位列表
     */
    List<ParkingSpaceDTO> getParkingSpacesByStatus(Integer status);
    
    /**
     * 根据车位类型获取车位列表
     */
    List<ParkingSpaceDTO> getParkingSpacesByType(Integer spaceType);
    
    /**
     * 根据绑定用户ID获取车位列表
     */
    List<ParkingSpaceDTO> getParkingSpacesByBindUserId(Long bindUserId);
    
    /**
     * 获取空闲车位列表
     */
    List<ParkingSpaceDTO> getAvailableParkingSpaces();
    
    /**
     * 批量更新车位状态
     */
    boolean batchUpdateParkingSpaceStatus(List<Long> ids, Integer status);
    
    /**
     * 检查车位在当前时间是否被占用
     *
     * @param spaceId 车位ID
     * @return true-占用中，false-未占用
     */
    boolean isSpaceOccupied(Long spaceId);
    

    
    /**
     * 获取所有车位（包含当前占用状态）
     *
     * @return 车位列表（包含占用状态）
     */
    List<ParkingSpaceDTO> getAllParkingSpacesWithOccupiedStatus();
    
    /**
     * 根据园区ID获取车位列表（包含当前占用状态）
     *
     * @param parkAreaId 园区ID
     * @return 车位列表（包含占用状态）
     */
    List<ParkingSpaceDTO> getParkingSpacesByParkAreaIdWithOccupiedStatus(Long parkAreaId);
}
