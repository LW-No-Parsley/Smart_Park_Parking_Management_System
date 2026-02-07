package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.GateDeviceDTO;
import com.syan.smart_park.service.GateDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 道闸设备控制器
 */
@RestController
@RequestMapping("/api/gate-device")
@RequiredArgsConstructor
public class GateDeviceController {

    private final GateDeviceService gateDeviceService;

    /**
     * 获取所有道闸设备列表
     *
     * @return 道闸设备列表
     */
    @GetMapping("/list")
    public R<List<GateDeviceDTO>> getAllGateDevices() {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getAllGateDevices();
        return R.success(gateDevices);
    }

    /**
     * 根据ID获取道闸设备详情
     *
     * @param id 设备ID
     * @return 道闸设备详情
     */
    @GetMapping("/{id}")
    public R<GateDeviceDTO> getGateDeviceById(@PathVariable Long id) {
        GateDeviceDTO gateDeviceDTO = gateDeviceService.getGateDeviceById(id);
        if (gateDeviceDTO == null) {
            return R.error(ReturnCode.RC602); // 道闸设备不存在
        }
        return R.success(gateDeviceDTO);
    }

    /**
     * 创建道闸设备
     *
     * @param gateDeviceDTO 道闸设备信息
     * @return 创建的道闸设备
     */
    @PostMapping
    public R<GateDeviceDTO> createGateDevice(@Valid @RequestBody GateDeviceDTO gateDeviceDTO) {
        GateDeviceDTO createdGateDevice = gateDeviceService.createGateDevice(gateDeviceDTO);
        if (createdGateDevice == null) {
            return R.error(ReturnCode.RC500); // 创建失败
        }
        return R.success(createdGateDevice);
    }

    /**
     * 更新道闸设备信息
     *
     * @param id 设备ID
     * @param gateDeviceDTO 道闸设备信息
     * @return 更新后的道闸设备
     */
    @PutMapping("/{id}")
    public R<GateDeviceDTO> updateGateDevice(@PathVariable Long id, @Valid @RequestBody GateDeviceDTO gateDeviceDTO) {
        GateDeviceDTO updatedGateDevice = gateDeviceService.updateGateDevice(id, gateDeviceDTO);
        if (updatedGateDevice == null) {
            return R.error(ReturnCode.RC602); // 道闸设备不存在或更新失败
        }
        return R.success(updatedGateDevice);
    }

    /**
     * 删除道闸设备
     *
     * @param id 设备ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteGateDevice(@PathVariable Long id) {
        boolean success = gateDeviceService.deleteGateDevice(id);
        if (!success) {
            return R.error(ReturnCode.RC602); // 道闸设备不存在或删除失败
        }
        return R.success(true);
    }

    /**
     * 根据园区ID获取道闸设备列表
     *
     * @param parkAreaId 园区ID
     * @return 道闸设备列表
     */
    @GetMapping("/park-area/{parkAreaId}")
    public R<List<GateDeviceDTO>> getGateDevicesByParkAreaId(@PathVariable Long parkAreaId) {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getGateDevicesByParkAreaId(parkAreaId);
        return R.success(gateDevices);
    }

    /**
     * 根据设备类型获取道闸设备列表
     *
     * @param deviceType 设备类型：1-入口道闸，2-出口道闸
     * @return 道闸设备列表
     */
    @GetMapping("/device-type/{deviceType}")
    public R<List<GateDeviceDTO>> getGateDevicesByDeviceType(@PathVariable Integer deviceType) {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getGateDevicesByDeviceType(deviceType);
        return R.success(gateDevices);
    }

    /**
     * 根据设备状态获取道闸设备列表
     *
     * @param status 设备状态：0-离线，1-在线，2-故障
     * @return 道闸设备列表
     */
    @GetMapping("/status/{status}")
    public R<List<GateDeviceDTO>> getGateDevicesByStatus(@PathVariable Integer status) {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getGateDevicesByStatus(status);
        return R.success(gateDevices);
    }

    /**
     * 更新设备心跳时间
     *
     * @param id 设备ID
     * @param lastHeartbeat 最后心跳时间
     * @return 更新结果
     */
    @PutMapping("/{id}/heartbeat")
    public R<Boolean> updateHeartbeat(@PathVariable Long id, @RequestBody LocalDateTime lastHeartbeat) {
        boolean success = gateDeviceService.updateHeartbeat(id, lastHeartbeat);
        if (!success) {
            return R.error(ReturnCode.RC602); // 道闸设备不存在或更新失败
        }
        return R.success(true);
    }

    /**
     * 批量更新设备状态
     *
     * @param ids 设备ID列表
     * @param status 设备状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    public R<Boolean> batchUpdateStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean success = gateDeviceService.batchUpdateStatus(ids, status);
        if (!success) {
            return R.error(ReturnCode.RC500); // 批量更新失败
        }
        return R.success(true);
    }

    /**
     * 根据设备序列号获取道闸设备
     *
     * @param deviceSn 设备序列号
     * @return 道闸设备详情
     */
    @GetMapping("/device-sn/{deviceSn}")
    public R<GateDeviceDTO> getGateDeviceByDeviceSn(@PathVariable String deviceSn) {
        GateDeviceDTO gateDeviceDTO = gateDeviceService.getGateDeviceByDeviceSn(deviceSn);
        if (gateDeviceDTO == null) {
            return R.error(ReturnCode.RC602); // 道闸设备不存在
        }
        return R.success(gateDeviceDTO);
    }

    /**
     * 获取园区入口道闸设备列表
     *
     * @param parkAreaId 园区ID
     * @return 入口道闸设备列表
     */
    @GetMapping("/entrance/{parkAreaId}")
    public R<List<GateDeviceDTO>> getEntranceGateDevices(@PathVariable Long parkAreaId) {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getEntranceGateDevices(parkAreaId);
        return R.success(gateDevices);
    }

    /**
     * 获取园区出口道闸设备列表
     *
     * @param parkAreaId 园区ID
     * @return 出口道闸设备列表
     */
    @GetMapping("/exit/{parkAreaId}")
    public R<List<GateDeviceDTO>> getExitGateDevices(@PathVariable Long parkAreaId) {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getExitGateDevices(parkAreaId);
        return R.success(gateDevices);
    }

    /**
     * 获取在线道闸设备列表
     *
     * @return 在线道闸设备列表
     */
    @GetMapping("/online")
    public R<List<GateDeviceDTO>> getOnlineGateDevices() {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getOnlineGateDevices();
        return R.success(gateDevices);
    }

    /**
     * 获取故障道闸设备列表
     *
     * @return 故障道闸设备列表
     */
    @GetMapping("/faulty")
    public R<List<GateDeviceDTO>> getFaultyGateDevices() {
        List<GateDeviceDTO> gateDevices = gateDeviceService.getFaultyGateDevices();
        return R.success(gateDevices);
    }

    /**
     * 搜索道闸设备（按名称或序列号）
     *
     * @param keyword 搜索关键词
     * @return 道闸设备列表
     */
    @GetMapping("/search")
    public R<List<GateDeviceDTO>> searchGateDevices(@RequestParam String keyword) {
        // 这里需要实现搜索逻辑，暂时返回所有道闸设备
        // 实际项目中应该实现具体的搜索逻辑
        List<GateDeviceDTO> gateDevices = gateDeviceService.getAllGateDevices();
        return R.success(gateDevices);
    }
}
