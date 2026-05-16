package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.dao.ParkingSpaceMapper;
import com.syan.smart_park.dao.ParkingZoneMapper;
import com.syan.smart_park.dao.ParkUserMapper;
import com.syan.smart_park.dao.ReservationMapper;
import com.syan.smart_park.dao.SpaceOccupyMapper;
import com.syan.smart_park.entity.*;
import com.syan.smart_park.entity.ParkingSpace;
import com.syan.smart_park.entity.ParkingSpaceDTO;
import com.syan.smart_park.entity.ParkingZone;
import com.syan.smart_park.service.OperationLogService;
import com.syan.smart_park.service.ParkAreaService;
import com.syan.smart_park.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final SpaceOccupyMapper spaceOccupyMapper;
    private final ReservationMapper reservationMapper;
    private final ParkAreaService parkAreaService;
    private final OperationLogService operationLogService;
    private final ParkUserMapper parkUserMapper;

//     @Override
//     public List<ParkingSpaceDTO> getAllParkingSpaces() {
//         List<ParkingSpace> parkingSpaces = this.list();
//         return parkingSpaces.stream()
//                 .map(ParkingSpaceDTO::fromParkingSpace)
//                 .collect(Collectors.toList());
//     }

    @Override
    public PageResult<ParkingSpaceDTO> listParkingSpaces(Long parkAreaId, Long zoneId, Integer status,
                                                         Integer spaceType, Long bindUserId,
                                                         Boolean available, Boolean withOccupied,
                                                         Integer page, Integer size) {
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        if (parkAreaId != null) {
            queryWrapper.eq(ParkingSpace::getParkAreaId, parkAreaId);
        }
        if (zoneId != null) {
            queryWrapper.eq(ParkingSpace::getZoneId, zoneId);
        }
        if (status != null) {
            queryWrapper.eq(ParkingSpace::getStatus, status);
        }
        if (spaceType != null) {
            queryWrapper.eq(ParkingSpace::getSpaceType, spaceType);
        }
        if (bindUserId != null) {
            queryWrapper.eq(ParkingSpace::getBindUserId, bindUserId);
        }
        queryWrapper.orderByAsc(ParkingSpace::getParkAreaId)
                    .orderByAsc(ParkingSpace::getSpaceNumber);

        Page<ParkingSpace> mpPage = new Page<>(page, size);
        Page<ParkingSpace> resultPage = this.page(mpPage, queryWrapper);

        List<ParkingSpaceDTO> dtos = resultPage.getRecords().stream()
                .map(ParkingSpaceDTO::fromParkingSpace)
                .collect(Collectors.toList());

        // 填充绑定用户名
        if (bindUserId != null) {
            ParkUser parkUser = parkUserMapper.selectById(bindUserId);
            String bindUsername = (parkUser != null) ? parkUser.getUsername() : null;
            for (ParkingSpaceDTO dto : dtos) {
                dto.setBindUsername(bindUsername);
            }
        }

        // 过滤空闲车位
        if (available != null && available) {
            LocalDateTime checkTime = LocalDateTime.now();
            dtos = dtos.stream()
                    .filter(dto -> !isSpaceOccupiedAtTime(dto.getId(), checkTime))
                    .collect(Collectors.toList());
        }

        // 设置占用状态（默认计算，除非显式传 withOccupied=false）
        if (withOccupied == null || withOccupied) {
            LocalDateTime currentTime = LocalDateTime.now();
            for (ParkingSpaceDTO dto : dtos) {
                try {
                    dto.setCurrentOccupiedStatus(isSpaceOccupiedAtTime(dto.getId(), currentTime) ? 1 : 0);
                } catch (Exception e) {
                    dto.setCurrentOccupiedStatus(0);
                }
            }
        }

        return PageResult.of(dtos, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
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
        
        // 创建车位后，更新对应园区的总车位数
        if (parkingSpace.getParkAreaId() != null) {
            parkAreaService.updateTotalSpaces(parkingSpace.getParkAreaId());
        }
        
        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("车位管理");
        logDTO.setAction("创建车位");
        logDTO.setDetail("车位ID:" + parkingSpace.getId() + "，编号:" + parkingSpace.getSpaceNumber() + "，园区ID:" + parkingSpace.getParkAreaId());
        operationLogService.createOperationLog(logDTO);
        
        return ParkingSpaceDTO.fromParkingSpace(parkingSpace);
    }

    @Override
    @Transactional
    public List<ParkingSpaceDTO> batchCreateParkingSpaces(ParkingSpaceDTO dto, String prefix, int startNumber, int count) {
        if (count <= 0 || count > 200) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC400,
                "批量创建数量必须在1-200之间"
            );
        }
        if (dto.getParkAreaId() == null) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC400,
                "请选择所属园区"
            );
        }

        List<ParkingSpaceDTO> result = new java.util.ArrayList<>();
        StringBuilder detailLog = new StringBuilder();

        for (int i = 0; i < count; i++) {
            int num = startNumber + i;
            String spaceNumber = prefix + "-" + String.format("%03d", num);

            ParkingSpace space = new ParkingSpace();
            space.setParkAreaId(dto.getParkAreaId());
            space.setZoneId(dto.getZoneId());
            space.setSpaceNumber(spaceNumber);
            space.setSpaceType(dto.getSpaceType() != null ? dto.getSpaceType() : 1);
            space.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            space.setLatitude(dto.getLatitude());
            space.setLongitude(dto.getLongitude());
            this.save(space);

            ParkingSpaceDTO spaceDTO = ParkingSpaceDTO.fromParkingSpace(space);
            result.add(spaceDTO);
            if (detailLog.length() > 0) detailLog.append("，");
            detailLog.append(spaceNumber);
        }

        parkAreaService.updateTotalSpaces(dto.getParkAreaId());

        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("车位管理");
        logDTO.setAction("批量创建车位");
        logDTO.setDetail("园区ID:" + dto.getParkAreaId() + "，数量:" + count + "，编号范围:" + prefix + "-" + String.format("%03d", startNumber) + " ~ " + prefix + "-" + String.format("%03d", startNumber + count - 1));
        operationLogService.createOperationLog(logDTO);

        return result;
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
        
        // 更新现有实体的字段
        existingParkingSpace.setParkAreaId(parkingSpaceDTO.getParkAreaId());
        existingParkingSpace.setZoneId(parkingSpaceDTO.getZoneId());
        existingParkingSpace.setSpaceNumber(parkingSpaceDTO.getSpaceNumber());
        existingParkingSpace.setSpaceType(parkingSpaceDTO.getSpaceType());
        existingParkingSpace.setStatus(parkingSpaceDTO.getStatus());
        existingParkingSpace.setLatitude(parkingSpaceDTO.getLatitude());
        existingParkingSpace.setLongitude(parkingSpaceDTO.getLongitude());
        existingParkingSpace.setBindUserId(parkingSpaceDTO.getBindUserId());
        
        // 直接更新，不使用乐观锁
        boolean updated = this.updateById(existingParkingSpace);
        
        if (!updated) {
            throw new com.syan.smart_park.common.exception.BusinessException(
                com.syan.smart_park.common.exception.ReturnCode.RC500,
                "更新车位失败"
            );
        }
        
        // 记录操作日志
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setModule("车位管理");
        logDTO.setAction("更新车位");
        logDTO.setDetail("车位ID:" + id + "，编号:" + existingParkingSpace.getSpaceNumber() + "，状态:" + existingParkingSpace.getStatus());
        operationLogService.createOperationLog(logDTO);
        
        return ParkingSpaceDTO.fromParkingSpace(existingParkingSpace);
    }

    @Override
    @Transactional
    public boolean deleteParkingSpace(Long id) {
        // 先获取车位信息，以便知道属于哪个园区
        ParkingSpace parkingSpace = this.getById(id);
        if (parkingSpace == null) {
            return false;
        }
        
        Long parkAreaId = parkingSpace.getParkAreaId();
        boolean result = this.removeById(id);
        
        // 删除车位后，更新对应园区的总车位数
        if (result && parkAreaId != null) {
            parkAreaService.updateTotalSpaces(parkAreaId);
        }
        
        // 记录操作日志
        if (result) {
            OperationLogDTO logDTO = new OperationLogDTO();
            logDTO.setModule("车位管理");
            logDTO.setAction("删除车位");
            logDTO.setDetail("车位ID:" + id + "，编号:" + parkingSpace.getSpaceNumber());
            operationLogService.createOperationLog(logDTO);
        }
        
        return result;
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

    @Override
    public boolean isSpaceOccupied(Long spaceId) {
        return isSpaceOccupiedAtTime(spaceId, LocalDateTime.now());
    }

    /**
     * 检查指定车位在指定时间是否被占用
     * 同时检查 space_occupy 表（实际占用）和 reservation 表（有效预约占用）
     *
     * @param spaceId 车位ID
     * @param checkTime 检查时间
     * @return true-占用中，false-未占用
     */
    private boolean isSpaceOccupiedAtTime(Long spaceId, LocalDateTime checkTime) {
        // 检查实际占用记录（车辆正在场内）
        Long occupyCount = spaceOccupyMapper.countOccupiedBySpaceIdAndTime(spaceId, checkTime);
        if (occupyCount != null && occupyCount > 0) {
            return true;
        }
        // 检查有效预约占用（已审批通过、未取消、在预约时间段内）
        Long reserveCount = reservationMapper.countReservedBySpaceIdAndTime(spaceId, checkTime);
        return reserveCount != null && reserveCount > 0;
    }

    @Override
    public ParkingSpaceDTO getParkingSpaceWithOccupiedStatus(Long id) {
        ParkingSpace parkingSpace = this.getById(id);
        if (parkingSpace == null) {
            return null;
        }
        
        ParkingSpaceDTO dto = ParkingSpaceDTO.fromParkingSpace(parkingSpace);
        // 设置当前占用状态
        dto.setCurrentOccupiedStatus(isSpaceOccupied(id) ? 1 : 0);
        return dto;
    }

    @Override
    public List<ParkingSpaceDTO> getSpacesAvailableForBinding() {
        // 1. 获取所有状态正常的车位
        LambdaQueryWrapper<ParkingSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingSpace::getStatus, 1);
        List<ParkingSpace> allNormalSpaces = this.list(queryWrapper);

        // 2. 查询已被长期绑定（reservationId=0）的车位ID列表
        LambdaQueryWrapper<SpaceOccupy> occupyQuery = new LambdaQueryWrapper<>();
        occupyQuery.eq(SpaceOccupy::getReservationId, 0L)
                   .ge(SpaceOccupy::getEndTime, LocalDateTime.of(9999, 12, 31, 0, 0));
        List<SpaceOccupy> boundOccupies = spaceOccupyMapper.selectList(occupyQuery);
        java.util.Set<Long> boundSpaceIds = boundOccupies.stream()
                .map(SpaceOccupy::getSpaceId)
                .collect(Collectors.toSet());

        // 3. 过滤掉已被长期绑定的车位
        return allNormalSpaces.stream()
                .filter(space -> !boundSpaceIds.contains(space.getId()))
                .map(space -> {
                    ParkingSpaceDTO dto = ParkingSpaceDTO.fromParkingSpace(space);
                    dto.setCurrentOccupiedStatus(0);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
