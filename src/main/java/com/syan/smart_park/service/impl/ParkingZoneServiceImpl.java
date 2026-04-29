package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkAreaMapper;
import com.syan.smart_park.dao.ParkingZoneMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.ParkArea;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.entity.ParkingZoneDTO;
import com.syan.smart_park.service.OperationLogService;
import com.syan.smart_park.service.ParkingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 车位分区服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingZoneServiceImpl extends ServiceImpl<ParkingZoneMapper, ParkingZone> implements ParkingZoneService {

    private final ParkingZoneMapper parkingZoneMapper;
    private final ParkAreaMapper parkAreaMapper;
    private final OperationLogService operationLogService;

    @Override
    public List<ParkingZoneDTO> getAllParkingZones() {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return convertToParkingZoneDTOList(parkingZones);
    }
    
    /**
     * 根据园区ID列表获取园区名称映射
     */
    private Map<Long, String> getParkAreaNameMap(List<Long> parkAreaIds) {
        if (parkAreaIds == null || parkAreaIds.isEmpty()) {
            return Map.of();
        }
        
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ParkArea::getId, parkAreaIds)
                   .eq(ParkArea::getDeleted, 0)
                   .select(ParkArea::getId, ParkArea::getName);
        
        List<ParkArea> parkAreas = parkAreaMapper.selectList(queryWrapper);
        return parkAreas.stream()
                .collect(Collectors.toMap(ParkArea::getId, ParkArea::getName));
    }
    
    /**
     * 将ParkingZone列表转换为ParkingZoneDTO列表，并设置园区名称
     */
    private List<ParkingZoneDTO> convertToParkingZoneDTOList(List<ParkingZone> parkingZones) {
        if (parkingZones == null || parkingZones.isEmpty()) {
            return List.of();
        }
        
        // 获取所有园区ID
        List<Long> parkAreaIds = parkingZones.stream()
                .map(ParkingZone::getParkAreaId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询园区信息
        Map<Long, String> parkAreaNameMap = getParkAreaNameMap(parkAreaIds);
        
        // 转换为DTO并设置园区名称
        return parkingZones.stream()
                .map(parkingZone -> {
                    ParkingZoneDTO dto = ParkingZoneDTO.fromParkingZone(parkingZone);
                    dto.setParkAreaName(parkAreaNameMap.get(parkingZone.getParkAreaId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ParkingZoneDTO getParkingZoneById(Long id) {
        ParkingZone parkingZone = parkingZoneMapper.selectById(id);
        if (parkingZone == null || parkingZone.getDeleted() == 1) {
            return null;
        }
        
        ParkingZoneDTO dto = ParkingZoneDTO.fromParkingZone(parkingZone);
        // 设置园区名称
        ParkArea parkArea = parkAreaMapper.selectById(parkingZone.getParkAreaId());
        if (parkArea != null && parkArea.getDeleted() == 0) {
            dto.setParkAreaName(parkArea.getName());
        }
        return dto;
    }

    @Override
    public ParkingZoneDTO createParkingZone(ParkingZoneDTO parkingZoneDTO) {
        // 检查是否已存在相同园区和分区名称的记录（包括已删除的记录，因为唯一约束包含所有记录）
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getParkAreaId, parkingZoneDTO.getParkAreaId())
                   .eq(ParkingZone::getZoneName, parkingZoneDTO.getZoneName());
        Long count = this.count(queryWrapper);
        
        if (count > 0) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1301, 
                "该园区已存在相同名称的分区"
            );
        }
        
        ParkingZone parkingZone = parkingZoneDTO.toParkingZone();
        parkingZone.setDeleted(0);
        
        int result = parkingZoneMapper.insert(parkingZone);
        if (result > 0) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("分区管理");
            logDTO.setAction("创建分区");
            logDTO.setDetail("分区ID:" + parkingZone.getId() + "，名称:" + parkingZone.getZoneName() + "，园区ID:" + parkingZone.getParkAreaId());
            operationLogService.createOperationLog(logDTO);
            
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
        
        // 如果分区名称或园区ID有变化，需要检查是否会导致重复
        if (!existingParkingZone.getParkAreaId().equals(parkingZoneDTO.getParkAreaId()) ||
            !existingParkingZone.getZoneName().equals(parkingZoneDTO.getZoneName())) {
            
            // 检查是否已存在相同园区和分区名称的记录（排除当前记录，包括已删除的记录）
            LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ParkingZone::getParkAreaId, parkingZoneDTO.getParkAreaId())
                       .eq(ParkingZone::getZoneName, parkingZoneDTO.getZoneName())
                       .ne(ParkingZone::getId, id); // 排除当前记录
            Long count = this.count(queryWrapper);
            
            if (count > 0) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC1301, 
                    "该园区已存在相同名称的分区"
                );
            }
        }
        
        ParkingZone parkingZone = parkingZoneDTO.toParkingZone();
        parkingZone.setId(id);
        parkingZone.setDeleted(existingParkingZone.getDeleted());
        
        int result = parkingZoneMapper.updateById(parkingZone);
        if (result > 0) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("分区管理");
            logDTO.setAction("更新分区");
            logDTO.setDetail("分区ID:" + id + "，名称:" + parkingZone.getZoneName() + "，园区ID:" + parkingZone.getParkAreaId());
            operationLogService.createOperationLog(logDTO);
            
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
        
        // 使用MyBatis-Plus的removeById方法，它会自动处理逻辑删除（因为有@TableLogic注解）
        boolean result = this.removeById(id);
        
        if (result) {
            // 记录操作日志
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("分区管理");
            logDTO.setAction("删除分区");
            logDTO.setDetail("分区ID:" + id + "，名称:" + parkingZone.getZoneName());
            operationLogService.createOperationLog(logDTO);
        }
        
        return result;
    }

    @Override
    public List<ParkingZoneDTO> getParkingZonesByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .eq(ParkingZone::getParkAreaId, parkAreaId)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return convertToParkingZoneDTOList(parkingZones);
    }

    @Override
    public List<ParkingZoneDTO> getParkingZonesByStatus(Integer status) {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .eq(ParkingZone::getStatus, status)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return convertToParkingZoneDTOList(parkingZones);
    }

    @Override
    public List<ParkingZoneDTO> searchParkingZones(String keyword) {
        LambdaQueryWrapper<ParkingZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingZone::getDeleted, 0)
                   .like(ParkingZone::getZoneName, keyword)
                   .orderByAsc(ParkingZone::getSortOrder)
                   .orderByDesc(ParkingZone::getCreateTime);
        
        List<ParkingZone> parkingZones = parkingZoneMapper.selectList(queryWrapper);
        return convertToParkingZoneDTOList(parkingZones);
    }

    @Override
    public boolean batchUpdateParkingZoneStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<ParkingZone> parkingZones = ids.stream()
                .map(id -> {
                    ParkingZone parkingZone = new ParkingZone();
                    parkingZone.setId(id);
                    parkingZone.setStatus(status);
                    return parkingZone;
                })
                .collect(Collectors.toList());
        
        return this.updateBatchById(parkingZones);
    }

    @Override
    public boolean batchDeleteParkingZones(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 先查询出所有记录
        List<ParkingZone> parkingZones = this.listByIds(ids);
        if (parkingZones.isEmpty()) {
            return false;
        }
        
        // 过滤掉已删除的记录
        List<ParkingZone> toDelete = parkingZones.stream()
                .filter(pz -> pz.getDeleted() == 0)
                .collect(Collectors.toList());
        
        if (toDelete.isEmpty()) {
            return false;
        }
        
        // 批量逻辑删除
        for (ParkingZone parkingZone : toDelete) {
            parkingZone.setDeleted(1);
        }
        
        return this.updateBatchById(toDelete);
    }
}
