package com.syan.smart_park.controller;

import com.syan.smart_park.common.PageResult;
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
     * 统一分页查询道闸设备列表（支持多条件组合筛选）
     * 合并了原 /park-area/* /device-type/* /status/* /device-sn/*
     *        /entrance/* /exit/* /online /faulty /search 等路由
     */
    @GetMapping("/list")
    public R<PageResult<GateDeviceDTO>> getGateDeviceList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) Integer deviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String deviceSn) {
        PageResult<GateDeviceDTO> result = gateDeviceService.pageGateDevices(
                current, size, parkAreaId, deviceType, status, deviceSn);
        return R.success(result);
    }

    /**
     * 根据ID获取道闸设备详情
     */
    @GetMapping("/{id}")
    public R<GateDeviceDTO> getGateDeviceById(@PathVariable Long id) {
        GateDeviceDTO gateDeviceDTO = gateDeviceService.getGateDeviceById(id);
        if (gateDeviceDTO == null) {
            return R.error(ReturnCode.RC602);
        }
        return R.success(gateDeviceDTO);
    }

    /**
     * 创建道闸设备
     */
    @PostMapping
    public R<GateDeviceDTO> createGateDevice(@Valid @RequestBody GateDeviceDTO gateDeviceDTO) {
        GateDeviceDTO createdGateDevice = gateDeviceService.createGateDevice(gateDeviceDTO);
        if (createdGateDevice == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(createdGateDevice);
    }

    /**
     * 更新道闸设备信息
     */
    @PutMapping("/{id}")
    public R<GateDeviceDTO> updateGateDevice(@PathVariable Long id,
                                             @Valid @RequestBody GateDeviceDTO gateDeviceDTO) {
        GateDeviceDTO updatedGateDevice = gateDeviceService.updateGateDevice(id, gateDeviceDTO);
        if (updatedGateDevice == null) {
            return R.error(ReturnCode.RC602);
        }
        return R.success(updatedGateDevice);
    }

    /**
     * 更新设备心跳时间
     */
    @PutMapping("/{id}/heartbeat")
    public R<Boolean> updateHeartbeat(@PathVariable Long id,
                                      @RequestBody LocalDateTime lastHeartbeat) {
        boolean success = gateDeviceService.updateHeartbeat(id, lastHeartbeat);
        if (!success) {
            return R.error(ReturnCode.RC602);
        }
        return R.success(true);
    }

    /**
     * 批量更新设备状态
     */
    @PutMapping("/batch/status")
    public R<Boolean> batchUpdateStatus(@RequestParam List<Long> ids,
                                        @RequestParam Integer status) {
        boolean success = gateDeviceService.batchUpdateStatus(ids, status);
        if (!success) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(true);
    }
}
