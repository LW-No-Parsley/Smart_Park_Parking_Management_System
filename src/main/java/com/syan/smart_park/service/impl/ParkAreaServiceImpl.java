package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.dao.ParkAreaMapper;
import com.syan.smart_park.dao.ParkingSpaceMapper;
import com.syan.smart_park.dao.ReservationMapper;
import com.syan.smart_park.dao.SpaceOccupyMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.entity.ParkAreaOccupancyStats;
import com.syan.smart_park.service.OperationLogService;
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
    private final ReservationMapper reservationMapper;
    private final OperationLogService operationLogService;

    @Override
    public PageResult<ParkAreaDTO> listParkAreas(Integer status, String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getDeleted, 0);
        if (status != null) {
            queryWrapper.eq(ParkArea::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like(ParkArea::getName, keyword.trim())
                                   .or().like(ParkArea::getAddress, keyword.trim()));
        }
        queryWrapper.orderByDesc(ParkArea::getCreateTime);

        Page<ParkArea> mpPage = new Page<>(page, size);
        Page<ParkArea> resultPage = parkAreaMapper.selectPage(mpPage, queryWrapper);

        List<ParkAreaDTO> dtos = resultPage.getRecords().stream()
                .map(ParkAreaDTO::fromParkArea)
                .collect(Collectors.toList());

        return PageResult.of(dtos, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
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
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("园区管理");
            logDTO.setAction("创建园区");
            logDTO.setDetail("园区ID:" + parkArea.getId() + "，名称:" + parkArea.getName() + "，地址:" + parkArea.getAddress());
            operationLogService.createOperationLog(logDTO);
            
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
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("园区管理");
            logDTO.setAction("更新园区");
            logDTO.setDetail("园区ID:" + id + "，名称:" + parkArea.getName() + "，地址:" + parkArea.getAddress());
            operationLogService.createOperationLog(logDTO);
            
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
        
        if (result) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("园区管理");
            logDTO.setAction("删除园区");
            logDTO.setDetail("园区ID:" + id + "，名称:" + parkArea.getName());
            operationLogService.createOperationLog(logDTO);
        }
        
        return result;
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
        
        // 获取当前时间占用的车位数（space_occupy + reservation）
        LocalDateTime currentTime = LocalDateTime.now();
        Long occupyCount = spaceOccupyMapper.countOccupiedSpacesByParkAreaIdAndTime(parkAreaId, currentTime);
        Long reserveCount = reservationMapper.countReservedSpacesByParkAreaIdAndTime(parkAreaId, currentTime);

        long occupiedSpaces = (occupyCount != null ? occupyCount : 0)
                            + (reserveCount != null ? reserveCount : 0);
        
        return new ParkAreaOccupancyStats(
            parkAreaId,
            parkArea.getName(),
            totalSpaces,
            (int) occupiedSpaces
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
                    
                    // 获取当前时间占用的车位数（space_occupy + reservation）
                    Long occupyCount = spaceOccupyMapper.countOccupiedSpacesByParkAreaIdAndTime(
                        parkArea.getId(), currentTime);
                    Long reserveCount = reservationMapper.countReservedSpacesByParkAreaIdAndTime(
                        parkArea.getId(), currentTime);

                    long occupiedSpaces = (occupyCount != null ? occupyCount : 0)
                                        + (reserveCount != null ? reserveCount : 0);
                    
                    return new ParkAreaOccupancyStats(
                        parkArea.getId(),
                        parkArea.getName(),
                        totalSpaces,
                        (int) occupiedSpaces
                    );
                })
                .collect(Collectors.toList());
    }
}
