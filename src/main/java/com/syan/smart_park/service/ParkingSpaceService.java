package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.ParkingSpace;
import com.syan.smart_park.entity.ParkingSpaceDTO;

import java.util.List;

/**
 * 车位服务接口
 */
public interface ParkingSpaceService extends IService<ParkingSpace> {
    
    /**
     * 统一查询车位列表（支持多条件筛选 + 分页）
     */
    PageResult<ParkingSpaceDTO> listParkingSpaces(Long parkAreaId, Long zoneId, Integer status,
                                                  Integer spaceType, Long bindUserId,
                                                  Boolean available, Boolean withOccupied,
                                                  Integer page, Integer size);
    
    /**
     * 获取车位详情（包含当前占用状态）
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
     * 获取可用于车辆绑定（长期占用）的车位列表
     * 返回状态正常且没有被其他车辆长期绑定的车位
     * 预约产生的临时占用不影响绑定选择
     *
     * @return 可用于绑定的车位列表
     */
    List<ParkingSpaceDTO> getSpacesAvailableForBinding();
}
