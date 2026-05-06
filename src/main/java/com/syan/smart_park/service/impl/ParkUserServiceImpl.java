package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.ParkUserMapper;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.service.ParkUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停车场小程序用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkUserServiceImpl implements ParkUserService {

    private final ParkUserMapper parkUserMapper;

    @Override
    public ParkUserDTO getByOpenid(String openid) {
        ParkUser parkUser = parkUserMapper.selectByOpenid(openid);
        return ParkUserDTO.fromParkUser(parkUser);
    }

    @Override
    public ParkUserDTO getByPhone(String phone) {
        ParkUser parkUser = parkUserMapper.selectByPhone(phone);
        return ParkUserDTO.fromParkUser(parkUser);
    }

    @Override
    @Transactional
    public ParkUserDTO createOrUpdate(ParkUser parkUser) {
        if (parkUser == null) {
            throw new BusinessException(ReturnCode.RC600); // 用户信息不能为空
        }

        // 检查用户状态
        if (parkUser.getStatus() != null && parkUser.getStatus() == 0) {
            throw new BusinessException(ReturnCode.RC602); // 用户已被禁用
        }

        // 根据openid查询是否已存在用户
        ParkUser existingUser = parkUserMapper.selectByOpenid(parkUser.getOpenid());
        
        if (existingUser == null) {
            // 新用户注册
            parkUser.setStatus(1); // 默认启用
            parkUser.setCreateTime(LocalDateTime.now());
            parkUser.setUpdateTime(LocalDateTime.now());
            parkUser.setDeleted(0);
            
            int result = parkUserMapper.insert(parkUser);
            if (result <= 0) {
                throw new BusinessException(ReturnCode.RC500); // 系统异常
            }
        } else {
            // 更新用户信息
            parkUser.setId(existingUser.getId());
            parkUser.setUpdateTime(LocalDateTime.now());
            
            // 保留原有的一些字段
            parkUser.setCreateTime(existingUser.getCreateTime());
            parkUser.setStatus(existingUser.getStatus());
            parkUser.setDeleted(existingUser.getDeleted());
            
            int result = parkUserMapper.updateById(parkUser);
            if (result <= 0) {
                throw new BusinessException(ReturnCode.RC500); // 系统异常
            }
        }

        // 更新最后登录时间
        updateLastLoginTime(parkUser.getId());

        return ParkUserDTO.fromParkUser(parkUser);
    }

    @Override
    public boolean updateLastLoginTime(Long userId) {
        if (userId == null) {
            return false;
        }

        UpdateWrapper<ParkUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                    .set("update_time", LocalDateTime.now());

        int result = parkUserMapper.update(null, updateWrapper);
        return result > 0;
    }

    // ====== 园区用户管理 ======

    @Override
    public List<ParkUserDTO> getAllParkUsers() {
        QueryWrapper<ParkUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByAsc("create_time");
        return parkUserMapper.selectList(queryWrapper).stream()
                .map(ParkUserDTO::fromParkUser)
                .collect(Collectors.toList());
    }

    @Override
    public ParkUserDTO getParkUserById(Long id) {
        ParkUser parkUser = parkUserMapper.selectById(id);
        if (parkUser == null || parkUser.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }
        return ParkUserDTO.fromParkUser(parkUser);
    }

    @Override
    @Transactional
    public ParkUserDTO createParkUser(ParkUser parkUser) {
        // 检查 openid 唯一性
        if (parkUser.getOpenid() != null && !parkUser.getOpenid().isEmpty()) {
            ParkUser existing = parkUserMapper.selectByOpenid(parkUser.getOpenid());
            if (existing != null) {
                throw new BusinessException(ReturnCode.RC603, "该微信已注册");
            }
        }

        // 检查手机号唯一性
        if (parkUser.getPhone() != null && !parkUser.getPhone().isEmpty()) {
            ParkUser existing = parkUserMapper.selectByPhone(parkUser.getPhone());
            if (existing != null) {
                throw new BusinessException(ReturnCode.RC603, "该手机号已存在");
            }
        }

        parkUser.setStatus(parkUser.getStatus() != null ? parkUser.getStatus() : 1);
        parkUser.setUserType(parkUser.getUserType() != null ? parkUser.getUserType() : 2);
        parkUser.setCreateTime(LocalDateTime.now());
        parkUser.setUpdateTime(LocalDateTime.now());
        parkUser.setDeleted(0);
        parkUserMapper.insert(parkUser);
        return ParkUserDTO.fromParkUser(parkUser);
    }

    @Override
    @Transactional
    public ParkUserDTO updateParkUser(Long id, ParkUser parkUser) {
        ParkUser existing = parkUserMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }

        // 如果修改手机号，检查唯一性
        if (parkUser.getPhone() != null && !parkUser.getPhone().equals(existing.getPhone())) {
            ParkUser phoneUser = parkUserMapper.selectByPhone(parkUser.getPhone());
            if (phoneUser != null && !phoneUser.getId().equals(id)) {
                throw new BusinessException(ReturnCode.RC603, "该手机号已被其他用户使用");
            }
        }

        parkUser.setId(id);
        parkUser.setUpdateTime(LocalDateTime.now());
        parkUserMapper.updateById(parkUser);
        return ParkUserDTO.fromParkUser(parkUserMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteParkUser(Long id) {
        ParkUser parkUser = parkUserMapper.selectById(id);
        if (parkUser == null || parkUser.getDeleted() == 1) {
            throw new BusinessException(ReturnCode.RC600); // 用户不存在
        }
        parkUser.setDeleted(1);
        parkUser.setUpdateTime(LocalDateTime.now());
        parkUserMapper.updateById(parkUser);
    }

    @Override
    public List<ParkUserDTO> getParkUsersByStatus(Integer status) {
        QueryWrapper<ParkUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("status", status)
                   .orderByAsc("create_time");
        return parkUserMapper.selectList(queryWrapper).stream()
                .map(ParkUserDTO::fromParkUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkUserDTO> getParkUsersByType(Integer userType) {
        QueryWrapper<ParkUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("user_type", userType)
                   .orderByAsc("create_time");
        return parkUserMapper.selectList(queryWrapper).stream()
                .map(ParkUserDTO::fromParkUser)
                .collect(Collectors.toList());
    }
}
