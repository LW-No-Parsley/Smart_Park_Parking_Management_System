package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkAreaDTO;

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
}
