package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.GateDevice;
import com.syan.smart_park.entity.GateDeviceDTO;

import java.util.List;

/**
 * 道闸设备服务接口
 */
public interface GateDeviceService extends IService<GateDevice> {

    /**
     * 统一分页查询道闸设备（支持多条件组合筛选）
     *
     * @param current      当前页码
     * @param size         每页大小
     * @param parkAreaId   园区ID（可选）
     * @param deviceType   设备类型：1-入口道闸 2-出口道闸（可选）
     * @param status       设备状态：0-离线 1-在线 2-故障（可选）
     * @param deviceSn     设备序列号（可选）
     * @return 分页结果
     */
    PageResult<GateDeviceDTO> pageGateDevices(long current, long size,
                                              Long parkAreaId, Integer deviceType,
                                              Integer status, String deviceSn);

    /**
     * 根据ID获取道闸设备详情
     * @param id 设备ID
     * @return 道闸设备DTO
     */
    GateDeviceDTO getGateDeviceById(Long id);

    /**
     * 创建道闸设备
     * @param gateDeviceDTO 道闸设备DTO
     * @return 创建的道闸设备DTO
     */
    GateDeviceDTO createGateDevice(GateDeviceDTO gateDeviceDTO);

    /**
     * 更新道闸设备信息
     * @param id 设备ID
     * @param gateDeviceDTO 道闸设备DTO
     * @return 更新后的道闸设备DTO
     */
    GateDeviceDTO updateGateDevice(Long id, GateDeviceDTO gateDeviceDTO);

    /**
     * 更新设备心跳时间
     * @param id 设备ID
     * @param lastHeartbeat 最后心跳时间
     * @return 是否更新成功
     */
    boolean updateHeartbeat(Long id, java.time.LocalDateTime lastHeartbeat);

    /**
     * 更新单个设备状态
     * @param id 设备ID
     * @param status 设备状态
     * @return 是否更新成功
     */
    boolean updateGateDeviceStatus(Long id, Integer status);

    /**
     * 批量更新设备状态
     * @param ids 设备ID列表
     * @param status 设备状态
     * @return 是否更新成功
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
}
