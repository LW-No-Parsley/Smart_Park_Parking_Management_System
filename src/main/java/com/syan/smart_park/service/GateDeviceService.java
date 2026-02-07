package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.GateDevice;
import com.syan.smart_park.entity.GateDeviceDTO;

import java.util.List;

/**
 * 道闸设备服务接口
 */
public interface GateDeviceService extends IService<GateDevice> {

    /**
     * 获取所有道闸设备列表
     * @return 道闸设备DTO列表
     */
    List<GateDeviceDTO> getAllGateDevices();

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
     * 删除道闸设备
     * @param id 设备ID
     * @return 是否删除成功
     */
    boolean deleteGateDevice(Long id);

    /**
     * 根据园区ID获取道闸设备列表
     * @param parkAreaId 园区ID
     * @return 道闸设备DTO列表
     */
    List<GateDeviceDTO> getGateDevicesByParkAreaId(Long parkAreaId);

    /**
     * 根据设备类型获取道闸设备列表
     * @param deviceType 设备类型：1-入口道闸，2-出口道闸
     * @return 道闸设备DTO列表
     */
    List<GateDeviceDTO> getGateDevicesByDeviceType(Integer deviceType);

    /**
     * 根据设备状态获取道闸设备列表
     * @param status 设备状态：0-离线，1-在线，2-故障
     * @return 道闸设备DTO列表
     */
    List<GateDeviceDTO> getGateDevicesByStatus(Integer status);

    /**
     * 更新设备心跳时间
     * @param id 设备ID
     * @param lastHeartbeat 最后心跳时间
     * @return 是否更新成功
     */
    boolean updateHeartbeat(Long id, java.time.LocalDateTime lastHeartbeat);

    /**
     * 批量更新设备状态
     * @param ids 设备ID列表
     * @param status 设备状态
     * @return 是否更新成功
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 根据设备序列号获取道闸设备
     * @param deviceSn 设备序列号
     * @return 道闸设备DTO
     */
    GateDeviceDTO getGateDeviceByDeviceSn(String deviceSn);

    /**
     * 获取园区入口道闸设备列表
     * @param parkAreaId 园区ID
     * @return 入口道闸设备DTO列表
     */
    List<GateDeviceDTO> getEntranceGateDevices(Long parkAreaId);

    /**
     * 获取园区出口道闸设备列表
     * @param parkAreaId 园区ID
     * @return 出口道闸设备DTO列表
     */
    List<GateDeviceDTO> getExitGateDevices(Long parkAreaId);

    /**
     * 获取在线道闸设备列表
     * @return 在线道闸设备DTO列表
     */
    List<GateDeviceDTO> getOnlineGateDevices();

    /**
     * 获取故障道闸设备列表
     * @return 故障道闸设备DTO列表
     */
    List<GateDeviceDTO> getFaultyGateDevices();
}
