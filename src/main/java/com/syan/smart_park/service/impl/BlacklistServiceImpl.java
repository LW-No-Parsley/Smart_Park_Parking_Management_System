package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.dao.BlacklistMapper;
import com.syan.smart_park.entity.Blacklist;
import com.syan.smart_park.entity.BlacklistDTO;
import com.syan.smart_park.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 黑名单服务实现类
 */
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    private final BlacklistMapper blacklistMapper;

    @Override
    public List<BlacklistDTO> getAllBlacklists() {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public BlacklistDTO getBlacklistById(Long id) {
        Blacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null || blacklist.getDeleted() == 1) {
            return null;
        }
        return BlacklistDTO.fromBlacklist(blacklist);
    }

    @Override
    public BlacklistDTO createBlacklist(BlacklistDTO blacklistDTO) {
        Blacklist blacklist = blacklistDTO.toBlacklist();
        blacklist.setDeleted(0);
        
        int result = blacklistMapper.insert(blacklist);
        if (result > 0) {
            return getBlacklistById(blacklist.getId());
        }
        return null;
    }

    @Override
    public BlacklistDTO updateBlacklist(Long id, BlacklistDTO blacklistDTO) {
        Blacklist existingBlacklist = blacklistMapper.selectById(id);
        if (existingBlacklist == null || existingBlacklist.getDeleted() == 1) {
            return null;
        }
        
        Blacklist blacklist = blacklistDTO.toBlacklist();
        blacklist.setId(id);
        blacklist.setDeleted(existingBlacklist.getDeleted());
        
        int result = blacklistMapper.updateById(blacklist);
        if (result > 0) {
            return getBlacklistById(id);
        }
        return null;
    }

    @Override
    public boolean deleteBlacklist(Long id) {
        Blacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null || blacklist.getDeleted() == 1) {
            return false;
        }
        
        blacklist.setDeleted(1);
        int result = blacklistMapper.updateById(blacklist);
        return result > 0;
    }

    @Override
    public List<BlacklistDTO> getBlacklistsByPlateNumber(String plateNumber) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .eq(Blacklist::getPlateNumber, plateNumber)
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public List<BlacklistDTO> getBlacklistsByStatus(Integer status) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .eq(Blacklist::getStatus, status)
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public boolean isPlateNumberInBlacklist(String plateNumber) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .eq(Blacklist::getPlateNumber, plateNumber)
                   .eq(Blacklist::getStatus, 1) // 生效状态
                   .le(Blacklist::getStartTime, LocalDateTime.now()) // 生效时间 <= 当前时间
                   .ge(Blacklist::getEndTime, LocalDateTime.now()); // 失效时间 >= 当前时间
        
        return blacklistMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<BlacklistDTO> getActiveBlacklists() {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .eq(Blacklist::getStatus, 1) // 生效状态
                   .le(Blacklist::getStartTime, LocalDateTime.now()) // 生效时间 <= 当前时间
                   .ge(Blacklist::getEndTime, LocalDateTime.now()) // 失效时间 >= 当前时间
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public List<BlacklistDTO> getExpiredBlacklists() {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .lt(Blacklist::getEndTime, LocalDateTime.now()) // 失效时间 < 当前时间
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Blacklist::getId, ids)
                   .eq(Blacklist::getDeleted, 0);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        if (blacklists.isEmpty()) {
            return false;
        }
        
        for (Blacklist blacklist : blacklists) {
            blacklist.setStatus(status);
            blacklistMapper.updateById(blacklist);
        }
        
        return true;
    }

    @Override
    public List<BlacklistDTO> getBlacklistsByCreatedBy(Long createdBy) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .eq(Blacklist::getCreatedBy, createdBy)
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }

    @Override
    public List<BlacklistDTO> searchBlacklists(String keyword) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getDeleted, 0)
                   .and(wrapper -> wrapper
                       .like(Blacklist::getPlateNumber, keyword)
                       .or()
                       .like(Blacklist::getReason, keyword))
                   .orderByDesc(Blacklist::getCreateTime);
        
        List<Blacklist> blacklists = blacklistMapper.selectList(queryWrapper);
        return blacklists.stream()
                         .map(BlacklistDTO::fromBlacklist)
                         .collect(Collectors.toList());
    }
}
