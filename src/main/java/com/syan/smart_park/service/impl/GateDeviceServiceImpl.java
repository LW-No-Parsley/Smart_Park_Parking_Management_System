package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.GateDeviceMapper;
import com.syan.smart_park.entity.GateDevice;
import com.syan.smart_park.entity.GateDeviceDTO;
import com.syan.smart_park.service.GateDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 道闸设备服务实现类
 */
@Service
@RequiredArgsConstructor
public class GateDeviceServiceImpl extends ServiceImpl<GateDeviceMapper, GateDevice> implements GateDeviceService {

    private final GateDeviceMapper gateDeviceMapper;

    @Override
    public List<GateDeviceDTO> getAllGateDevices() {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
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
            return getGateDeviceById(id);
        }
        return null;
    }

    @Override
    public boolean deleteGateDevice(Long id) {
        GateDevice gateDevice = gateDeviceMapper.selectById(id);
        if (gateDevice == null || gateDevice.getDeleted() == 1) {
            return false;
        }
        
        gateDevice.setDeleted(1);
        int result = gateDeviceMapper.updateById(gateDevice);
        return result > 0;
    }

    @Override
    public List<GateDeviceDTO> getGateDevicesByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getParkAreaId, parkAreaId)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
    }

    @Override
    public List<GateDeviceDTO> getGateDevicesByDeviceType(Integer deviceType) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getDeviceType, deviceType)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
    }

    @Override
    public List<GateDeviceDTO> getGateDevicesByStatus(Integer status) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getStatus, status)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
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
        
        for (GateDevice gateDevice : gateDevices) {
            gateDevice.setStatus(status);
            gateDeviceMapper.updateById(gateDevice);
        }
        
        return true;
    }

    @Override
    public GateDeviceDTO getGateDeviceByDeviceSn(String deviceSn) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getDeviceSn, deviceSn);
        
        GateDevice gateDevice = gateDeviceMapper.selectOne(queryWrapper);
        if (gateDevice == null) {
            return null;
        }
        return GateDeviceDTO.fromGateDevice(gateDevice);
    }

    @Override
    public List<GateDeviceDTO> getEntranceGateDevices(Long parkAreaId) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getDeviceType, 1) // 入口道闸
                   .eq(parkAreaId != null, GateDevice::getParkAreaId, parkAreaId)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
    }

    @Override
    public List<GateDeviceDTO> getExitGateDevices(Long parkAreaId) {
        LambdaQueryWrapper<GateDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GateDevice::getDeleted, 0)
                   .eq(GateDevice::getDeviceType, 2) // 出口道闸
                   .eq(parkAreaId != null, GateDevice::getParkAreaId, parkAreaId)
                   .orderByDesc(GateDevice::getCreateTime);
        
        List<GateDevice> gateDevices = gateDeviceMapper.selectList(queryWrapper);
        return gateDevices.stream()
                         .map(GateDeviceDTO::fromGateDevice)
                         .collect(Collectors.toList());
    }

    @Override
    public List<GateDeviceDTO> getOnlineGateDevices() {
        return getGateDevicesByStatus(1); // 在线状态
    }

    @Override
    public List<GateDeviceDTO> getFaultyGateDevices() {
        return getGateDevicesByStatus(2); // 故障状态
    }
}
