package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.ParkingSpaceMapper;
import com.syan.smart_park.dao.ParkingZoneMapper;
import com.syan.smart_park.entity.ParkingSpace;
import com.syan.smart_park.entity.ParkingSpaceDTO;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车位服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingZoneMapper parkingZoneMapper;

    @Override
    public List<ParkingSpaceDTO> getAllParkingSpaces() {
        List<ParkingSpace> parkingSpaces = this.list();
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public ParkingSpaceDTO getParkingSpaceById(Long id) {
        ParkingSpace parkingSpace = this.getById(id);
        return ParkingSpaceDTO.fromParkingSpace(parkingSpace);
    }

    @Override
    @Transactional
    public ParkingSpaceDTO createParkingSpace(ParkingSpaceDTO parkingSpaceDTO) {
        // 检查zoneId是否有效（如果提供了zoneId）
        if (parkingSpaceDTO.getZoneId() != null) {
            // 检查分区是否存在且未删除
            LambdaQueryWrapper<ParkingZone> zoneQueryWrapper = new LambdaQueryWrapper<>();
            zoneQueryWrapper.eq(ParkingZone::getId, parkingSpaceDTO.getZoneId())
                           .eq(ParkingZone::getDeleted, 0);
            Long zoneCount = parkingZoneMapper.selectCount(zoneQueryWrapper);
            
            if (zoneCount == 0) {
                throw new com.syan.smart_park.common.exception.BusinessException(
                    com.syan.smart_park.common.exception.ReturnCode.RC1301, 
                    "指定的分区不存在或已被删除"
                );
            }
        }
        
        ParkingSpace parkingSpace = parkingSpaceDTO.toParkingSpace();
        this.save(parkingSpace);
        return ParkingSpaceDTO.fromParkingSpace(parkingSpace);
    }

    @Override
    @Transactional
    public ParkingSpaceDTO updateParkingSpace(Long id, ParkingSpaceDTO parkingSpaceDTO) {
        // 使用FOR UPDATE锁定记录，防止并发冲突
        ParkingSpace existingParkingSpace = parkingSpaceMapper.selectForUpdate(id);
        if (existingParkingSpace == null) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC1300,
                "车位不存在或已被删除"
            );
        }
        
        ParkingSpace parkingSpace = parkingSpaceDTO.toParkingSpace();
        parkingSpace.setId(id);
        // 设置乐观锁版本号
        parkingSpace.setVersion(existingParkingSpace.getVersion());
        this.updateById(parkingSpace);
        
        return ParkingSpaceDTO.fromParkingSpace(parkingSpace);
    }

    @Override
    @Transactional
    public boolean deleteParkingSpace(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<ParkingSpaceDTO> getParkingSpacesByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getParkAreaId, parkAreaId);
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSpaceDTO> getParkingSpacesByZoneId(Long zoneId) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getZoneId, zoneId);
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSpaceDTO> getParkingSpacesByStatus(Integer status) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getStatus, status);
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSpaceDTO> getParkingSpacesByType(Integer spaceType) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getSpaceType, spaceType);
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSpaceDTO> getParkingSpacesByBindUserId(Long bindUserId) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getBindUserId, bindUserId);
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSpaceDTO> getAvailableParkingSpaces() {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getStatus, 1); // 1表示空闲状态
        List<ParkingSpace> parkingSpaces = this.list(queryWrapper);
        
        return parkingSpaces.stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean batchUpdateParkingSpaceStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<ParkingSpace> parkingSpaces = ids.stream()
                .map(id -> {
                    ParkingSpace parkingSpace = new ParkingSpace();
                    parkingSpace.setId(id);
                    parkingSpace.setStatus(status);
                    return parkingSpace;
                })
                .collect(Collectors.toList());
        
        return this.updateBatchById(parkingSpaces);
    }
}
