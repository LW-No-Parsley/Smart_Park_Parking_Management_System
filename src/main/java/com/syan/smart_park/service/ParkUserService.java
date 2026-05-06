package com.syan.smart_park.service;

import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;

import java.util.List;

/**
 * 停车场小程序用户服务接口
 */
public interface ParkUserService {

    /**
     * 根据openid查询用户
     */
    ParkUserDTO getByOpenid(String openid);

    /**
     * 根据手机号查询用户
     */
    ParkUserDTO getByPhone(String phone);

    /**
     * 创建或更新用户（微信登录）
     */
    ParkUserDTO createOrUpdate(ParkUser parkUser);

    /**
     * 更新用户最后登录时间
     */
    boolean updateLastLoginTime(Long userId);

    // ====== 园区用户管理 ======

    /**
     * 获取所有园区用户
     */
    List<ParkUserDTO> getAllParkUsers();

    /**
     * 根据ID获取园区用户
     */
    ParkUserDTO getParkUserById(Long id);

    /**
     * 创建园区用户
     */
    ParkUserDTO createParkUser(ParkUser parkUser);

    /**
     * 更新园区用户
     */
    ParkUserDTO updateParkUser(Long id, ParkUser parkUser);

    /**
     * 删除园区用户（软删除）
     */
    void deleteParkUser(Long id);

    /**
     * 根据状态获取园区用户列表
     */
    List<ParkUserDTO> getParkUsersByStatus(Integer status);

    /**
     * 根据用户类型获取园区用户列表
     */
    List<ParkUserDTO> getParkUsersByType(Integer userType);
}
