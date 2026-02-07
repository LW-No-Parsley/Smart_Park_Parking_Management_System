package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkingZoneMapper;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.entity.ParkingZoneDTO;
import com.syan.smart_park.service.ParkingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车位分区服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingZoneServiceImpl extends ServiceImpl<ParkingZoneMapper, ParkingZone> implements ParkingZoneService {

    private final ParkingZoneMapper parkingZoneMapper;

    @Override
    public List<ParkingZoneDTO> getAllParkingZones() {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return parkingZones.stream()
                          .map(ParkingZoneDTO::fromParkingZone)
                          .collect(Collectors.toList());
    }

    @Override
    public ParkingZoneDTO getParkingZoneById(Long id) {
        ParkingZone parkingZone = parkingZoneMapper.selectById(id);
        if (parkingZone == null || parkingZone.getDeleted() == 1) {
            return null;
        }
        return ParkingZoneDTO.fromParkingZone(parkingZone);
    }

    @Override
    public ParkingZoneDTO createParkingZone(ParkingZoneDTO parkingZoneDTO) {
        ParkingZone parkingZone = parkingZoneDTO.toParkingZone();
        parkingZone.setDeleted(0);
        
        int result = parkingZoneMapper.insert(parkingZone);
        if (result > 0) {
            return getParkingZoneById(parkingZone.getId());
        }
        return null;
    }

    @Override
    public ParkingZoneDTO updateParkingZone(Long id, ParkingZoneDTO parkingZoneDTO) {
        ParkingZone existingParkingZone = parkingZoneMapper.selectById(id);
        if (existingParkingZone == null || existingParkingZone.getDeleted() == 1) {
            return null;
        }
        
        ParkingZone parkingZone = parkingZoneDTO.toParkingZone();
        parkingZone.setId(id);
        parkingZone.setDeleted(existingParkingZone.getDeleted());
        
        int result = parkingZoneMapper.updateById(parkingZone);
        if (result > 0) {
            return getParkingZoneById(id);
        }
        return null;
    }

    @Override
    public boolean deleteParkingZone(Long id) {
        ParkingZone parkingZone = parkingZoneMapper.selectById(id);
        if (parkingZone == null || parkingZone.getDeleted() == 1) {
            return false;
        }
        
        parkingZone.setDeleted(1);
        int result = parkingZoneMapper.updateById(parkingZone);
        return result > 0;
    }

    @Override
    public List<ParkingZoneDTO> getParkingZonesByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .eq(ParkingZone::getParkAreaId, parkAreaId)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return parkingZones.stream()
                          .map(ParkingZoneDTO::fromParkingZone)
                          .collect(Collectors.toList());
    }

    @Override
    public List<ParkingZoneDTO> getParkingZonesByStatus(Integer status) {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .eq(ParkingZone::getStatus, status)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return parkingZones.stream()
                          .map(ParkingZoneDTO::fromParkingZone)
                          .collect(Collectors.toList());
    }
}
