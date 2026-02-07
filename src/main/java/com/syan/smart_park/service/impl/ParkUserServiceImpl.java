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
}
