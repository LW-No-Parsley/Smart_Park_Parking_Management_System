package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.dao.GateDeviceMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.GateDevice;
import com.syan.smart_park.entity.GateDeviceDTO;
import com.syan.smart_park.service.GateDeviceService;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 道闸设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GateDeviceServiceImpl extends ServiceImpl<GateDeviceMapper, GateDevice> implements GateDeviceService {

    private final GateDeviceMapper gateDeviceMapper;
    private final OperationLogService operationLogService;

    @Override
    public PageResult<GateDeviceDTO> pageGateDevices(long current, long size,
                                                     Long parkAreaId, Integer deviceType,
                                                     Integer status, String deviceSn) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(parkAreaId != null, GateDevice::getParkAreaId, parkAreaId);
        queryWrapper.eq(deviceType != null, GateDevice::getDeviceType, deviceType);
        queryWrapper.eq(status != null, GateDevice::getStatus, status);
        queryWrapper.eq(deviceSn != null && !deviceSn.isEmpty(), GateDevice::getDeviceSn, deviceSn);
        queryWrapper.orderByDesc(GateDevice::getCreateTime);

        IPage<GateDevice> page = this.page(new Page<>(current, size), queryWrapper);
        List<GateDeviceDTO> dtoList = page.getRecords().stream()
                .map(GateDeviceDTO::fromGateDevice)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public GateDeviceDTO getGateDeviceById(Long id) {
        GateDevice gateDevice = gateDeviceMapper.selectById(id);
        if (gateDevice == null || gateDevice.getDeleted() == 1) {
            return null;
        }
        return GateDeviceDTO.fromGateDevice(gateDevice);
    }

    @Override
    public GateDeviceDTO createGateDevice(GateDeviceDTO gateDeviceDTO) {
        GateDevice gateDevice = gateDeviceDTO.toGateDevice();
        gateDevice.setDeleted(0);
        
        int result = gateDeviceMapper.insert(gateDevice);
        if (result > 0) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("道闸设备管理");
            logDTO.setAction("创建设备");
            logDTO.setDetail("设备ID:" + gateDevice.getId() + "，名称:" + gateDevice.getGateName() + "，设备编号:" + gateDevice.getDeviceSn());
            operationLogService.createOperationLog(logDTO);
            
            return getGateDeviceById(gateDevice.getId());
        }
        return null;
    }

    @Override
    public GateDeviceDTO updateGateDevice(Long id, GateDeviceDTO gateDeviceDTO) {
        GateDevice existingGateDevice = gateDeviceMapper.selectById(id);
        if (existingGateDevice == null || existingGateDevice.getDeleted() == 1) {
            return null;
        }
        
        GateDevice gateDevice = gateDeviceDTO.toGateDevice();
        gateDevice.setId(id);
        gateDevice.setDeleted(existingGateDevice.getDeleted());
        
        int result = gateDeviceMapper.updateById(gateDevice);
        if (result > 0) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("道闸设备管理");
            logDTO.setAction("更新设备");
            logDTO.setDetail("设备ID:" + id + "，名称:" + gateDevice.getGateName() + "，设备编号:" + gateDevice.getDeviceSn());
            operationLogService.createOperationLog(logDTO);
            
            return getGateDeviceById(id);
        }
        return null;
    }

    @Override
    public boolean updateHeartbeat(Long id, LocalDateTime lastHeartbeat) {
        GateDevice gateDevice = gateDeviceMapper.selectById(id);
        if (gateDevice == null || gateDevice.getDeleted() == 1) {
            return false;
        }
        
        gateDevice.setLastHeartbeat(lastHeartbeat);
        int result = gateDeviceMapper.updateById(gateDevice);
        return result > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GateDevice::getId, ids)
                   .eq(GateDevice::getDeleted, 0);

        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        if (gateDevices.isEmpty()) {
            return false;
        }

        // 校验所有记录属于同一园区，防止越权跨园区操作
        Set<Long> areaIds = gateDevices.stream()
                .map(GateDevice::getParkAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (areaIds.size() > 1) {
            log.warn("批量操作跨园区: IDs=" + ids + ", areas=" + areaIds);
            throw new BusinessException(ReturnCode.RC403, "不允许跨园区批量操作");
        }

        for (GateDevice gateDevice : gateDevices) {
            gateDevice.setStatus(status);
            gateDeviceMapper.updateById(gateDevice);
        }

        return true;
    }
}
