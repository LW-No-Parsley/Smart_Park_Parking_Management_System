package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.entity.ParkingZoneDTO;

import java.util.List;

/**
 * 车位分区服务接口
 */
public interface ParkingZoneService extends IService<ParkingZone> {

    /**
     * 获取所有车位分区列表
     * @return 车位分区DTO列表
     */
    List<ParkingZoneDTO> getAllParkingZones();

    /**
     * 根据ID获取车位分区详情
     * @param id 分区ID
     * @return 车位分区DTO
     */
    ParkingZoneDTO getParkingZoneById(Long id);

    /**
     * 创建车位分区
     * @param parkingZoneDTO 车位分区DTO
     * @return 创建的车位分区DTO
     */
    ParkingZoneDTO createParkingZone(ParkingZoneDTO parkingZoneDTO);

    /**
     * 更新车位分区信息
     * @param id 分区ID
     * @param parkingZoneDTO 车位分区DTO
     * @return 更新后的车位分区DTO
     */
    ParkingZoneDTO updateParkingZone(Long id, ParkingZoneDTO parkingZoneDTO);

    /**
     * 删除车位分区
     * @param id 分区ID
     * @return 是否删除成功
     */
    boolean deleteParkingZone(Long id);

    /**
     * 根据园区ID获取车位分区列表
     * @param parkAreaId 园区ID
     * @return 车位分区DTO列表
     */
    List<ParkingZoneDTO> getParkingZonesByParkAreaId(Long parkAreaId);

    /**
     * 根据状态获取车位分区列表
     * @param status 分区状态
     * @return 车位分区DTO列表
     */
    List<ParkingZoneDTO> getParkingZonesByStatus(Integer status);
}
