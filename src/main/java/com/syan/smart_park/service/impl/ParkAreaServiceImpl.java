package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkAreaMapper;
import com.syan.smart_park.dao.ParkingSpaceMapper;
import com.syan.smart_park.dao.SpaceOccupyMapper;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.entity.ParkAreaOccupancyStats;
import com.syan.smart_park.service.ParkAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 园区服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ParkAreaServiceImpl extends ServiceImpl<ParkAreaMapper, ParkArea> implements ParkAreaService {

    private final ParkAreaMapper parkAreaMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final SpaceOccupyMapper spaceOccupyMapper;

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
        
        // 使用MyBatis Plus的removeById方法进行逻辑删除
        // 这会自动设置deleted=1，因为实体上有@TableLogic注解
        boolean result = removeById(id);
        return result;
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

    @Override
    public boolean updateTotalSpaces(Long parkAreaId) {
        // 检查园区是否存在且未删除
        ParkArea parkArea = parkAreaMapper.selectById(parkAreaId);
        if (parkArea == null || parkArea.getDeleted() == 1) {
            return false;
        }
        
        // 查询该园区下的有效车位数量
        LambdaQueryWrapper<com.syan.smart_park.entity.ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.syan.smart_park.entity.ParkingSpace::getParkAreaId, parkAreaId)
                   .eq(com.syan.smart_park.entity.ParkingSpace::getDeleted, 0);
        
        Long totalSpaces = parkingSpaceMapper.selectCount(queryWrapper);
        
        // 更新园区的总车位数
        parkArea.setTotalSpaces(totalSpaces.intValue());
        int result = parkAreaMapper.updateById(parkArea);
        
        return result > 0;
    }

    @Override
    public int updateAllTotalSpaces() {
        // 获取所有未删除的园区
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getDeleted, 0);
        
        List<ParkArea> parkAreas = parkAreaMapper.selectList(queryWrapper);
        int updatedCount = 0;
        
        // 更新每个园区的总车位数
        for (ParkArea parkArea : parkAreas) {
            boolean success = updateTotalSpaces(parkArea.getId());
            if (success) {
                updatedCount++;
            }
        }
        
        return updatedCount;
    }

    @Override
    public ParkAreaOccupancyStats getParkAreaOccupancyStats(Long parkAreaId) {
        // 检查园区是否存在且未删除
        ParkArea parkArea = parkAreaMapper.selectById(parkAreaId);
        if (parkArea == null || parkArea.getDeleted() == 1) {
            return null;
        }
        
        // 获取总车位数
        Integer totalSpaces = parkArea.getTotalSpaces();
        
        // 获取当前时间占用的车位数
        LocalDateTime currentTime = LocalDateTime.now();
        Long occupiedSpaces = spaceOccupyMapper.countOccupiedSpacesByParkAreaIdAndTime(parkAreaId, currentTime);
        
        if (occupiedSpaces == null) {
            occupiedSpaces = 0L;
        }
        
        return new ParkAreaOccupancyStats(
            parkAreaId,
            parkArea.getName(),
            totalSpaces,
            occupiedSpaces.intValue()
        );
    }

    @Override
    public List<ParkAreaOccupancyStats> getAllParkAreasOccupancyStats() {
        // 获取所有未删除的园区
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getDeleted, 0);
        
        List<ParkArea> parkAreas = parkAreaMapper.selectList(queryWrapper);
        LocalDateTime currentTime = LocalDateTime.now();
        
        return parkAreas.stream()
                .map(parkArea -> {
                    // 获取总车位数
                    Integer totalSpaces = parkArea.getTotalSpaces();
                    
                    // 获取当前时间占用的车位数
                    Long occupiedSpaces = spaceOccupyMapper.countOccupiedSpacesByParkAreaIdAndTime(
                        parkArea.getId(), currentTime);
                    
                    if (occupiedSpaces == null) {
                        occupiedSpaces = 0L;
                    }
                    
                    return new ParkAreaOccupancyStats(
                        parkArea.getId(),
                        parkArea.getName(),
                        totalSpaces,
                        occupiedSpaces.intValue()
                    );
                })
                .collect(Collectors.toList());
    }
}
