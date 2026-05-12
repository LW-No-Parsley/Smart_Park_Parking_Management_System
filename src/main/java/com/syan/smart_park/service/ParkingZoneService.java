package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.entity.ParkingZoneDTO;

import java.util.List;

/**
 * 车位分区服务接口
 */
public interface ParkingZoneService extends IService<ParkingZone> {

    /**
     * 统一查询车位分区列表（支持多条件筛选 + 分页）
     *
     * @param parkAreaId 园区ID（可选）
     * @param status     分区状态（可选）
     * @param keyword    搜索关键词，按分区名称模糊搜索（可选）
     * @param page       页码
     * @param size       每页大小
     */
    PageResult<ParkingZoneDTO> listParkingZones(Long parkAreaId, Integer status, String keyword, Integer page, Integer size);

    /**
     * 根据ID获取车位分区详情
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
     * 批量更新分区状态
     * @param ids 分区ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean batchUpdateParkingZoneStatus(List<Long> ids, Integer status);

    /**
     * 批量删除分区
     * @param ids 分区ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteParkingZones(List<Long> ids);
}
