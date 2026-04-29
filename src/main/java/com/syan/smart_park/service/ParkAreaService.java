package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.entity.ParkAreaOccupancyStats;

import java.util.List;

/**
 * 园区服务接口
 */
public interface ParkAreaService extends IService<ParkArea> {

    /**
     * 获取所有园区列表
     * @return 园区DTO列表
     */
    List<ParkAreaDTO> getAllParkAreas();

    /**
     * 根据ID获取园区详情
     * @param id 园区ID
     * @return 园区DTO
     */
    ParkAreaDTO getParkAreaById(Long id);

    /**
     * 创建园区
     * @param parkAreaDTO 园区DTO
     * @return 创建的园区DTO
     */
    ParkAreaDTO createParkArea(ParkAreaDTO parkAreaDTO);

    /**
     * 更新园区信息
     * @param id 园区ID
     * @param parkAreaDTO 园区DTO
     * @return 更新后的园区DTO
     */
    ParkAreaDTO updateParkArea(Long id, ParkAreaDTO parkAreaDTO);

    /**
     * 删除园区
     * @param id 园区ID
     * @return 是否删除成功
     */
    boolean deleteParkArea(Long id);

    /**
     * 根据状态获取园区列表
     * @param status 园区状态
     * @return 园区DTO列表
     */
    List<ParkAreaDTO> getParkAreasByStatus(Integer status);

    /**
     * 更新园区总车位数
     * @param parkAreaId 园区ID
     * @return 是否更新成功
     */
    boolean updateTotalSpaces(Long parkAreaId);

    /**
     * 更新所有园区的总车位数
     * @return 更新的园区数量
     */
    int updateAllTotalSpaces();
    
    /**
     * 获取园区占用统计信息
     *
     * @param parkAreaId 园区ID
     * @return 占用统计信息，包含总车位数、占用车位数、空闲车位数
     */
    ParkAreaOccupancyStats getParkAreaOccupancyStats(Long parkAreaId);
    
    /**
     * 获取所有园区的占用统计信息
     *
     * @return 所有园区的占用统计信息列表
     */
    List<ParkAreaOccupancyStats> getAllParkAreasOccupancyStats();
    
    /**
     * 搜索园区
     * @param keyword 搜索关键词（园区名称或地址）
     * @return 园区DTO列表
     */
    List<ParkAreaDTO> searchParkAreas(String keyword);
}
