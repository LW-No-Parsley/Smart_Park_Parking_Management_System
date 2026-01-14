package com.syan.smart_park.service;

import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;

/**
 * 停车场小程序用户服务接口
 */
public interface ParkUserService {
    
    /**
     * 根据openid查询用户
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    ParkUserDTO getByOpenid(String openid);
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    ParkUserDTO getByPhone(String phone);
    
    /**
     * 创建或更新用户（微信登录）
     *
     * @param parkUser 用户信息
     * @return 用户DTO
     */
    ParkUserDTO createOrUpdate(ParkUser parkUser);
    
    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateLastLoginTime(Long userId);
}
