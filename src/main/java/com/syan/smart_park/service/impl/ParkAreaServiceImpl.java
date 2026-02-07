package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkAreaMapper;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.service.ParkAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 园区服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkAreaServiceImpl extends ServiceImpl<ParkAreaMapper, ParkArea> implements ParkAreaService {

    private final ParkAreaMapper parkAreaMapper;

    @Override
    public List<ParkAreaDTO> getAllParkAreas() {
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getDeleted, 0)
                   .orderByDesc(ParkArea::getCreateTime);
        
        List<ParkArea> parkAreas = parkAreaMapper.selectList(queryWrapper);
        return parkAreas.stream()
                       .map(ParkAreaDTO::fromParkArea)
                       .collect(Collectors.toList());
    }

    @Override
    public ParkAreaDTO getParkAreaById(Long id) {
        ParkArea parkArea = parkAreaMapper.selectById(id);
        if (parkArea == null || parkArea.getDeleted() == 1) {
            return null;
        }
        return ParkAreaDTO.fromParkArea(parkArea);
    }

    @Override
    public ParkAreaDTO createParkArea(ParkAreaDTO parkAreaDTO) {
        ParkArea parkArea = parkAreaDTO.toParkArea();
        parkArea.setDeleted(0);
        
        int result = parkAreaMapper.insert(parkArea);
        if (result > 0) {
            return getParkAreaById(parkArea.getId());
        }
        return null;
    }

    @Override
    public ParkAreaDTO updateParkArea(Long id, ParkAreaDTO parkAreaDTO) {
        ParkArea existingParkArea = parkAreaMapper.selectById(id);
        if (existingParkArea == null || existingParkArea.getDeleted() == 1) {
            return null;
        }
        
        ParkArea parkArea = parkAreaDTO.toParkArea();
        parkArea.setId(id);
        parkArea.setDeleted(existingParkArea.getDeleted());
        
        int result = parkAreaMapper.updateById(parkArea);
        if (result > 0) {
            return getParkAreaById(id);
        }
        return null;
    }

    @Override
    public boolean deleteParkArea(Long id) {
        ParkArea parkArea = parkAreaMapper.selectById(id);
        if (parkArea == null || parkArea.getDeleted() == 1) {
            return false;
        }
        
        parkArea.setDeleted(1);
        int result = parkAreaMapper.updateById(parkArea);
        return result > 0;
    }

    @Override
    public List<ParkAreaDTO> getParkAreasByStatus(Integer status) {
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getDeleted, 0)
                   .eq(ParkArea::getStatus, status)
                   .orderByDesc(ParkArea::getCreateTime);
        
        List<ParkArea> parkAreas = parkAreaMapper.selectList(queryWrapper);
        return parkAreas.stream()
                       .map(ParkAreaDTO::fromParkArea)
                       .collect(Collectors.toList());
    }
}
